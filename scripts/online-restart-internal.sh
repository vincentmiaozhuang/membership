#!/bin/bash

# ==============================================================================
# 会员卡管理系统 - 生产环境一键发布脚本（内部管理平台）
# 使用方式：bash online-restart-internal.sh
# 适用环境：阿里云 Linux 生产服务器
# 功能：更新代码 → 重新编译 → 重启前后端服务
# ==============================================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置参数
APP_NAME="membership-internal"
PROJECT_DIR="/data/www/membership-system"
JAR_FILE="${PROJECT_DIR}/membership-internal/target/membership-internal-1.0.0.jar"
LOG_DIR="/var/log/membership"
PID_FILE="/var/run/${APP_NAME}.pid"
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

echo -e "\n${BLUE}===============================================${NC}"
echo -e "${BLUE}    会员卡管理系统 - 生产环境一键发布脚本${NC}"
echo -e "${BLUE}===============================================${NC}"
echo -e "$(date '+%Y-%m-%d %H:%M:%S')"

# ------------------------------------------------------------------------------
# 步骤1: 停止原有服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤1】停止原有服务...${NC}"

echo "  - 查找并停止后端服务..."
PID=$(ps -ef | grep membership-internal | grep -v grep | awk '{print $2}')
if [ -n "$PID" ]; then
    echo "    找到进程: $PID"
    kill -9 "$PID" 2>/dev/null
    sleep 3
    echo -e "    ${GREEN}✓ 后端服务已停止${NC}"
else
    echo -e "    ${YELLOW}提示: 未找到运行中的后端服务${NC}"
fi

# ------------------------------------------------------------------------------
# 步骤2: 获取最新代码
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤2】获取最新代码...${NC}"

cd "${PROJECT_DIR}" || exit 1

echo "  - 查看当前状态..."
git status

echo "  - 拉取远程代码..."
git fetch origin

echo "  - 强制重置到远程 main 分支..."
git reset --hard origin/main

echo "  - 删除未跟踪文件..."
git clean -f -d

echo -e "    ${GREEN}✓ 代码更新完成${NC}"
echo -e "    ${GREEN}✓ 当前提交: $(git rev-parse --short HEAD)${NC}"

# ------------------------------------------------------------------------------
# 步骤3: 重新编译后端
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤3】重新编译后端...${NC}"

echo "  - 开始编译..."
cd "${PROJECT_DIR}"
mvn clean package -DskipTests -pl membership-core,membership-internal

if [ $? -eq 0 ]; then
    echo -e "    ${GREEN}✓ 编译成功${NC}"
else
    echo -e "    ${RED}✗ 编译失败！${NC}"
    exit 1
fi

# ------------------------------------------------------------------------------
# 步骤4: 重新启动后端服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤4】重新启动后端服务...${NC}"

cd "${PROJECT_DIR}/membership-internal"
nohup java ${JAVA_OPTS} -jar "${JAR_FILE}" > "${LOG_DIR}/internal-backend.log" 2>&1 &

# 保存 PID
echo $! > "${PID_FILE}"

# 等待启动
echo "  - 等待服务启动..."
SERVICE_STARTED=false
for i in {1..30}; do
    if curl -s http://localhost:8080/auth/login > /dev/null 2>&1; then
        SERVICE_STARTED=true
        break
    fi
    sleep 1
    echo -n "."
done
echo ""

if [ "$SERVICE_STARTED" = true ]; then
    echo -e "    ${GREEN}✓ 后端服务启动成功${NC}"
else
    echo -e "    ${RED}✗ 后端服务启动失败！${NC}"
    echo -e "    ${RED}  查看日志: tail -f ${LOG_DIR}/internal-backend.log${NC}"
    exit 1
fi

# ------------------------------------------------------------------------------
# 步骤5: 前端构建
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤5】前端构建...${NC}"

echo "  - 进入前端目录..."
cd "${PROJECT_DIR}/frontend"

echo "  - 安装依赖..."
npm install

echo "  - 构建生产版本..."
npm run build

if [ $? -eq 0 ]; then
    echo -e "    ${GREEN}✓ 前端构建成功${NC}"
else
    echo -e "    ${RED}✗ 前端构建失败！${NC}"
    exit 1
fi

echo "  - Nginx 已直接指向 dist 目录，无需复制文件"
echo -e "    ${GREEN}✓ 前端构建完成${NC}"

# ------------------------------------------------------------------------------
# 步骤6: 重载 Nginx（平滑重启，不中断服务）
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤6】重载 Nginx...${NC}"

sudo systemctl reload nginx

if [ $? -eq 0 ]; then
    echo -e "    ${GREEN}✓ Nginx 重启成功${NC}"
else
    echo -e "    ${RED}✗ Nginx 重启失败！${NC}"
    exit 1
fi

# ------------------------------------------------------------------------------
# 完成提示
# ------------------------------------------------------------------------------
echo -e "\n${GREEN}===============================================${NC}"
echo -e "${GREEN}              生产环境发布完成！${NC}"
echo -e "${GREEN}===============================================${NC}"
echo -e "\n${BLUE}发布信息:${NC}"
echo -e "  ├─ 项目目录: ${PROJECT_DIR}"
echo -e "  ├─ Git 提交: $(git rev-parse --short HEAD)"
echo -e "  └─ 发布时间: $(date '+%Y-%m-%d %H:%M:%S')"
echo -e "\n${BLUE}服务状态:${NC}"
echo -e "  ├─ 后端服务: ${GREEN}✓ 运行中${NC} (端口 8080)"
echo -e "  ├─ 前端服务: ${GREEN}✓ 已构建${NC} (Nginx 直接指向 dist)"
echo -e "  └─ Nginx: ${GREEN}✓ 已重载${NC}"
echo -e "\n${BLUE}访问地址:${NC}"
echo -e "  └─ http://localhost"
echo -e "\n${BLUE}日志位置:${NC}"
echo -e "  ├─ 后端日志: ${LOG_DIR}/internal-backend.log"
echo -e "  └─ Nginx日志: /var/log/nginx/"
echo -e "\n${GREEN}===============================================${NC}"
echo -e "${GREEN}              发布完成！${NC}"
echo -e "${GREEN}===============================================${NC}"
