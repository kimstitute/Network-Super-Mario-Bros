@echo off
chcp 65001 > nul
echo ========================================
echo   Super Mario Bros - Server Mode
echo ========================================
echo.
echo Starting server on port 25565...
echo.
java -cp ".;src" network.server.GameServer
pause

