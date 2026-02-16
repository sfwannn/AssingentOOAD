@echo off
REM Parking Lot Management System - Compile Script
REM This script compiles the Java files

echo.
echo ====================================
echo Poke Mall Parking System
echo Compilation Only
echo ====================================
echo.

REM Compile all Java files
echo Compiling Java files...
javac -d bin -cp "lib\*" ^
    "src\*.java" ^
    "src\Database\*.java" ^
    "src\Management\*.java" ^
    "src\ParkingLot\*.java" ^
    "src\Vehicles\*.java" ^
    "ui\*.java" 2>&1

if errorlevel 1 (
    echo.
    echo ERROR: Compilation failed!
    echo.
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo All classes compiled to the 'bin' directory.
echo.

pause
