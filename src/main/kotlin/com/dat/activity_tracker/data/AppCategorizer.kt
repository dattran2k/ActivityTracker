package com.dat.activity_tracker.data

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Logger
import org.json.JSONObject

/**
 * Categorizes applications into different groups
 */
class AppCategorizer(private val configPath: String = "data/app_categories.json") {
    private val logger = Logger.getLogger("activity_tracker.categorizer")
    private var categories: MutableMap<String, String> = mutableMapOf()
    
    // Application category constants
    companion object {
        const val SYSTEM = "System"
        const val BROWSER = "Browser"
        const val PRODUCTIVITY = "Productivity"
        const val DEVELOPMENT = "Development"
        const val ENTERTAINMENT = "Entertainment"
        const val COMMUNICATION = "Communication"
        const val UTILITY = "Utility"
        const val UNKNOWN = "Unknown"
    }
    
    init {
        loadCategories()
        
        // Initialize default categories if not loaded
        if (categories.isEmpty()) {
            initializeDefaultCategories()
        }
    }
    
    /**
     * Load application categories from configuration file
     */
    private fun loadCategories() {
        try {
            val configFile = File(configPath)
            if (configFile.exists()) {
                val jsonContent = Files.readString(Paths.get(configPath))
                val jsonObject = JSONObject(jsonContent)
                
                for (key in jsonObject.keys()) {
                    categories[key] = jsonObject.getString(key)
                }
                
                logger.info("Loaded ${categories.size} application categories")
            } else {
                logger.info("No category configuration found, using defaults")
            }
        } catch (e: Exception) {
            logger.severe("Error loading application categories: ${e.message}")
            categories = mutableMapOf()
        }
    }
    
    /**
     * Save application categories to configuration file
     */
    private fun saveCategories() {
        try {
            val configFile = File(configPath)
            configFile.parentFile.mkdirs()
            
            val jsonObject = JSONObject()
            categories.forEach { (key, value) ->
                jsonObject.put(key, value)
            }
            
            Files.writeString(Paths.get(configPath), jsonObject.toString(4))
            logger.info("Saved ${categories.size} application categories")
        } catch (e: Exception) {
            logger.severe("Error saving application categories: ${e.message}")
        }
    }
    
    /**
     * Initialize default application categories
     */
    private fun initializeDefaultCategories() {
        // System applications
        val systemApps = listOf(
            "svchost.exe", "system", "registry", "smss.exe", "csrss.exe",
            "wininit.exe", "services.exe", "lsass.exe", "fontdrvhost.exe",
            "dwm.exe", "taskhost.exe", "explorer.exe", "taskhostw.exe", 
            "conhost.exe", "SearchApp.exe", "ShellExperienceHost.exe",
            "RuntimeBroker.exe", "backgroundTaskHost.exe", "StartMenuExperienceHost.exe",
            "sihost.exe", "ctfmon.exe", "SecurityHealthService.exe",
            "WindowsInternal.ComposableShell.Experiences.TextInput.InputApp.exe",
            "sppsvc.exe", "SearchIndexer.exe", "SystemSettings.exe", "WinStore.App.exe",
            "ApplicationFrameHost.exe", "LockApp.exe", "DataExchangeHost.exe"
        )
        
        // Browser applications
        val browserApps = listOf(
            "chrome.exe", "firefox.exe", "msedge.exe", "opera.exe", "brave.exe",
            "vivaldi.exe", "safari.exe", "iexplore.exe"
        )
        
        // Development applications
        val devApps = listOf(
            "code.exe", "devenv.exe", "pycharm64.exe", "idea64.exe",
            "eclipse.exe", "android studio.exe", "studio64.exe", "webstorm64.exe",
            "phpstorm64.exe", "rider64.exe", "notepad++.exe", "sublime_text.exe",
            "cmd.exe", "powershell.exe", "windowsterminal.exe", "git-bash.exe",
            "python.exe", "java.exe", "javaw.exe"
        )
        
        // Productivity applications
        val productivityApps = listOf(
            "winword.exe", "excel.exe", "powerpnt.exe", "outlook.exe", "onenote.exe",
            "access.exe", "publisher.exe", "acrord32.exe", "acrobat.exe",
            "libreoffice.exe", "soffice.exe", "calc.exe", "writer.exe",
            "thunderbird.exe", "evernote.exe", "notion.exe", "slack.exe",
            "msteams.exe", "zoom.exe", "obs64.exe", "anydesk.exe", "teamviewer.exe"
        )
        
        // Entertainment applications
        val entertainmentApps = listOf(
            "spotify.exe", "itunes.exe", "vlc.exe", "wmplayer.exe", "Music.UI.exe",
            "netflix.exe", "steam.exe", "epicgameslauncher.exe", "origin.exe",
            "battle.net.exe", "mpc-hc.exe", "mpc-hc64.exe", "foobar2000.exe",
            "aimp.exe", "mpv.exe", "winamp.exe", "groove.exe"
        )
        
        // Communication applications
        val communicationApps = listOf(
            "skype.exe", "telegram.exe", "whatsapp.exe", "discord.exe",
            "signal.exe", "microsoft.skypeapp.exe", "teams.exe", "slack.exe",
            "zoom.exe", "viber.exe", "wechat.exe", "mail.exe", "thunderbird.exe",
            "outlook.exe", "yammer.exe", "googlechat.exe", "messenger.exe",
            "skypeforwindows.exe", "rocketchat.exe", "claude.exe"
        )
        
        // Utility applications
        val utilityApps = listOf(
            "notepad.exe", "calc.exe", "mspaint.exe", "snippingtool.exe",
            "stikynot.exe", "magnify.exe", "narrator.exe", "photos.exe",
            "7zfm.exe", "winrar.exe", "winzip.exe", "ccleaner.exe",
            "cleanmgr.exe", "mstsc.exe", "snippingtool.exe", "wordpad.exe",
            "calculator.exe", "paint.exe", "paint3d.exe", "notepad.exe"
        )
        
        // Add all apps to categories dictionary
        systemApps.forEach { categories[it.lowercase()] = SYSTEM }
        browserApps.forEach { categories[it.lowercase()] = BROWSER }
        devApps.forEach { categories[it.lowercase()] = DEVELOPMENT }
        productivityApps.forEach { categories[it.lowercase()] = PRODUCTIVITY }
        entertainmentApps.forEach { categories[it.lowercase()] = ENTERTAINMENT }
        communicationApps.forEach { categories[it.lowercase()] = COMMUNICATION }
        utilityApps.forEach { categories[it.lowercase()] = UTILITY }
        
        // Save the default categories
        saveCategories()
    }
    
