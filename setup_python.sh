#!/bin/bash

echo "Setting up Python dependencies for Activity Tracker..."

# Check if Python is installed
if ! command -v python3 &> /dev/null; then
    echo "Python 3 not found! Please install Python 3.6 or higher."
    echo "Visit https://www.python.org/downloads/ to download Python."
    exit 1
fi

# Determine Python executable
PYTHON_CMD="python3"
if ! command -v python3 &> /dev/null; then
    if command -v python &> /dev/null; then
        PYTHON_CMD="python"
    else
        echo "Python not found! Please install Python 3.6 or higher."
        exit 1
    fi
fi

# Create Python directory if it doesn't exist
mkdir -p python

# Run dependency installer
$PYTHON_CMD python/install_dependencies.py

echo "Setup complete!"
