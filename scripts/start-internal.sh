#!/bin/bash
# ========================================================
# 会员管理系统 - 内部管理后台启动脚本
# ========================================================

# 设置环境变量
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_301.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 项目根目录
PROJECT_DIR=$(cd "$(dirname "$0")/.." && pwd)

# 日志目录
LOG_DIR="$PROJECT_DIR/log"
mkdir -p "$LOG_DIR"

# JAR包路径
JAR_FILE="$PROJECT_DIR/membership-internal/target/membership-internal-1.0.0.jar"

# 检查JAR包是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "错误：JAR包不存在，请先编译项目"
    echo "执行命令: mvn clean package -DskipTests -pl membership-internal"
    exit 1
fi

# JVM参数
JAVA_OPTS="-Xms256m -Xmx512m -XX:MetaspaceSize=128m -XX:MaxMetaspaceSize=256m"
JAVA_OPTS="$JAVA_OPTS -Djava.security.egd=file:/dev/./urandom"
JAVA_OPTS="$JAVA_OPTS -Dlogging.file.path=$LOG_DIR"

# 启动命令
echo "========================================================"
echo "启动会员管理系统 - 内部管理后台"
echo "项目目录: $PROJECT_DIR"
echo "JAR包路径: $JAR_FILE"
echo "日志目录: $LOG_DIR"
echo "启动时间: $(date)"
echo "========================================================"

nohup java $JAVA_OPTS -jar "$JAR_FILE" > "$LOG_DIR/internal-startup.log" 2>&1 &

# 记录PID
echo $! > "$PROJECT_DIR/internal.pid"

echo "服务已启动，PID: $(cat "$PROJECT_DIR/internal.pid")"
echo "日志文件: $LOG_DIR/internal-backend.log"
echo "访问地址: http://localhost:8080"