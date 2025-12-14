@echo off
chcp 65001 > nul
echo ========================================
echo   Super Mario Bros - Client Mode
echo ========================================
echo.
set /p SERVER_ADDRESS="Enter server address (default: localhost): "
if "%SERVER_ADDRESS%"=="" set SERVER_ADDRESS=localhost
echo.
echo Connecting to %SERVER_ADDRESS%:25565...
echo.
java -cp ".;src" network.client.GameClient %SERVER_ADDRESS% 25565
pause

