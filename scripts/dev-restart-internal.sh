#!/bin/bash

# ==============================================================================
# 会员卡管理系统 - 一键重启脚本
# 功能：停止服务 → 重新编译 → 启动后端 → 启动前端
# 使用方式：./restart-internal.sh
# ==============================================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目路径
PROJECT_DIR="/Users/vincentmiao/Documents/trae_projects/members/membership-system"
BACKEND_DIR="${PROJECT_DIR}/membership-internal"
FRONTEND_DIR="${PROJECT_DIR}/frontend"

# 日志文件
LOG_FILE="${PROJECT_DIR}/logs/restart.log"

# 确保日志目录存在
mkdir -p "${PROJECT_DIR}/logs"

echo -e "\n${BLUE}===============================================${NC}"
echo -e "${BLUE}         会员卡管理系统 - 一键重启脚本${NC}"
echo -e "${BLUE}===============================================${NC}"
echo -e "$(date '+%Y-%m-%d %H:%M:%S')" | tee -a "$LOG_FILE"

# ------------------------------------------------------------------------------
# 步骤1: 停止现有服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤1】停止现有服务...${NC}" | tee -a "$LOG_FILE"

# 停止后端服务（端口8080）
if lsof -ti:8080 > /dev/null 2>&1; then
    echo "  - 停止后端服务 (端口8080)..."
    lsof -ti:8080 | xargs kill -9 2>/dev/null
    sleep 2
    echo -e "    ${GREEN}✓ 后端服务已停止${NC}" | tee -a "$LOG_FILE"
else
    echo -e "    ${YELLOW}⚠ 后端服务未运行${NC}" | tee -a "$LOG_FILE"
fi

# 停止前端服务（端口5175/5177）
for port in 5175 5177; do
    if lsof -ti:$port > /dev/null 2>&1; then
        echo "  - 停止前端服务 (端口$port)..."
        lsof -ti:$port | xargs kill -9 2>/dev/null
        sleep 1
    fi
done
echo -e "    ${GREEN}✓ 前端服务已停止${NC}" | tee -a "$LOG_FILE"

# ------------------------------------------------------------------------------
# 步骤2: 重新编译后端项目
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤2】重新编译后端项目...${NC}" | tee -a "$LOG_FILE"

cd "$PROJECT_DIR"
echo "  - 编译 membership-core 和 membership-internal..."

if mvn clean package -DskipTests -pl membership-core,membership-internal > "$LOG_FILE" 2>&1; then
    echo -e "    ${GREEN}✓ 编译成功${NC}" | tee -a "$LOG_FILE"
else
    echo -e "    ${RED}✗ 编译失败！查看日志: $LOG_FILE${NC}" | tee -a "$LOG_FILE"
    echo -e "\n${RED}编译错误详情:${NC}"
    tail -50 "$LOG_FILE"
    exit 1
fi

# ------------------------------------------------------------------------------
# 步骤3: 启动后端服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤3】启动后端服务...${NC}" | tee -a "$LOG_FILE"

cd "$BACKEND_DIR"

# 检查jar文件是否存在
JAR_FILE="target/membership-internal-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo -e "    ${RED}✗ JAR文件不存在: $JAR_FILE${NC}" | tee -a "$LOG_FILE"
    exit 1
fi

echo "  - 启动后端服务..."
nohup java -jar "$JAR_FILE" > "${PROJECT_DIR}/logs/backend.log" 2>&1 &

# 等待后端启动
echo "  - 等待后端服务启动..."
BACKEND_STARTED=false
for i in {1..30}; do
    if curl -s http://localhost:8080/auth/login > /dev/null 2>&1; then
        BACKEND_STARTED=true
        break
    fi
    sleep 1
    echo -n "."
done
echo ""

if $BACKEND_STARTED; then
    echo -e "    ${GREEN}✓ 后端服务启动成功 (http://localhost:8080)${NC}" | tee -a "$LOG_FILE"
else
    echo -e "    ${RED}✗ 后端服务启动超时！查看日志: ${PROJECT_DIR}/logs/backend.log${NC}" | tee -a "$LOG_FILE"
    exit 1
fi

# ------------------------------------------------------------------------------
# 步骤4: 启动前端服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤4】启动前端服务...${NC}" | tee -a "$LOG_FILE"

cd "$FRONTEND_DIR"

echo "  - 启动前端开发服务器..."
nohup npm run dev > "${PROJECT_DIR}/logs/frontend.log" 2>&1 &

# 等待前端启动
echo "  - 等待前端服务启动..."
FRONTEND_STARTED=false
FRONTEND_PORT=""
for i in {1..20}; do
    sleep 1
    if lsof -ti:5175 > /dev/null 2>&1; then
        FRONTEND_STARTED=true
        FRONTEND_PORT="5175"
        break
    elif lsof -ti:5177 > /dev/null 2>&1; then
        FRONTEND_STARTED=true
        FRONTEND_PORT="5177"
        break
    fi
    echo -n "."
done
echo ""

if $FRONTEND_STARTED; then
    echo -e "    ${GREEN}✓ 前端服务启动成功 (http://localhost:$FRONTEND_PORT)${NC}" | tee -a "$LOG_FILE"
else
    echo -e "    ${RED}✗ 前端服务启动超时！查看日志: ${PROJECT_DIR}/logs/frontend.log${NC}" | tee -a "$LOG_FILE"
fi

# ------------------------------------------------------------------------------
# 完成提示
# ------------------------------------------------------------------------------
echo -e "\n${GREEN}===============================================${NC}"
echo -e "${GREEN}              服务重启完成！${NC}"
echo -e "${GREEN}===============================================${NC}"
echo -e "\n${BLUE}服务地址:${NC}"
echo -e "  ├─ 后端API: http://localhost:8080"
echo -e "  └─ 前端页面: http://localhost:$FRONTEND_PORT"
echo -e "\n${BLUE}登录信息:${NC}"
echo -e "  ├─ 用户名: Vincent 或 admin"
echo -e "  └─ 密码: admin123"
echo -e "\n${YELLOW}日志文件:${NC}"
echo -e "  ├─ 重启日志: ${PROJECT_DIR}/logs/restart.log"
echo -e "  ├─ 后端日志: ${PROJECT_DIR}/logs/backend.log"
echo -e "  └─ 前端日志: ${PROJECT_DIR}/logs/frontend.log"
echo -e "\n${GREEN}请打开浏览器访问管理后台！${NC}"
echo -e "${GREEN}===============================================${NC}"
