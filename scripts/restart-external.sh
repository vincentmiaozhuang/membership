#!/bin/bash

# ==============================================================================
# 会员卡管理系统 - 外部接口一键重启脚本
# 功能：停止服务 → 重新编译 → 启动外部接口服务
# 使用方式：./restart-external.sh
# ==============================================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 项目路径
PROJECT_DIR="/Users/vincentmiao/Documents/trae_projects/members/membership-system"
BACKEND_DIR="${PROJECT_DIR}/membership-external"

# 日志文件
LOG_FILE="${PROJECT_DIR}/logs/restart-external.log"

# 确保日志目录存在
mkdir -p "${PROJECT_DIR}/logs"

echo -e "\n${BLUE}===============================================${NC}"
echo -e "${BLUE}       会员卡管理系统 - 外部接口重启脚本${NC}"
echo -e "${BLUE}===============================================${NC}"
echo -e "$(date '+%Y-%m-%d %H:%M:%S')" | tee -a "$LOG_FILE"

# ------------------------------------------------------------------------------
# 步骤1: 停止现有服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤1】停止现有服务...${NC}" | tee -a "$LOG_FILE"

# 停止外部接口服务（端口8081）
if lsof -ti:8081 > /dev/null 2>&1; then
    echo "  - 停止外部接口服务 (端口8081)..."
    lsof -ti:8081 | xargs kill -9 2>/dev/null
    sleep 2
    echo -e "    ${GREEN}✓ 外部接口服务已停止${NC}" | tee -a "$LOG_FILE"
else
    echo -e "    ${YELLOW}⚠ 外部接口服务未运行${NC}" | tee -a "$LOG_FILE"
fi

# ------------------------------------------------------------------------------
# 步骤2: 重新编译后端项目
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤2】重新编译后端项目...${NC}" | tee -a "$LOG_FILE"

cd "$PROJECT_DIR"
echo "  - 编译 membership-core 和 membership-external..."

if mvn clean package -DskipTests -pl membership-core,membership-external > "$LOG_FILE" 2>&1; then
    echo -e "    ${GREEN}✓ 编译成功${NC}" | tee -a "$LOG_FILE"
else
    echo -e "    ${RED}✗ 编译失败！查看日志: $LOG_FILE${NC}" | tee -a "$LOG_FILE"
    echo -e "\n${RED}编译错误详情:${NC}"
    tail -50 "$LOG_FILE"
    exit 1
fi

# ------------------------------------------------------------------------------
# 步骤3: 启动外部接口服务
# ------------------------------------------------------------------------------
echo -e "\n${YELLOW}【步骤3】启动外部接口服务...${NC}" | tee -a "$LOG_FILE"

cd "$BACKEND_DIR"

# 检查jar文件是否存在
JAR_FILE="target/membership-external-1.0.0.jar"
if [ ! -f "$JAR_FILE" ]; then
    echo -e "    ${RED}✗ JAR文件不存在: $JAR_FILE${NC}" | tee -a "$LOG_FILE"
    exit 1
fi

echo "  - 启动外部接口服务..."
nohup java -jar "$JAR_FILE" > "${PROJECT_DIR}/logs/external-backend.log" 2>&1 &

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

if $SERVICE_STARTED; then
    echo -e "    ${GREEN}✓ 外部接口服务启动成功 (http://localhost:8081)${NC}" | tee -a "$LOG_FILE"
else
    echo -e "    ${RED}✗ 服务启动超时！查看日志: ${PROJECT_DIR}/logs/external-backend.log${NC}" | tee -a "$LOG_FILE"
    exit 1
fi

# ------------------------------------------------------------------------------
# 完成提示
# ------------------------------------------------------------------------------
echo -e "\n${GREEN}===============================================${NC}"
echo -e "${GREEN}              服务启动完成！${NC}"
echo -e "${GREEN}===============================================${NC}"
echo -e "\n${BLUE}服务地址:${NC}"
echo -e "  └─ 外部接口: http://localhost:8081"
echo -e "\n${BLUE}可用接口:${NC}"
echo -e "  ├─ GET  /api/external/v1/customers          # 查询客户列表"
echo -e "  └─ GET  /api/external/v1/customers/{id}     # 查询单个客户"
echo -e "\n${YELLOW}日志文件:${NC}"
echo -e "  ├─ 重启日志: ${PROJECT_DIR}/logs/restart-external.log"
echo -e "  └─ 服务日志: ${PROJECT_DIR}/logs/external-backend.log"
echo -e "\n${GREEN}外部接口服务已就绪！${NC}"
echo -e "${GREEN}===============================================${NC}"
