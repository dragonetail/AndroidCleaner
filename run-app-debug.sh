#!/bin/bash

#export ANDROID_HOME=$HOME/Library/Android/sdk
#export PATH=$PATH:$ANDROID_HOME/platform-tools

# 确保gradlew有执行权限
chmod +x gradlew

# 清理并安装debug版本
# ./gradlew clean installDebug

# 如果安装成功，启动应用
if [ $? -eq 0 ]; then
    # 检查是否有设备连接
    if [ -x "$(command -v adb)" ]; then
        adb shell am start -n com.blackharry.androidcleaner.debug/com.blackharry.androidcleaner.MainActivity
    else
        echo "错误：未找到adb命令。请确保Android SDK platform-tools已安装并添加到PATH中。"
        exit 1
    fi
else
    echo "错误：应用安装失败"
    exit 1
fi 

# adb logcat
adb logcat | grep com.blackharry.androidcleaner.debug
