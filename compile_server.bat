@echo off
chcp 65001 > nul
echo ========================================
echo   Super Mario Bros - Compile Server
echo ========================================
echo.
echo Compiling server...
echo.
javac -encoding UTF-8 -d . -sourcepath src src/network/server/GameServer.java
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   Server compilation successful!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   Server compilation failed!
    echo ========================================
)
echo.
pause
