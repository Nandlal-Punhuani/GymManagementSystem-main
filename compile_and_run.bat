@echo off
echo Gym Management System - Compilation and Execution Script
echo ========================================================

REM Check if Java is installed
java -version >nul 2>&1
if %errorlevel% neq 0 (
    echo Error: Java is not installed or not in PATH
    echo Please install Java JDK 8 or higher
    pause
    exit /b 1
)

REM Check if MySQL Connector JAR exists
if not exist "mysql-connector-java-*.jar" (
    echo Warning: MySQL Connector JAR not found
    echo Please download mysql-connector-java-8.0.33.jar or similar
    echo and place it in this directory
)

echo Compiling Java files...
javac -cp ".;mysql-connector-java-*.jar" *.java

if %errorlevel% neq 0 (
    echo.
    echo Compilation failed! Please check the errors above.
    echo Make sure you have:
    echo 1. Java JDK installed
    echo 2. MySQL Connector JAR file
    pause
    exit /b 1
)

echo.
echo Compilation successful!
echo.
echo Starting Gym Management System...
echo.

java -cp ".;mysql-connector-java-*.jar" GymManagementSystem

if %errorlevel% neq 0 (
    echo.
    echo Application failed to start! Please check:
    echo 1. MySQL server is running
    echo 2. Database 'gym_management' exists
    echo 3. Database credentials are correct
    pause
)

echo.
echo Application closed.
pause