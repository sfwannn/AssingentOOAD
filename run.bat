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
if not exist bin mkdir bin
set "SOURCES_FILE=%TEMP%\ooad_sources.txt"
(
    for /r "%CD%\src" %%f in (*.java) do @echo %%f
    for /r "%CD%\ui" %%f in (*.java) do @echo %%f
) > "%SOURCES_FILE%"

javac -d bin -cp "lib\*" @"%SOURCES_FILE%" 2>&1
del "%SOURCES_FILE%" >nul 2>&1

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
