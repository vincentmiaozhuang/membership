#!/bin/bash

# ==============================================================================
# 会员卡管理系统 - 生产环境外部接口一键重启脚本
# 功能：获取最新代码 → 停止服务 → 重新编译 → 启动服务 → 重启Nginx
# 使用方式：bash online-restart-external.sh
# 适用环境：阿里云 Linux 生产服务器
#####
# ==============================================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置参数
APP_NAME="membership-external"
PROJECT_DIR="/data/www/membership-system"
BACKEND_DIR="${PROJECT_DIR}/membership-external"
LOG_DIR="/var/log/membership"
PID_FILE="/var/run/${APP_NAME}.pid"
JAR_FILE="${BACKEND_DIR}/target/membership-external-1.0.0.jar"
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

# 确保目录存在
mkdir -p "${LOG_DIR}"

echo -e "\n${BLUE}===============================================${NC}"
echo -e "${BLUE}   会员卡管理系统 - 生产环境外部接口重启脚本${NC}"
echo -e "${BLUE}===============================================${NC}"
echo -e "$(date '+%Y-%m-%d %H:%M:%S')"

# ------------------------------------------------------------------------------
# 步骤1: 获取最新代码
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤1】获取最新代码...${NC}"

cd "${PROJECT_DIR}" || exit 1

echo "  - 拉取远程代码..."
git fetch origin

echo "  - 强制重置到远程 main 分支..."
git reset --hard origin/main

# 注意：不执行 git clean，避免删除前端构建产物

echo -e "    ${GREEN}✓ 代码更新完成${NC}"
echo -e "    ${GREEN}✓ 当前提交: $(git rev-parse --short HEAD)${NC}"

# ------------------------------------------------------------------------------
# 步骤2: 停止现有服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤2】停止现有服务...${NC}"

echo "  - 查找并停止外部接口服务..."
PID=$(ps -ef | grep membership-external | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "    找到进程: $PID"
    kill -9 "$PID" 2>/dev/null
    sleep 3
    echo -e "    ${GREEN}✓ 外部接口服务已停止${NC}"
else
    echo -e "    ${YELLOW}提示: 未找到运行中的外部接口服务${NC}"
fi

# ------------------------------------------------------------------------------
# 步骤3: 重新编译后端项目
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤3】重新编译后端项目...${NC}"

cd "${PROJECT_DIR}"
echo "  - 编译 membership-core 和 membership-external..."

if mvn clean package -DskipTests -pl membership-core,membership-external; then
    echo -e "    ${GREEN}✓ 编译成功${NC}"
else
    echo -e "    ${RED}✗ 编译失败！${NC}"
    exit 1
fi

# ------------------------------------------------------------------------------
# 步骤4: 启动外部接口服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤4】启动外部接口服务...${NC}"

cd "${BACKEND_DIR}"

# 检查jar文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo -e "    ${RED}✗ JAR文件不存在: $JAR_FILE${NC}"
    exit 1
fi

echo "  - 启动外部接口服务..."
nohup java ${JAVA_OPTS} -jar "${JAR_FILE}" > "${LOG_DIR}/external-backend.log" 2>&1 &

# 保存 PID
echo $! > "${PID_FILE}"

# 等待服务启动
echo "  - 等待服务启动..."
SERVICE_STARTED=false
for i in {1..30}; do
    if curl -s http://localhost:8081/api/external/v1/customers > /dev/null 2>&1; then
        SERVICE_STARTED=true
        break
    fi
    sleep 1
    echo -n "."
done
echo ""

if [ "$SERVICE_STARTED" = true ]; then
    echo -e "    ${GREEN}✓ 外部接口服务启动成功${NC}"
else
    echo -e "    ${RED}✗ 服务启动超时！查看日志: ${LOG_DIR}/external-backend.log${NC}"
    exit 1
fi

# ------------------------------------------------------------------------------
# 步骤5: 重启 Nginx
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤5】重启 Nginx...${NC}"

echo "  - 测试 Nginx 配置..."
if nginx -t; then
    echo "  - 重载 Nginx..."
    sudo systemctl reload nginx
    echo -e "    ${GREEN}✓ Nginx 重载成功${NC}"
else
    echo -e "    ${RED}✗ Nginx 配置错误！${NC}"
    exit 1
fi

# ------------------------------------------------------------------------------
# 完成提示
# ------------------------------------------------------------------------------
echo -e "\n${GREEN}===============================================${NC}"
echo -e "${GREEN}              生产环境部署完成！${NC}"
echo -e "${GREEN}===============================================${NC}"
echo -e "\n${BLUE}发布信息:${NC}"
echo -e "  ├─ 项目目录: ${PROJECT_DIR}"
echo -e "  ├─ Git 提交: $(git rev-parse --short HEAD)"
echo -e "  └─ 发布时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo -e "\n${BLUE}服务状态:${NC}"
echo -e "  ├─ 外部接口服务: ${GREEN}✓ 运行中${NC} (端口 8081)"
echo -e "  └─ Nginx: ${GREEN}✓ 已重载${NC}"
echo -e "\n${BLUE}访问地址:${NC}"
echo -e "  └─ http://您的域名/api/external/v1/customers"
echo -e "\n${BLUE}日志位置:${NC}"
echo -e "  ├─ 服务日志: ${LOG_DIR}/external-backend.log"
echo -e "  └─ Nginx日志: /var/log/nginx/"
echo -e "\n${GREEN}===============================================${NC}"
echo -e "${GREEN}              部署完成！${NC}"
echo -e "${GREEN}===============================================${NC}"
