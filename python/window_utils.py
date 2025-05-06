#!/usr/bin/env python3
# -*- coding: utf-8 -*-

'''
Window Utilities - Advanced window tracking methods

This module provides platform-specific window tracking functionality
that may be complex to implement directly in Java/Kotlin.
'''

import os
import platform
import subprocess
import json
import sys
import base64
import io
from PIL import Image

def get_active_window_info():
    """Get information about the active window using platform-specific methods"""
    system = platform.system()
    result = {"app_name": "", "window_title": "", "category": "Unknown", "icon": ""}
    
    try:
        if system == "Windows":
            # For Windows, we use Python libraries directly
            try:
                import win32gui
                import win32process
                import psutil
                
                # Get foreground window handle
                hwnd = win32gui.GetForegroundWindow()
                
                # Get window title
                window_title = win32gui.GetWindowText(hwnd)
                
                # Get process ID
                _, pid = win32process.GetWindowThreadProcessId(hwnd)
                
                # Get process name
                try:
                    process = psutil.Process(pid)
                    app_name = process.name()
                    
                    # Try to get icon data
                    if hasattr(process, 'exe'):
                        try:
                            from app_icon_util import get_app_icon
                            icon_data = get_app_icon(app_name)
                            if icon_data and not "error" in icon_data:
                                result["icon"] = icon_data
                        except Exception as e:
                            print(f"Error getting icon: {str(e)}")
                except (psutil.NoSuchProcess, psutil.AccessDenied):
                    app_name = "Unknown"
                    
                result["app_name"] = app_name
                result["window_title"] = window_title
                
            except ImportError:
                # Fallback if libraries aren't available
                result["app_name"] = "Python import error"
        
        elif system == "Darwin":  # macOS
            # AppleScript to get active application name and window title
            script = '''
            tell application "System Events"
                set frontApp to name of first application process whose frontmost is true
                set frontAppPath to path of first application process whose frontmost is true
                set windowTitle to ""
                
                tell process frontApp
                    if exists (1st window whose value of attribute "AXMain" is true) then
                        set windowTitle to name of 1st window whose value of attribute "AXMain" is true
                    end if
                end tell
                
                return frontApp & ":" & windowTitle & ":" & frontAppPath
            end tell
            '''
            
            proc = subprocess.run(["osascript", "-e", script], capture_output=True, text=True)
            if proc.stdout:
                parts = proc.stdout.strip().split(":")
                if len(parts) >= 3:
                    result["app_name"] = parts[0]
                    result["window_title"] = parts[1]
                    app_path = ":".join(parts[2:])  # Rejoin in case path contains colons
                    
                    # Try to get icon
                    try:
                        from app_icon_util import get_app_icon
                        icon_data = get_app_icon(result["app_name"])
                        if icon_data and not "error" in icon_data:
                            result["icon"] = icon_data
                    except Exception as e:
                        print(f"Error getting macOS icon: {str(e)}")
        
        elif system == "Linux":
            # Use xdotool to get active window info
            try:
                # Get window ID
                window_id = subprocess.check_output(["xdotool", "getactivewindow"]).decode().strip()
                
                # Get window name
                window_name = subprocess.check_output(
                    ["xdotool", "getwindowname", window_id]
                ).decode().strip()
                
                # Get window class (app name)
                window_class = subprocess.check_output(
                    ["xdotool", "getwindowclassname", window_id]
                ).decode().strip()
                
                result["app_name"] = window_class
                result["window_title"] = window_name
                
                # Try to get icon
                try:
                    from app_icon_util import get_app_icon
                    icon_data = get_app_icon(result["app_name"])
                    if icon_data and not "error" in icon_data:
                        result["icon"] = icon_data
                except Exception as e:
                    print(f"Error getting Linux icon: {str(e)}")
            except:
                pass
    except Exception as e:
        result["error"] = str(e)
    
    return json.dumps(result)

def get_running_applications():
    """Get a list of all running applications using platform-specific methods"""
    system = platform.system()
    result = {}
    
    try:
        if system == "Windows":
            try:
                import psutil
                
                for proc in psutil.process_iter(['pid', 'name', 'exe']):
                    try:
                        proc_info = proc.info()
                        app_name = proc_info['name']
                        
                        if app_name and app_name not in result:
                            # Try to get window title if available
                            window_title = ""
                            
                            # Try to get icon
                            icon_data = ""
                            try:
                                from app_icon_util import get_app_icon
                                icon_data = get_app_icon(app_name)
                            except Exception as e:
                                print(f"Error getting icon for {app_name}: {str(e)}")
                            
                            # Store results with icon data
                            result[app_name] = {
                                "title": window_title or app_name,
                                "icon": icon_data if icon_data and not "error" in icon_data else ""
                            }
                    except (psutil.NoSuchProcess, psutil.AccessDenied):
                        pass
            except ImportError:
                result["error"] = "Missing required Python libraries"
        
        elif system == "Darwin":  # macOS
            # AppleScript to get list of running applications
            script = '''
            tell application "System Events"
                set appList to ""
                repeat with theProcess in application processes
                    set appName to name of theProcess
                    set appPath to path of theProcess
                    set appList to appList & appName & ":" & appPath & ";"
                end repeat
                return appList
            end tell
            '''
            
            proc = subprocess.run(["osascript", "-e", script], capture_output=True, text=True)
            if proc.stdout:
                apps = proc.stdout.strip().split(";")
                for app in apps:
                    if app and ":" in app:
                        parts = app.split(":")
                        app_name = parts[0]
                        app_path = ":".join(parts[1:])  # Rejoin path in case it contains colons
                        
                        if app_name:
                            # Try to get icon
                            icon_data = ""
                            try:
                                from app_icon_util import get_app_icon
                                icon_data = get_app_icon(app_name)
                            except Exception as e:
                                print(f"Error getting icon for {app_name}: {str(e)}")
                            
                            result[app_name] = {
                                "title": app_name,
                                "icon": icon_data if icon_data and not "error" in icon_data else ""
                            }
        
        elif system == "Linux":
            # Use wmctrl to get list of windows
            try:
                output = subprocess.check_output(["wmctrl", "-l"]).decode()
                for line in output.splitlines():
                    parts = line.split(None, 3)
                    if len(parts) >= 4:
                        window_id = parts[0]
                        window_title = parts[3]
                        
                        # Get window class
                        try:
                            window_class = subprocess.check_output(
                                ["xprop", "-id", window_id, "WM_CLASS"]
                            ).decode().strip()
                            if "=" in window_class:
                                app_name = window_class.split("=")[1].strip().split(",")[0].strip('"')
                                
                                # Try to get icon
                                icon_data = ""
                                try:
                                    from app_icon_util import get_app_icon
                                    icon_data = get_app_icon(app_name)
                                except Exception as e:
                                    print(f"Error getting icon for {app_name}: {str(e)}")
                                
                                result[app_name] = {
                                    "title": window_title,
                                    "icon": icon_data if icon_data and not "error" in icon_data else ""
                                }
                        except:
                            pass
            except:
                pass
    except Exception as e:
        result["error"] = str(e)
    
    return json.dumps(result)

# Main execution for command line use
if __name__ == "__main__":
    if len(sys.argv) > 1:
        command = sys.argv[1]
        if command == "get_active_window_info":
            print(get_active_window_info())
        elif command == "get_running_applications":
            print(get_running_applications())
        else:
            print(json.dumps({"error": f"Unknown command: {command}"}))
    else:
        print(json.dumps({"error": "No command specified"}))
