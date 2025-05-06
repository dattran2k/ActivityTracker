#!/usr/bin/env python3
# -*- coding: utf-8 -*-

'''
Install required Python dependencies for Activity Tracker
'''

import subprocess
import sys
import platform

def install_package(package):
    print(f"Installing {package}...")
    subprocess.check_call([sys.executable, "-m", "pip", "install", "--user", package])

# Common dependencies for all platforms
common_dependencies = [
    "pillow",  # For image processing
    "psutil",  # For process information
]

# Platform-specific dependencies
if platform.system() == "Windows":
    windows_dependencies = [
        "pywin32",     # For Windows API access
        "pypiwin32",   # Additional Windows-specific utilities
    ]
    for package in windows_dependencies:
        try:
            install_package(package)
        except Exception as e:
            print(f"Failed to install {package}: {e}")

elif platform.system() == "Darwin":  # macOS
    macos_dependencies = [
        "pyobjc",      # For macOS API access
    ]
    for package in macos_dependencies:
        try:
            install_package(package)
        except Exception as e:
            print(f"Failed to install {package}: {e}")

elif platform.system() == "Linux":
    linux_dependencies = [
        "python-xlib",  # For X Window System access
    ]
    for package in linux_dependencies:
        try:
            install_package(package)
        except Exception as e:
            print(f"Failed to install {package}: {e}")

# Install common dependencies
for package in common_dependencies:
    try:
        install_package(package)
    except Exception as e:
        print(f"Failed to install {package}: {e}")

print("Dependencies installation completed.")
