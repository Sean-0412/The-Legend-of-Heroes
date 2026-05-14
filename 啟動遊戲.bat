@echo off
REM Build and launch RPG game

cd /d "%~dp0"

if exist bin rmdir /s /q bin
mkdir bin

echo Compiling source files...
javac -encoding UTF-8 -d bin src\*.java

if errorlevel 1 (
    echo Build failed.
    pause
    exit /b 1
)

echo Build complete. Launching game...
java -cp bin src.RPGGame

if errorlevel 1 (
    echo Game exited with an error.
    pause
)
