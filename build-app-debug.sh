#!/bin/bash

#export ANDROID_HOME=$HOME/Library/Android/sdk
#export PATH=$PATH:$ANDROID_HOME/platform-tools

# 确保gradlew有执行权限
chmod +x gradlew

# 清理并安装debug版本
./gradlew clean installDebug
