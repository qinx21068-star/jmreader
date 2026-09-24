#!/data/data/com.termux/files/usr/bin/bash

# JM Reader Termux 构建脚本
# 在 Android 设备上使用 Termux 构建 APK

set -e

echo "================================"
echo "  JM Reader Termux 构建脚本"
echo "================================"
echo ""

# 检查 Termux 环境
if [ ! -d "/data/data/com.termux" ]; then
    echo "❌ 错误：请在 Termux 中运行此脚本"
    exit 1
fi

echo "✅ Termux 环境检测通过"
echo ""

# 步骤 1：更新包管理器
echo "📦 步骤 1/5: 更新包管理器..."
pkg update -y

# 步骤 2：安装必要工具
echo "📦 步骤 2/5: 安装构建工具..."
pkg install -y openjdk-17 git wget

# 步骤 3：设置环境变量
echo "⚙️  步骤 3/5: 配置环境变量..."
export JAVA_HOME=$PREFIX/opt/openjdk
export PATH=$JAVA_HOME/bin:$PATH
export ANDROID_HOME=$HOME/.android-sdk
export GRADLE_OPTS="-Xmx2048m -XX:MaxMetaspaceSize=512m -XX:+HeapDumpOnOutOfMemoryError"

echo "Java 版本:"
java -version

# 步骤 4：清理旧构建
echo "🧹 步骤 4/5: 清理旧构建..."
if [ -d "app/build" ]; then
    rm -rf app/build
fi
if [ -d ".gradle" ]; then
    rm -rf .gradle
fi

# 步骤 5：开始构建
echo "🔨 步骤 5/5: 开始构建 APK..."
echo "⚠️  警告：构建可能需要 10-30 分钟，请保持屏幕常亮"
echo ""

chmod +x gradlew

# 使用 --no-daemon 避免后台进程占用内存
./gradlew assembleDebug \
    --no-daemon \
    --no-parallel \
    --max-workers=1 \
    --stacktrace

# 检查构建结果
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo ""
    echo "================================"
    echo "  ✅ 构建成功！"
    echo "================================"
    echo ""
    echo "APK 位置:"
    echo "  app/build/outputs/apk/debug/app-debug.apk"
    echo ""
    APK_SIZE=$(du -h app/build/outputs/apk/debug/app-debug.apk | cut -f1)
    echo "APK 大小: $APK_SIZE"
    echo ""
    echo "安装命令:"
    echo "  termux-open app/build/outputs/apk/debug/app-debug.apk"
    echo ""
else
    echo ""
    echo "❌ 构建失败，请检查错误信息"
    exit 1
fi
