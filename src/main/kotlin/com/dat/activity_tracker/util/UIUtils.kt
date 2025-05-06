package com.dat.activity_tracker.util

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import androidx.compose.ui.graphics.toComposeImageBitmap
import java.awt.Desktop
import java.awt.Image
import java.awt.Toolkit
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.util.Base64
import java.util.logging.Level
import java.util.logging.Logger
import javax.imageio.ImageIO
import javax.swing.ImageIcon
import javax.swing.filechooser.FileSystemView
import java.nio.file.Paths

/**
 * Format duration in seconds to HH:MM:SS format
 */
fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, remainingSeconds)
}

/**
 * Convert base64 data URL to ImageBitmap
 */
fun base64ToImageBitmap(dataUrl: String): ImageBitmap? {
    val logger = Logger.getLogger("activity_tracker.util")
    
    try {
        // Extract the base64 data from the data URL format
        if (dataUrl.startsWith("data:image")) {
            val base64Data = dataUrl.split(",").getOrNull(1)?.trim()
            if (!base64Data.isNullOrEmpty()) {
                // Decode base64 to byte array
                val imageBytes = Base64.getDecoder().decode(base64Data)
                val inputStream = ByteArrayInputStream(imageBytes)
                
                // Read the image
                val bufferedImage = ImageIO.read(inputStream)
                if (bufferedImage != null) {
                    return bufferedImage.toComposeImageBitmap()
                }
            }
        }
    } catch (e: Exception) {
        logger.warning("Failed to convert base64 data URL to ImageBitmap: ${e.message}")
    }
    
    return null
}

/**
 * Open a report file with the default system application
 */
fun openReportFile(path: String) {
    try {
        val file = File(path)
        if (file.exists()) {
            Desktop.getDesktop().open(file)
        }
    } catch (e: Exception) {
        LogManager.addLogEntry(Level.WARNING, "Failed to open report file: ${e.message}")
    }
}

/**
 * Get app icon for the given application name
 * This function attempts multiple methods to retrieve the icon:
 * 1. Using Python script for cross-platform support via PythonExecutor
 * 2. Using Java's FileSystemView for executables (Windows-specific)
 * 3. Using FileSystemView for file icons (fallback)
 * 4. Creating a simple placeholder icon with app initials
 */
