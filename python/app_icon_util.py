#!/usr/bin/env python3
# -*- coding: utf-8 -*-

'''
App Icon Utilities - Advanced app icon retrieval methods

This module provides platform-specific app icon retrieval functionality
that may be complex to implement directly in Java/Kotlin.
'''

import os
import platform
import subprocess
import json
import sys
import base64
import io
from PIL import Image, ImageDraw
import tempfile

def get_app_icon(app_name):
    """Get application icon using platform-specific methods and return as base64"""
    system = platform.system()
    result = {"error": "Icon not found"}
    icon_data = None
    
    try:
        if system == "Windows":
            # For Windows, we try different methods
            try:
                import win32ui
                import win32gui
                import win32con
                import win32api
                import pywintypes
                
                # Check if the app name is an executable
                if app_name.lower().endswith('.exe'):
                    # Try different paths where the executable might be
                    program_files = os.environ.get('ProgramFiles', 'C:\\Program Files')
                    program_files_x86 = os.environ.get('ProgramFiles(x86)', 'C:\\Program Files (x86)')
                    
                    # Possible locations
                    paths = [
                        os.path.join(program_files, app_name),
                        os.path.join(program_files_x86, app_name),
                    ]
                    
                    # Extract folder name (without .exe)
                    app_folder = os.path.splitext(app_name)[0]
                    paths.extend([
                        os.path.join(program_files, app_folder, app_name),
                        os.path.join(program_files_x86, app_folder, app_name),
                    ])
                    
                    # Try to find the executable
                    for path in paths:
                        if os.path.isfile(path):
                            try:
                                # Get the large icon
                                ico_x = win32api.GetSystemMetrics(win32con.SM_CXICON)
                                ico_y = win32api.GetSystemMetrics(win32con.SM_CYICON)
                                
                                large, small = win32gui.ExtractIconEx(path, 0)
                                if large:
                                    # We got the icon handle
                                    icon_handle = large[0]
                                    
                                    # Create a DC and bitmap for the icon
                                    hdc = win32ui.CreateDCFromHandle(win32gui.GetDC(0))
                                    hbmp = win32ui.CreateBitmap()
                                    hbmp.CreateCompatibleBitmap(hdc, ico_x, ico_y)
                                    hdc = hdc.CreateCompatibleDC()
                                    
                                    # Draw the icon onto the bitmap
                                    hdc.SelectObject(hbmp)
                                    hdc.DrawIcon((0, 0), icon_handle)
                                    
                                    # Convert bitmap to Python Image
                                    bmpinfo = hbmp.GetInfo()
                                    bmpstr = hbmp.GetBitmapBits(True)
                                    img = Image.frombuffer(
                                        'RGBA',
                                        (bmpinfo['bmWidth'], bmpinfo['bmHeight']),
                                        bmpstr, 'raw', 'BGRA', 0, 1
                                    )
                                    
                                    # Clean up resources
                                    win32gui.DestroyIcon(icon_handle)
                                    hdc.DeleteDC()
                                    
                                    # Convert to base64
                                    buffered = io.BytesIO()
                                    img.save(buffered, format="PNG")
                                    icon_data = base64.b64encode(buffered.getvalue()).decode('utf-8')
                                    break
                            except Exception as e:
                                print(f"Error extracting icon from {path}: {str(e)}")
                
                # If we haven't found an icon yet, try using the default shell icon
                if not icon_data:
                    try:
                        import win32com.client
                        
                        shell = win32com.client.Dispatch("WScript.Shell")
                        # Create a temporary shortcut to get the icon
                        with tempfile.NamedTemporaryFile(suffix='.lnk', delete=False) as temp_file:
                            shortcut_path = temp_file.name
                        
                        shortcut = shell.CreateShortCut(shortcut_path)
                        if app_name.lower().endswith('.exe'):
                            # Set the target to the executable name
                            shortcut.TargetPath = app_name
                            shortcut.Save()
                            
                            # Extract icon from the shortcut
                            icon_path = win32gui.ExtractIcon(0, shortcut_path, 0)
                            if icon_path:
                                # Same process as before to convert icon to image
                                ico_x = win32api.GetSystemMetrics(win32con.SM_CXICON)
                                ico_y = win32api.GetSystemMetrics(win32con.SM_CYICON)
                                
                                hdc = win32ui.CreateDCFromHandle(win32gui.GetDC(0))
                                hbmp = win32ui.CreateBitmap()
                                hbmp.CreateCompatibleBitmap(hdc, ico_x, ico_y)
                                hdc = hdc.CreateCompatibleDC()
                                
                                hdc.SelectObject(hbmp)
                                hdc.DrawIcon((0, 0), icon_path)
                                
                                bmpinfo = hbmp.GetInfo()
                                bmpstr = hbmp.GetBitmapBits(True)
                                img = Image.frombuffer(
                                    'RGBA',
                                    (bmpinfo['bmWidth'], bmpinfo['bmHeight']),
                                    bmpstr, 'raw', 'BGRA', 0, 1
                                )
                                
                                win32gui.DestroyIcon(icon_path)
                                hdc.DeleteDC()
                                
                                buffered = io.BytesIO()
                                img.save(buffered, format="PNG")
                                icon_data = base64.b64encode(buffered.getvalue()).decode('utf-8')
                        
                        # Clean up
                        try:
                            os.unlink(shortcut_path)
                        except:
                            pass
                    except Exception as e:
                        print(f"Error getting icon from shortcut: {str(e)}")
            except ImportError:
                print("Missing required Windows libraries")
        
        elif system == "Darwin":  # macOS
            try:
                # Use AppleScript to get the app's icon
                script = f'''
                tell application "System Events"
                    try
                        set appPath to path of application file "{app_name}"
                        set appIcon to icon of application file appPath
                        -- Save icon to temporary file
                        set tempFolder to path to temporary items
                        set tempFile to (tempFolder as text) & "icon_temp.png"
                        -- Ensure the file doesn't exist
                        try
                            tell application "Finder"
                                delete file tempFile
                            end tell
                        end try
                        -- Save icon to file
                        save appIcon as «class PNGf» in tempFile
                        return tempFile
                    on error errorMsg
                        return "Error: " & errorMsg
                    end try
                end tell
                '''
                
                # Run the AppleScript
                proc = subprocess.run(["osascript", "-e", script], capture_output=True, text=True)
                output = proc.stdout.strip()
                
                if output and not output.startswith("Error:"):
                    # Read the icon file
                    with open(output, 'rb') as icon_file:
                        icon_bytes = icon_file.read()
                        icon_data = base64.b64encode(icon_bytes).decode('utf-8')
                    
                    # Clean up
                    try:
                        os.unlink(output)
                    except:
                        pass
            except Exception as e:
                print(f"Error getting macOS app icon: {str(e)}")
        
        elif system == "Linux":
            try:
                # Try to find the icon in the system icon theme
                icon_name = app_name.lower().replace('.exe', '')
                
                # Use the 'find' command to locate the icon in common icon locations
                icon_paths = subprocess.run([
                    "find", 
                    "/usr/share/icons", "/usr/share/pixmaps", 
                    "-name", f"{icon_name}.*", "-o", "-name", f"{icon_name.split('.')[0]}.*"
                ], capture_output=True, text=True).stdout.strip().split('\n')
                
                # Filter for image files
                valid_exts = ['.png', '.svg', '.xpm', '.jpg', '.jpeg', '.ico']
                icon_paths = [p for p in icon_paths if any(p.lower().endswith(ext) for ext in valid_exts)]
                
                if icon_paths and icon_paths[0]:
                    icon_path = icon_paths[0]
                    # Read the icon file
                    with open(icon_path, 'rb') as icon_file:
                        icon_bytes = icon_file.read()
                        icon_data = base64.b64encode(icon_bytes).decode('utf-8')
            except Exception as e:
                print(f"Error getting Linux app icon: {str(e)}")
        
        # If no icon was found, create a placeholder
        if not icon_data:
            # Create a simple colored box with app initials
            size = 64
            img = Image.new('RGBA', (size, size), (50, 150, 250, 255))
            draw = ImageDraw.Draw(img)
            
            # Draw a border
            draw.rectangle([(0, 0), (size-1, size-1)], outline=(255, 255, 255, 200), width=2)
            
            # Get initials (max 2 characters)
            app_basename = os.path.basename(app_name)
            app_name_without_ext = os.path.splitext(app_basename)[0]
            initials = app_name_without_ext[0:1].upper()
            if len(app_name_without_ext) > 1:
                # Get first letter and first letter after a space or underscore
                for i in range(1, len(app_name_without_ext)):
                    if app_name_without_ext[i-1] in [' ', '_', '-'] or app_name_without_ext[i-1].islower() and app_name_without_ext[i].isupper():
                        initials += app_name_without_ext[i:i+1].upper()
                        break
            
            # Center text (approximate)
            draw.text((size/2-10, size/2-12), initials, fill=(255, 255, 255, 255))
            
            # Convert to base64
            buffered = io.BytesIO()
            img.save(buffered, format="PNG")
            icon_data = base64.b64encode(buffered.getvalue()).decode('utf-8')
        
        # Return as a data URL
        return f"data:image/png;base64,{icon_data}"
    
    except Exception as e:
        return json.dumps({"error": str(e)})

# Main execution
if __name__ == "__main__":
    if len(sys.argv) > 2:
        command = sys.argv[1]
        if command == "get_app_icon":
            app_name = sys.argv[2]
            print(get_app_icon(app_name))
        else:
            print(json.dumps({"error": f"Unknown command: {command}"}))
    else:
        print(json.dumps({"error": "No command or app name specified"}))
