package com.dat.activity_tracker.monitor

import com.dat.activity_tracker.data.AppCategorizer
import com.dat.activity_tracker.domain.repository.ActivityRepository
import com.dat.activity_tracker.domain.repository.CategoryRepository
import com.dat.activity_tracker.util.PythonExecutor
import com.sun.jna.Native
import com.sun.jna.Pointer
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef
import com.sun.jna.platform.win32.WinDef.HWND
import com.sun.jna.platform.win32.WinUser
import com.sun.jna.win32.StdCallLibrary
import com.sun.jna.ptr.IntByReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.logging.Logger

/**
 * Monitor computer activity
 */
class ActivityMonitor(
    private val activityRepository: ActivityRepository,
    private val categoryRepository: CategoryRepository,
    private val interval: Long = 1000 // Check every second
) {
    private val logger = Logger.getLogger("activity_tracker.monitor")
    var currentApp: String = ""
    var currentWindow: String = ""
    var appStartTime: LocalDateTime? = null
    private val osSystem = System.getProperty("os.name").lowercase()
    private val backgroundApps = mutableMapOf<String, LocalDateTime>()
    val appCategorizer = AppCategorizer()
    
    // Python executor for cross-platform support
    private val pythonExecutor = PythonExecutor()
    
    // Monitoring state
    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()
    
    // Background apps state flow
    private val _backgroundAppsList = MutableStateFlow<List<String>>(emptyList())
    val backgroundAppsList: StateFlow<List<String>> = _backgroundAppsList.asStateFlow()
    
    // JNA interfaces for native Windows API access (fallback if Python doesn't work)
    interface User32Ext : StdCallLibrary {
        companion object {
            val INSTANCE = Native.load("user32", User32Ext::class.java) as User32Ext
        }
        
        fun EnumWindows(lpEnumFunc: WinUser.WNDENUMPROC, userData: Pointer?): Boolean
        fun GetWindowThreadProcessId(hWnd: HWND, lpdwProcessId: IntByReference): Int
        fun GetForegroundWindow(): HWND
        fun GetWindowTextA(hWnd: HWND, lpString: ByteArray, nMaxCount: Int): Int
    }
    
    /**
     * Get a list of all running applications
     */
    fun getRunningApplications(): Map<String, String> {
        // First try using Python (works on all platforms)
        val pythonResult = pythonExecutor.getRunningApplications()
        if (pythonResult.isNotEmpty()) {
            return pythonResult
        }
        
        // Fallback to JNA for Windows
        val runningApps = mutableMapOf<String, String>()
        
        try {
            if (osSystem.contains("windows")) {
                val user32 = User32Ext.INSTANCE
                
                // Create a callback function for EnumWindows
                val callback = WinUser.WNDENUMPROC { hwnd, data ->
                    val processId = IntByReference()
                    user32.GetWindowThreadProcessId(hwnd, processId)
                    
                    // Get window title
                    val buffer = ByteArray(1024)
                    user32.GetWindowTextA(hwnd, buffer, buffer.size)
                    val windowTitle = Native.toString(buffer).trim()
                    
                    // Get process name using ProcessHandle
                    if (windowTitle.isNotEmpty() && processId.value > 0) {
                        val processHandle = ProcessHandle.of(processId.value.toLong())
                        if (processHandle.isPresent) {
                            val process = processHandle.get()
                            val appName = process.info().command().orElse("Unknown")
                                .split("\\\\").last()
                            
                            // Skip system processes
                            if (!appName.isNullOrEmpty() && !appCategorizer.isSystemApp(appName)) {
                                runningApps[appName] = windowTitle
                            }
                        }
                    }
                    
                    true // Continue enumeration
                }
                
                // Enumerate all windows
                user32.EnumWindows(callback, null)
            } else {
                logger.warning("Native Windows API not available")
            }
        } catch (e: Exception) {
            logger.severe("Error getting list of running applications: ${e.message}")
        }
        
        return runningApps
    }
    
    /**
     * Get information about the active window
     */
    fun getActiveWindowInfo(): Triple<String, String, String> {
        // First try using Python (works on all platforms)
        val pythonResult = pythonExecutor.getActiveWindowInfo()
        if (pythonResult.first.isNotBlank() || pythonResult.second.isNotBlank()) {
            val category = appCategorizer.getCategory(pythonResult.first)
            return Triple(pythonResult.first, pythonResult.second, category)
        }
        
        // Fallback to JNA for Windows
        var appName = ""
        var windowTitle = ""
        
        try {
            if (osSystem.contains("windows")) {
                val user32 = User32Ext.INSTANCE
                
                // Get handle of active window
                val hwnd = user32.GetForegroundWindow()
                
                // Get window title
                val buffer = ByteArray(1024)
                user32.GetWindowTextA(hwnd, buffer, buffer.size)
                windowTitle = Native.toString(buffer).trim()
                
                // Get process ID
                val processId = IntByReference()
                user32.GetWindowThreadProcessId(hwnd, processId)
                
                // Get process name
                if (processId.value > 0) {
                    val processHandle = ProcessHandle.of(processId.value.toLong())
                    if (processHandle.isPresent) {
                        val process = processHandle.get()
                        appName = process.info().command().orElse("Unknown")
                            .split("\\\\").last()
                    }
                }
            } else {
                logger.warning("Native Windows API not available")
            }
        } catch (e: Exception) {
            logger.severe("Error getting active window information: ${e.message}")
        }
        
        // Get app category
        val category = appCategorizer.getCategory(appName)
        
        return Triple(appName, windowTitle, category)
    }
    
    /**
     * Monitor activity in a separate coroutine
     */
    suspend fun startMonitoring(onActivityChange: (String, String, String) -> Unit) = withContext(Dispatchers.IO) {
        // Set monitoring state
        _isMonitoring.value = true
        
        // Log startup event
        activityRepository.logSystemEvent("startup")
        logger.info("Activity Tracker started")
        
        try {
            logger.info("Starting computer activity monitoring...")
            
            // Variables for tracking
            var lastCheckTime = LocalDateTime.now()
            val backgroundCheckInterval = 5 // Check background apps every 5 seconds
            
            while (_isMonitoring.value) {
                // 1. Monitor active window
                val (appName, windowTitle, appCategory) = getActiveWindowInfo()
                
                // If the application or window has changed
                if ((appName != currentApp || windowTitle != currentWindow) && appName.isNotBlank()) {
                    val now = LocalDateTime.now()
                    
                    // If there was a previous app running, save usage information
                    if (currentApp.isNotBlank() && appStartTime != null) {
                        val duration = java.time.Duration.between(appStartTime, now).seconds.toInt()
                        if (duration > 0) {
                            // Get the category of the previous app
                            val prevAppCategory = appCategorizer.getCategory(currentApp)
                            // Save the category to the database
                            categoryRepository.setAppCategory(currentApp, prevAppCategory)
                            activityRepository.logActivity(
                                currentApp,
                                currentWindow,
                                appStartTime!!,
                                now,
                                duration,
                                prevAppCategory
                            )
                            logger.fine("Used $currentApp ($currentWindow) for $duration seconds")
                        }
                    }
                    
                    // Update current application
                    currentApp = appName
                    currentWindow = windowTitle
                    appStartTime = now
                    
                    // Store newly categorized app in the database for future use
                    if (appCategory != "Unknown") {
                        categoryRepository.setAppCategory(appName, appCategory)
                    }
                    
                    logger.info("Switched to: $currentApp - $currentWindow - $appCategory")
                    
                    // Update UI
                    onActivityChange(currentApp, currentWindow, appCategory)
                }
                
                // 2. Monitor background applications
                val now = LocalDateTime.now()
                if (java.time.Duration.between(lastCheckTime, now).seconds >= backgroundCheckInterval) {
                    // Get list of running applications
                    val runningApps = getRunningApplications()
                    
                    // Check for new applications
                    for ((appName, windowTitle) in runningApps) {
                        // Skip current application (already tracked above)
                        if (appName == currentApp) {
                            continue
                        }
                        
                        // If the new app is not in the background tracking list
                        if (appName !in backgroundApps) {
                            backgroundApps[appName] = now
                            // Get app category
                            val category = appCategorizer.getCategory(appName)
                            // Store newly categorized app in the database for future use
                            if (category != "Unknown") {
                                categoryRepository.setAppCategory(appName, category)
                            }
                            logger.info("Detected background app: $appName - $category")
                            
                            // Update background apps state flow
                            _backgroundAppsList.value = backgroundApps.keys.toList()
                        }
                    }
                    
                    // Check for closed applications
                    val closedApps = mutableListOf<String>()
                    for (appName in backgroundApps.keys) {
                        if (appName !in runningApps && appName != currentApp) {
                            // App has stopped, log usage time
                            val startTime = backgroundApps[appName]!!
                            val duration = java.time.Duration.between(startTime, now).seconds.toInt()
                            if (duration > 0) {
                                // Store newly categorized app in the database for future use
                                val category = appCategorizer.getCategory(appName)
                                categoryRepository.setAppCategory(appName, category)
                                activityRepository.logActivity(
                                    appName,
                                    "Background: $appName",
                                    startTime,
                                    now,
                                    duration,
                                    category
                                )
                                logger.info("Background app $appName closed, usage time: $duration seconds - $category")
                                closedApps.add(appName)
                            }
                        }
                    }
                    
                    // Remove closed apps from tracking list
                    closedApps.forEach { backgroundApps.remove(it) }
                    
                    // Update background apps state flow
                    _backgroundAppsList.value = backgroundApps.keys.toList()
                    
                    // Update last check time
                    lastCheckTime = now
                }
                
                // Pause according to configured interval
                delay(interval)
            }
            
        } catch (e: Exception) {
            logger.severe("Error during monitoring: ${e.message}")
        } finally {
            // Save last activity before stopping
            val now = LocalDateTime.now()
            
            // Log last activity of the active window
            if (currentApp.isNotBlank() && appStartTime != null) {
                val duration = java.time.Duration.between(appStartTime, now).seconds.toInt()
                if (duration > 0) {
                    // Get app category for final log
                    val appCategory = appCategorizer.getCategory(currentApp)
                    activityRepository.logActivity(
                        currentApp,
                        currentWindow,
                        appStartTime!!,
                        now,
                        duration,
                        appCategory
                    )
                }
            }
            
            // Log background apps before shutdown
            for ((appName, startTime) in backgroundApps) {
                val duration = java.time.Duration.between(startTime, now).seconds.toInt()
                if (duration > 0) {
                    // Get app category for the background app
                    val appCategory = appCategorizer.getCategory(appName)
                    activityRepository.logActivity(
                        appName,
                        "Background: $appName",
                        startTime,
                        now,
                        duration,
                        appCategory
                    )
                }
            }
            
            // Log shutdown event
            activityRepository.logSystemEvent("shutdown")
            logger.info("Stopped activity monitoring")
            
            // Reset state
            _isMonitoring.value = false
            _backgroundAppsList.value = emptyList()
        }
    }
    
    /**
     * Stop monitoring
     */
    fun stopMonitoring() {
        if (_isMonitoring.value) {
            _isMonitoring.value = false
            logger.info("Stopping monitoring...")
        }
    }
}
