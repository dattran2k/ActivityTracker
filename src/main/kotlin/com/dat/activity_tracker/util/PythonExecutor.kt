package com.dat.activity_tracker.util

import java.io.File
import java.util.logging.Level
import java.util.logging.Logger
import org.json.JSONObject
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Executor for Python scripts - calls Python interpreter directly
 */
class PythonExecutor {
    private val logger = Logger.getLogger("activity_tracker.python")
    private val pythonScriptsDir = File("python")
    private val pythonExecutable = findPythonExecutable()
    
    init {
        // Check if Python scripts directory exists
        if (!pythonScriptsDir.exists() || !File(pythonScriptsDir, "window_utils.py").exists()) {
            logger.warning("Python scripts not found. Some functionality may be limited.")
        }
        
        logger.info("Python executor initialized with Python executable: $pythonExecutable")
        
        // Try to install dependencies
        try {
            if (File(pythonScriptsDir, "install_dependencies.py").exists()) {
                installDependencies()
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to install Python dependencies: ${e.message}", e)
        }
    }
    
    /**
     * Find Python executable on the system
     */
    private fun findPythonExecutable(): String {
        val isWindows = System.getProperty("os.name").lowercase().contains("windows")
        val possibleCommands = listOf("python3", "python", "py")
        
        for (cmd in possibleCommands) {
            try {
                val processBuilder = if (isWindows) {
                    ProcessBuilder("where", cmd)
                } else {
                    ProcessBuilder("which", cmd)
                }
                
                val process = processBuilder.start()
                val exitCode = process.waitFor()
                
                if (exitCode == 0) {
                    val result = process.inputStream.bufferedReader().readLine()
                    if (!result.isNullOrBlank()) {
                        logger.info("Found Python executable: $result")
                        return cmd
                    }
                }
            } catch (e: Exception) {
                logger.warning("Error checking for Python executable $cmd: ${e.message}")
            }
        }
        
        // Default to "python" if we couldn't find a specific one
        return "python"
    }
    
    /**
     * Install Python dependencies
     */
    private fun installDependencies() {
        try {
            val processBuilder = ProcessBuilder(
                pythonExecutable,
                Paths.get(pythonScriptsDir.absolutePath, "install_dependencies.py").toString()
            )
            processBuilder.directory(pythonScriptsDir)
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            if (exitCode == 0) {
                logger.info("Python dependencies installed successfully")
            } else {
                logger.warning("Failed to install Python dependencies: $output")
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Error installing Python dependencies: ${e.message}", e)
        }
    }
    
    /**
     * Execute a Python function in a script
     */
    fun executePythonFunction(scriptName: String, functionName: String, vararg args: String): String {
        val scriptPath = Paths.get(pythonScriptsDir.absolutePath, "$scriptName.py").toString()
        
        if (!Files.exists(Paths.get(scriptPath))) {
            logger.severe("Python script not found: $scriptPath")
            return "{\"error\": \"Script not found: $scriptName.py\"}"
        }
        
        try {
            val commandList = mutableListOf(pythonExecutable, scriptPath, functionName)
            commandList.addAll(args)
            
            val processBuilder = ProcessBuilder(commandList)
            processBuilder.redirectErrorStream(true)
            
            val process = processBuilder.start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            
            if (exitCode != 0) {
                logger.warning("Python script returned non-zero exit code: $exitCode")
                return "{\"error\": \"Execution failed with exit code $exitCode\", \"output\": \"$output\"}"
            }
            
            return output.trim()
        } catch (e: Exception) {
            logger.log(Level.SEVERE, "Error executing Python function: ${e.message}", e)
            return "{\"error\": \"${e.message}\"}"
        }
    }
    
    /**
     * Get active window information using Python
     */
    fun getActiveWindowInfo(): Triple<String, String, String> {
        try {
            val result = executePythonFunction("window_utils", "get_active_window_info")
            
            try {
                val jsonObject = JSONObject(result)
                
                val appName = jsonObject.optString("app_name", "")
                val windowTitle = jsonObject.optString("window_title", "")
                val category = jsonObject.optString("category", "Unknown")
                
                return Triple(appName, windowTitle, category)
            } catch (e: Exception) {
                logger.warning("Failed to parse active window info: $result")
                return Triple("", "", "Unknown")
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to get active window info: ${e.message}", e)
            return Triple("", "", "Unknown")
        }
    }
    
    /**
     * Get active window information including app icon
     */
    fun getActiveWindowInfoWithIcon(): Pair<Triple<String, String, String>, String> {
        try {
            val result = executePythonFunction("window_utils", "get_active_window_info")
            
            try {
                val jsonObject = JSONObject(result)
                
                val appName = jsonObject.optString("app_name", "")
                val windowTitle = jsonObject.optString("window_title", "")
                val category = jsonObject.optString("category", "Unknown")
                val iconData = jsonObject.optString("icon", "")
                
                return Pair(Triple(appName, windowTitle, category), iconData)
            } catch (e: Exception) {
                logger.warning("Failed to parse active window info with icon: $result")
                return Pair(Triple("", "", "Unknown"), "")
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to get active window info with icon: ${e.message}", e)
            return Pair(Triple("", "", "Unknown"), "")
        }
    }
    
    /**
     * Get running applications using Python
     */
    fun getRunningApplications(): Map<String, String> {
        try {
            val result = executePythonFunction("window_utils", "get_running_applications")
            
            try {
                val jsonObject = JSONObject(result)
                
                val runningApps = mutableMapOf<String, String>()
                for (key in jsonObject.keys()) {
                    if (key != "error") {
                        // Check if it's the new format (with icon) or old format
                        if (jsonObject.get(key) is JSONObject) {
                            val appInfo = jsonObject.getJSONObject(key)
                            runningApps[key] = appInfo.getString("title")
                        } else {
                            runningApps[key] = jsonObject.getString(key)
                        }
                    }
                }
                
                return runningApps
            } catch (e: Exception) {
                logger.warning("Failed to parse running applications: $result")
                return emptyMap()
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to get running applications: ${e.message}", e)
            return emptyMap()
        }
    }
    
    /**
     * Get running applications with their icons
     */
    fun getRunningApplicationsWithIcons(): Map<String, Pair<String, String>> {
        try {
            val result = executePythonFunction("window_utils", "get_running_applications")
            
            try {
                val jsonObject = JSONObject(result)
                
                val runningApps = mutableMapOf<String, Pair<String, String>>()
                for (key in jsonObject.keys()) {
                    if (key != "error") {
                        // Check if it's the new format (with icon) or old format
                        if (jsonObject.get(key) is JSONObject) {
                            val appInfo = jsonObject.getJSONObject(key)
                            val title = appInfo.getString("title")
                            val icon = appInfo.optString("icon", "")
                            runningApps[key] = Pair(title, icon)
                        } else {
                            // Old format - no icon data
                            runningApps[key] = Pair(jsonObject.getString(key), "")
                        }
                    }
                }
                
                return runningApps
            } catch (e: Exception) {
                logger.warning("Failed to parse running applications with icons: $result")
                return emptyMap()
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to get running applications with icons: ${e.message}", e)
            return emptyMap()
        }
    }
    
    /**
     * Get app icon using Python
     */
    fun getAppIcon(appName: String): String? {
        try {
            val result = executePythonFunction("app_icon_util", "get_app_icon", appName)
            
            // Check if we got a data URL (not an error)
            if (result.startsWith("data:image")) {
                return result
            } else {
                try {
                    val jsonObject = JSONObject(result)
                    if (jsonObject.has("error")) {
                        logger.warning("Error getting app icon for $appName: ${jsonObject.getString("error")}")
                    }
                } catch (e: Exception) {
                    // Not JSON, just log the raw result
                    logger.warning("Failed to get app icon for $appName: $result")
                }
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Exception getting app icon for $appName: ${e.message}", e)
        }
        
        return null
    }
}
