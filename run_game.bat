@echo off
chcp 65001 > nul
echo ========================================
echo   Super Mario Bros - Game Start
echo ========================================
echo.
echo Starting game...
echo.
java -cp ".;src" manager.GameEngine
pause

