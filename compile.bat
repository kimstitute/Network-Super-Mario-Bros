@echo off
chcp 65001 > nul
echo ========================================
echo   Super Mario Bros - Compile
echo ========================================
echo.
echo Compiling project...
echo.
javac -encoding UTF-8 -d . -sourcepath src src/manager/GameEngine.java
if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo   Compilation successful!
    echo ========================================
) else (
    echo.
    echo ========================================
    echo   Compilation failed!
    echo ========================================
)
echo.
pause