    /**
     * Get category for a given application name
     */
    fun getCategory(appName: String?): String {
        if (appName.isNullOrBlank()) {
            return UNKNOWN
        }
        
        val appLower = appName.lowercase()
        
        // Check if exact match exists
        if (categories.containsKey(appLower)) {
            return categories[appLower]!!
        }
        
        // Check for partial matches in app name
        for ((knownApp, category) in categories) {
            if (knownApp in appLower || appLower in knownApp) {
                return category
            }
        }
        
        // Apply heuristic rules for common application patterns
        val browserPatterns = listOf("chrome", "firefox", "edge", "opera", "safari", "brave")
        if (browserPatterns.any { it in appLower }) {
            return BROWSER
        }
        
        val devPatterns = listOf("code", "studio", "edit", "ide", "notepad", "vim", "emacs", "compiler", "terminal", "python", "java", "node", "npm")
        if (devPatterns.any { it in appLower }) {
            return DEVELOPMENT
        }
        
        val officePatterns = listOf("word", "excel", "powerpoint", "ppt", "doc", "spreadsheet", "calc", "write", "office", "libre", "outlook", "mail", "acrobat", "pdf")
        if (officePatterns.any { it in appLower }) {
            return PRODUCTIVITY
        }
        
        val mediaPatterns = listOf("play", "media", "vlc", "netflix", "spotify", "music", "video", "audio", "game", "steam", "player", "movie", "tv")
        if (mediaPatterns.any { it in appLower }) {
            return ENTERTAINMENT
        }
        
        val commPatterns = listOf("chat", "talk", "meet", "zoom", "teams", "skype", "discord", "slack", "messenger", "whatsapp", "telegram", "signal", "claude")
        if (commPatterns.any { it in appLower }) {
            return COMMUNICATION
        }
        
        if (appLower == "explorer.exe" || appLower.contains("finder")) {
            return UTILITY
        }
        
        // If extension is .exe and not categorized yet, consider it as a utility by default
        if (appLower.endsWith(".exe")) {
            return UTILITY
        }
        
        return UNKNOWN
    }
    
    /**
     * Add or update category for an application
     */
    fun addCategory(appName: String, category: String): Boolean {
        if (appName.isBlank()) {
            return false
        }
        
        val appLower = appName.lowercase()
        categories[appLower] = category
        saveCategories()
        return true
    }
    
    /**
     * Check if an application is a system app
     */
    fun isSystemApp(appName: String): Boolean {
        return getCategory(appName) == SYSTEM
    }
    
    /**
     * Get all unique categories
     */
    fun getAllCategories(): Set<String> {
        return categories.values.toSet()
    }
    
    /**
     * Get all applications in a specific category
     */
    fun getAppsByCategory(category: String): List<String> {
        return categories.filter { it.value == category }.keys.toList()
    }
}
