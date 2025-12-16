@echo off
chcp 65001 > nul
echo ========================================
echo   Super Mario Bros - Compile All
echo ========================================
echo.
echo Compiling entire project...
echo.
echo [1/2] Compiling server...
javac -encoding UTF-8 -d . -sourcepath src src/network/server/GameServer.java
if %ERRORLEVEL% NEQ 0 (
    echo Server compilation failed!
    pause
    exit /b 1
)
echo Server compilation successful!
echo.
echo [2/2] Compiling client...
javac -encoding UTF-8 -d . -sourcepath src src/manager/GameEngine.java
if %ERRORLEVEL% NEQ 0 (
    echo Client compilation failed!
    pause
    exit /b 1
)
echo Client compilation successful!
echo.
echo ========================================
echo   All compilation successful!
echo ========================================
echo.
pause