fun getAppIcon(appName: String): ImageBitmap? {
    val logger = Logger.getLogger("activity_tracker.util")
    
    try {
        // Method 1: Primary method - Try using the Python executor to get icon
        try {
            val pythonExecutor = com.dat.activity_tracker.util.PythonExecutor()
            val iconBase64 = pythonExecutor.getAppIcon(appName)
            
            // Check if we got a valid data URL
            if (iconBase64 != null && iconBase64.startsWith("data:image")) {
                // Convert base64 to ImageBitmap
                return base64ToImageBitmap(iconBase64)
            }
        } catch (e: Exception) {
            logger.warning("Failed to get icon using Python executor: ${e.message}")
        }
        
        // Method 2: Try to get icon using Java's FileSystemView for executables
        if (appName.endsWith(".exe", ignoreCase = true)) {
            var icon: BufferedImage? = null
            
            // Check if the application exists in Program Files
            val programFiles = System.getenv("ProgramFiles")
            val programFilesX86 = System.getenv("ProgramFiles(x86)")
            
            // List of potential executable locations
            val potentialPaths = mutableListOf<String>()
            if (programFiles != null) {
                potentialPaths.add("$programFiles\\$appName")
            }
            if (programFilesX86 != null) {
                potentialPaths.add("$programFilesX86\\$appName")
            }
            
            // Extract app folder name if it's an .exe file
            val appFolder = appName.substring(0, appName.lastIndexOf("."))
            if (programFiles != null) {
                potentialPaths.add("$programFiles\\$appFolder\\$appName")
            }
            if (programFilesX86 != null) {
                potentialPaths.add("$programFilesX86\\$appFolder\\$appName")
            }
            
            // Try to find the exe file
            for (path in potentialPaths) {
                val file = File(path)
                if (file.exists() && file.isFile) {
                    // Get icon using FileSystemView
                    val fileSystemIcon = FileSystemView.getFileSystemView().getSystemIcon(file)
                    if (fileSystemIcon != null) {
                        // Convert Icon to BufferedImage
                        icon = BufferedImage(fileSystemIcon.iconWidth, fileSystemIcon.iconHeight, BufferedImage.TYPE_INT_ARGB)
                        val g = icon.createGraphics()
                        fileSystemIcon.paintIcon(null, g, 0, 0)
                        g.dispose()
                        break
                    }
                }
            }
            
            if (icon != null) {
                return icon.toComposeImageBitmap()
            }
        }
        
        // Method 3: Fallback to generic file icon
        if (appName.contains(".")) {
            try {
                var icon: BufferedImage? = null
                
                // Create a temporary file with the same extension
                val extension = appName.substring(appName.lastIndexOf("."))
                val tempFile = File.createTempFile("icon_temp", extension)
                tempFile.deleteOnExit()
                
                // Get icon for this file type
                val fileSystemIcon = FileSystemView.getFileSystemView().getSystemIcon(tempFile)
                if (fileSystemIcon != null) {
                    // Convert Icon to BufferedImage
                    icon = BufferedImage(fileSystemIcon.iconWidth, fileSystemIcon.iconHeight, BufferedImage.TYPE_INT_ARGB)
                    val g = icon.createGraphics()
                    fileSystemIcon.paintIcon(null, g, 0, 0)
                    g.dispose()
                }
                
                // Clean up
                tempFile.delete()
                
                if (icon != null) {
                    return icon.toComposeImageBitmap()
                }
            } catch (e: Exception) {
                logger.warning("Failed to get generic file icon: ${e.message}")
            }
        }
        
        // Method 4: Create a simple colored placeholder icon with initials
        try {
            val size = 64
            val icon = BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB)
            val g = icon.createGraphics()
            
            // Draw background (blue)
            g.color = java.awt.Color(50, 150, 250)
            g.fillRect(0, 0, size, size)
            
            // Draw border
            g.color = java.awt.Color(255, 255, 255, 200)
            g.drawRect(0, 0, size-1, size-1)
            
            // Get app initials (max 2 characters)
            val fileName = appName.split("[/\\\\]".toRegex()).last()
            val nameWithoutExt = if (fileName.endsWith(".exe", ignoreCase = true)) {
                fileName.substring(0, fileName.length - 4)
            } else {
                fileName
            }
            
            // Get initials
            var initials = nameWithoutExt.take(1).uppercase()
            for (i in 1 until nameWithoutExt.length) {
                if (nameWithoutExt[i-1] in listOf(' ', '_', '-') || 
                    (nameWithoutExt[i-1].isLowerCase() && nameWithoutExt[i].isUpperCase())) {
                    initials += nameWithoutExt[i].uppercase()
                    if (initials.length >= 2) break
                }
            }
            
            // Draw text
            g.color = java.awt.Color.WHITE
            g.font = java.awt.Font("Dialog", java.awt.Font.BOLD, 22)
            val metrics = g.fontMetrics
            val x = (size - metrics.stringWidth(initials)) / 2
            val y = ((size - metrics.height) / 2) + metrics.ascent
            g.drawString(initials, x, y)
            
            g.dispose()
            return icon.toComposeImageBitmap()
        } catch (e: Exception) {
            logger.warning("Failed to create placeholder icon: ${e.message}")
        }
    } catch (e: Exception) {
        logger.warning("Failed to get app icon: ${e.message}")
    }
    
    return null
}

/**
 * Utility function to format app names to be more user-friendly
 */
fun formatAppName(appName: String): String {
    // Extract only the file name from full path if present
    val fileName = appName.split("[/\\\\]".toRegex()).last()

    // Remove .exe extension if present
    val nameWithoutExt = if (fileName.endsWith(".exe", ignoreCase = true)) {
        fileName.substring(0, fileName.length - 4)
    } else {
        fileName
    }

    // Capitalize first letter of each word and replace underscores with spaces
    return nameWithoutExt.split("[_.\\s]+".toRegex())
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}
