@echo off
chcp 65001 >nul
title NoobHubQueue 编译器
color 0A

echo.
echo ========================================
echo    NoobHubQueue 插件编译工具
echo ========================================
echo.

:: 检查Java
echo [1/4] 检查 Java 环境...
java -version >nul 2>&1
if errorlevel 1 (
    echo [错误] 未找到 Java！
    echo 请安装 Java 17 或更高版本
    pause
    exit /b 1
)
echo [√] Java 环境正常

:: 检查Maven
echo.
echo [2/4] 检查 Maven...
where mvn >nul 2>&1
if errorlevel 1 (
    echo [!] 未找到 Maven，使用内置 Maven Wrapper
    if not exist "mvnw.cmd" (
        echo [下载] Maven Wrapper...
        powershell -Command "Invoke-WebRequest -Uri 'https://repo.maven.apache.org/maven2/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile 'maven-wrapper.jar'"
    )
    set MVN_CMD=mvnw.cmd
) else (
    echo [√] Maven 已安装
    set MVN_CMD=mvn
)

:: 清理旧文件
echo.
echo [3/4] 清理旧文件...
if exist "target" (
    rmdir /s /q target
    echo [√] 已清理 target 目录
)

:: 编译
echo.
echo [4/4] 开始编译...
echo.
call %MVN_CMD% clean package -DskipTests

:: 检查结果
if errorlevel 1 (
    echo.
    echo ========================================
    echo [×] 编译失败！
    echo ========================================
    pause
    exit /b 1
)

:: 成功
echo.
echo ========================================
echo [√] 编译成功！
echo ========================================
echo.
echo 插件文件位置:
echo target\NoobHubQueue-1.0.0.jar
echo.
echo 请将此文件复制到服务器的 plugins 目录
echo.

:: 打开文件夹
echo 是否打开 target 文件夹? (Y/N)
set /p OPEN=">"
if /i "%OPEN%"=="Y" (
    explorer target
)

pause
