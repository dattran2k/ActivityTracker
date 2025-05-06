@echo off
echo Setting up Python dependencies for Activity Tracker...

rem Check if Python is installed
python --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo Python not found! Please install Python 3.6 or higher.
    echo Visit https://www.python.org/downloads/ to download Python.
    pause
    exit /b 1
)

rem Create Python directory if it doesn't exist
if not exist python mkdir python

rem Run dependency installer
python python\install_dependencies.py

echo Setup complete!
pause
