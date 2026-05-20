#!/bin/bash

# ==============================================================================
# 会员卡管理系统 - 外部接口服务部署脚本
# 使用方式：bash deploy-external.sh
# 适用环境：阿里云 Linux 服务器
# ==============================================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 配置参数
APP_NAME="membership-external"
APP_DIR="/opt/apps/membership-system"
JAR_FILE="${APP_DIR}/membership-external/target/membership-external-1.0.0.jar"
LOG_DIR="/var/log/membership"
PID_FILE="/var/run/${APP_NAME}.pid"
GIT_REPO="https://github.com/vincentmiaozhuang/membership.git"
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"

echo -e "\n${BLUE}===============================================${NC}"
echo -e "${BLUE}      会员卡管理系统 - 外部接口服务部署脚本${NC}"
echo -e "${BLUE}===============================================${NC}"
echo -e "$(date '+%Y-%m-%d %H:%M:%S')"

# ------------------------------------------------------------------------------
# 步骤1: 检查并安装依赖
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤1】检查系统依赖...${NC}"

# 检查 Java
if ! command -v java &> /dev/null; then
    echo "  - 安装 Java 8..."
    sudo yum install -y java-1.8.0-openjdk-devel
fi
echo -e "    ${GREEN}✓ Java 版本: $(java -version 2>&1 | head -n 1)${NC}"

# 检查 Maven
if ! command -v mvn &> /dev/null; then
    echo "  - 安装 Maven..."
    sudo yum install -y maven
fi
echo -e "    ${GREEN}✓ Maven 版本: $(mvn -v | head -n 1)${NC}"

# 检查 Git
if ! command -v git &> /dev/null; then
    echo "  - 安装 Git..."
    sudo yum install -y git
fi
echo -e "    ${GREEN}✓ Git 版本: $(git --version)${NC}"

# ------------------------------------------------------------------------------
# 步骤2: 创建目录结构
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤2】创建目录结构...${NC}"

sudo mkdir -p "${APP_DIR}"
sudo mkdir -p "${LOG_DIR}"
sudo mkdir -p "/opt/apps"

echo -e "    ${GREEN}✓ 应用目录: ${APP_DIR}${NC}"
echo -e "    ${GREEN}✓ 日志目录: ${LOG_DIR}${NC}"

# ------------------------------------------------------------------------------
# 步骤3: 拉取代码
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤3】拉取最新代码...${NC}"

if [ -d "${APP_DIR}/.git" ]; then
    echo "  - 更新现有仓库..."
    cd "${APP_DIR}" && git pull origin main
else
    echo "  - 克隆仓库..."
    git clone "${GIT_REPO}" "${APP_DIR}"
fi

cd "${APP_DIR}"
echo -e "    ${GREEN}✓ 当前分支: $(git rev-parse --abbrev-ref HEAD)${NC}"
echo -e "    ${GREEN}✓ 当前提交: $(git rev-parse --short HEAD)${NC}"

# ------------------------------------------------------------------------------
# 步骤4: 编译打包
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤4】编译打包...${NC}"

cd "${APP_DIR}"
mvn clean package -DskipTests -pl membership-core,membership-external

if [ $? -eq 0 ]; then
    echo -e "    ${GREEN}✓ 编译打包成功${NC}"
else
    echo -e "    ${RED}✗ 编译打包失败！${NC}"
    exit 1
fi

# ------------------------------------------------------------------------------
# 步骤5: 停止旧服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤5】停止旧服务...${NC}"

if [ -f "${PID_FILE}" ]; then
    PID=$(cat "${PID_FILE}")
    if kill -0 "${PID}" 2>/dev/null; then
        echo "  - 停止进程 ${PID}..."
        kill -TERM "${PID}"
        sleep 5
        if kill -0 "${PID}" 2>/dev/null; then
            echo "  - 强制终止进程..."
            kill -9 "${PID}"
        fi
    fi
    rm -f "${PID_FILE}"
fi
echo -e "    ${GREEN}✓ 旧服务已停止${NC}"

# ------------------------------------------------------------------------------
# 步骤6: 启动服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤6】启动服务...${NC}"

cd "${APP_DIR}/membership-external"
nohup java ${JAVA_OPTS} -jar "${JAR_FILE}" > "${LOG_DIR}/external-backend.log" 2>&1 &

# 保存 PID
echo $! > "${PID_FILE}"

# 等待启动
echo "  - 等待服务启动..."
for i in {1..30}; do
    if curl -s http://localhost:8081/api/external/v1/customers > /dev/null 2>&1; then
        echo -e "    ${GREEN}✓ 服务启动成功${NC}"
        break
    fi
    sleep 1
    echo -n "."
done
echo ""

# ------------------------------------------------------------------------------
# 步骤7: 检查服务状态
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤7】检查服务状态...${NC}"

if curl -s http://localhost:8081/api/external/v1/customers > /dev/null 2>&1; then
    echo -e "    ${GREEN}✓ 服务运行正常${NC}"
    echo -e "    ${GREEN}✓ 服务地址: http://localhost:8081${NC}"
else
    echo -e "    ${RED}✗ 服务启动失败！查看日志: ${LOG_DIR}/external-backend.log${NC}"
    exit 1
fi

# ------------------------------------------------------------------------------
# 完成提示
# ------------------------------------------------------------------------------
echo -e "\n${GREEN}===============================================${NC}"
echo -e "${GREEN}           外部接口服务部署完成！${NC}"
echo -e "${GREEN}===============================================${NC}"
echo -e "\n${BLUE}服务信息:${NC}"
echo -e "  ├─ 服务名称: ${APP_NAME}"
echo -e "  ├─ 服务端口: 8081"
echo -e "  ├─ 服务地址: http://localhost:8081"
echo -e "  ├─ PID文件: ${PID_FILE}"
echo -e "  └─ 日志文件: ${LOG_DIR}/external-backend.log"
echo -e "\n${BLUE}管理命令:${NC}"
echo -e "  ├─ 启动:   nohup java ${JAVA_OPTS} -jar ${JAR_FILE} > ${LOG_DIR}/external-backend.log 2>&1 &"
echo -e "  ├─ 停止:   kill -TERM \$(cat ${PID_FILE})"
echo -e "  └─ 日志:   tail -f ${LOG_DIR}/external-backend.log"
echo -e "\n${BLUE}可用接口:${NC}"
echo -e "  ├─ GET  /api/external/v1/customers        - 查询客户列表"
echo -e "  └─ GET  /api/external/v1/customers/{id}   - 查询单个客户"
echo -e "\n${GREEN}部署完成！请配置 Nginx 反向代理后访问。${NC}"
echo -e "${GREEN}===============================================${NC}"
