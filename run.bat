@echo off
REM Parking Lot Management System - Run Script
REM This script compiles and runs the application

echo.
echo ====================================
echo Poke Mall Parking System
echo ====================================
echo.

REM Set classpath to include MySQL connector
set CLASSPATH=lib\*;bin

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

echo Compilation successful!
echo.

REM Run the application
echo Starting application...
java -cp "lib\*;bin" Main

pause
