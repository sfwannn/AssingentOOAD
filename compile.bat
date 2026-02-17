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

echo.
echo Compilation successful!
echo All classes compiled to the 'bin' directory.
echo.

pause
