package com.dat.activity_tracker.data.local

import com.dat.activity_tracker.domain.model.AppTimelineEntry
import com.dat.activity_tracker.domain.model.AppUsageInfo
import com.dat.activity_tracker.domain.model.CategoryUsageInfo
import com.dat.activity_tracker.domain.model.UsageInfo
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger

/**
 * Database manager for Activity Tracker
 * Implements SQLite operations for desktop application
 */
class DatabaseManager(private val dbPath: String) {
    private val logger = Logger.getLogger("activity_tracker.database")
    private var connection: Connection? = null
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME

    /**
     * Get a connection to the database
     */
    fun getConnection(): Connection {
        if (connection == null || connection!!.isClosed) {
            try {
                // Load SQLite driver
                Class.forName("org.sqlite.JDBC")
                
                // Create connection
                connection = DriverManager.getConnection("jdbc:sqlite:$dbPath")
                logger.info("Database connection established")
            } catch (e: Exception) {
                logger.severe("Database connection error: ${e.message}")
                throw e
            }
        }
        return connection!!
    }

    /**
     * Initialize database if it doesn't exist
     */
    fun initializeDatabase() {
        val conn = getConnection()
        val stmt = conn.createStatement()
        
        try {
            // Activities table storing app usage information
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS activities (
                    id INTEGER PRIMARY KEY,
                    app_name TEXT NOT NULL,
                    window_title TEXT NOT NULL,
                    start_time TIMESTAMP NOT NULL,
                    end_time TIMESTAMP NOT NULL,
                    duration INTEGER NOT NULL,
                    category TEXT DEFAULT "Unknown"
                )
            """)
            
            // System events table (startup, shutdown)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS system_events (
                    id INTEGER PRIMARY KEY,
                    event_type TEXT NOT NULL,
                    timestamp TIMESTAMP NOT NULL
                )
            """)
            
            // App categories table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS app_categories (
                    id INTEGER PRIMARY KEY,
                    app_name TEXT UNIQUE NOT NULL,
                    category TEXT NOT NULL
                )
            """)
            
            logger.info("Database initialized")
        } catch (e: Exception) {
            logger.severe("Error initializing database: ${e.message}")
            throw e
        } finally {
            stmt.close()
        }
    }

    /**
     * Log an application activity
     */
    fun logActivity(
        appName: String,
        windowTitle: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        duration: Int,
        category: String = "Unknown"
    ) {
        val conn = getConnection()
        
        try {
            // First ensure the category is saved in app_categories table
            setAppCategory(appName, category)
            
            // Then log the activity with the category
            val stmt = conn.prepareStatement(
                """
                INSERT INTO activities 
                (app_name, window_title, start_time, end_time, duration, category) 
                VALUES (?, ?, ?, ?, ?, ?)
                """
            )
            stmt.setString(1, appName)
            stmt.setString(2, windowTitle)
            stmt.setString(3, startTime.format(formatter))
            stmt.setString(4, endTime.format(formatter))
            stmt.setInt(5, duration)
            stmt.setString(6, category)
            
            stmt.executeUpdate()
            stmt.close()
        } catch (e: Exception) {
            logger.severe("Error saving activity: ${e.message}")
        }
    }

    /**
     * Log a system event (startup/shutdown)
     */
    fun logSystemEvent(eventType: String) {
        val conn = getConnection()
        
        try {
            val timestamp = LocalDateTime.now()
            val stmt = conn.prepareStatement(
                "INSERT INTO system_events (event_type, timestamp) VALUES (?, ?)"
            )
            stmt.setString(1, eventType)
            stmt.setString(2, timestamp.format(formatter))
            
            stmt.executeUpdate()
            stmt.close()
            
            logger.info("System event logged: $eventType")
        } catch (e: Exception) {
            logger.severe("Error logging system event: ${e.message}")
        }
    }

    /**
     * Set category for an application
     */
    fun setAppCategory(appName: String, category: String) {
        val conn = getConnection()
        
        try {
            val stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO app_categories (app_name, category) VALUES (?, ?)"
            )
            stmt.setString(1, appName)
            stmt.setString(2, category)
            
            stmt.executeUpdate()
            stmt.close()
        } catch (e: Exception) {
            logger.severe("Error setting category: ${e.message}")
        }
    }

    /**
     * Get category for an application
     */
    fun getAppCategory(appName: String): String {
        val conn = getConnection()
        
        try {
            val stmt = conn.prepareStatement(
                "SELECT category FROM app_categories WHERE app_name = ?"
            )
            stmt.setString(1, appName)
            
            val rs = stmt.executeQuery()
            val category = if (rs.next()) rs.getString("category") else "Unknown"
            
            rs.close()
            stmt.close()
            
            return category
        } catch (e: Exception) {
            logger.severe("Error getting app category: ${e.message}")
            return "Unknown"
        }
    }

    /**
     * Get daily usage statistics
     */
    fun getDailyUsage(date: String? = null): List<UsageInfo> {
        val conn = getConnection()
        val formattedDate = date ?: LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
        
        try {
            val stmt = conn.prepareStatement("""
                SELECT app_name, SUM(duration) as total_duration 
                FROM activities 
                WHERE date(start_time) = ? 
                GROUP BY app_name 
                ORDER BY total_duration DESC
            """)
            stmt.setString(1, formattedDate)
            
            val rs = stmt.executeQuery()
            val result = mutableListOf<UsageInfo>()
            
            while (rs.next()) {
                val appName = rs.getString("app_name")
                val totalDuration = rs.getInt("total_duration")
                
                result.add(UsageInfo(appName, totalDuration))
            }
            
            rs.close()
            stmt.close()
            
            return result
        } catch (e: Exception) {
            logger.severe("Error getting daily usage: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Get weekly usage statistics
     */
    fun getWeeklyUsage(): List<UsageInfo> {
        val conn = getConnection()
        
        try {
            val stmt = conn.prepareStatement("""
                SELECT app_name, SUM(duration) as total_duration 
                FROM activities 
                WHERE date(start_time) >= date('now', '-7 days') 
                GROUP BY app_name 
                ORDER BY total_duration DESC
            """)
            
            val rs = stmt.executeQuery()
            val result = mutableListOf<UsageInfo>()
            
            while (rs.next()) {
                val appName = rs.getString("app_name")
                val totalDuration = rs.getInt("total_duration")
                
                result.add(UsageInfo(appName, totalDuration))
            }
            
            rs.close()
            stmt.close()
            
            return result
        } catch (e: Exception) {
            logger.severe("Error getting weekly usage: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Get monthly usage statistics
     */
    fun getMonthlyUsage(): List<UsageInfo> {
        val conn = getConnection()
        
        try {
            val stmt = conn.prepareStatement("""
                SELECT app_name, SUM(duration) as total_duration 
                FROM activities 
                WHERE date(start_time) >= date('now', '-30 days') 
                GROUP BY app_name 
                ORDER BY total_duration DESC
            """)
            
            val rs = stmt.executeQuery()
            val result = mutableListOf<UsageInfo>()
            
            while (rs.next()) {
                val appName = rs.getString("app_name")
                val totalDuration = rs.getInt("total_duration")
                
                result.add(UsageInfo(appName, totalDuration))
            }
            
            rs.close()
            stmt.close()
            
            return result
        } catch (e: Exception) {
            logger.severe("Error getting monthly usage: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Get usage statistics by category
     */
    fun getUsageByCategory(startDate: String? = null, endDate: String? = null): List<CategoryUsageInfo> {
        val conn = getConnection()
        
        try {
            var query = """
                SELECT c.category, SUM(a.duration) as total_duration 
                FROM activities a
                LEFT JOIN app_categories c ON a.app_name = c.app_name
            """
            
            val params = mutableListOf<String>()
            if (startDate != null) {
                query += " WHERE date(a.start_time) >= ?"
                params.add(startDate)
                
                if (endDate != null) {
                    query += " AND date(a.start_time) <= ?"
                    params.add(endDate)
                }
            } else if (endDate != null) {
                query += " WHERE date(a.start_time) <= ?"
                params.add(endDate)
            }
            
            query += " GROUP BY c.category ORDER BY total_duration DESC"
            
            val stmt = conn.prepareStatement(query)
            params.forEachIndexed { index, param ->
                stmt.setString(index + 1, param)
            }
            
            val rs = stmt.executeQuery()
            val result = mutableListOf<CategoryUsageInfo>()
            
            while (rs.next()) {
                val category = rs.getString("category") ?: "Unknown"
                val totalDuration = rs.getInt("total_duration")
                
                result.add(CategoryUsageInfo(category, totalDuration))
            }
            
            rs.close()
            stmt.close()
            
            return result
        } catch (e: Exception) {
            logger.severe("Error getting usage by category: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Get usage statistics with app categories
     */
    fun getUsageByAppWithCategory(date: String? = null): List<AppUsageInfo> {
        val conn = getConnection()
        
        try {
            var query = """
                SELECT a.app_name, a.window_title, SUM(a.duration) as total_duration, 
                       COALESCE(c.category, a.category, 'Unknown') as category
                FROM activities a
                LEFT JOIN app_categories c ON a.app_name = c.app_name
            """
            
            if (date != null) {
                query += " WHERE date(a.start_time) = ? "
            } else {
                query += " WHERE date(a.start_time) >= date('now', '-7 days') "
            }
            
            query += " GROUP BY a.app_name ORDER BY total_duration DESC"
            
            val stmt = conn.prepareStatement(query)
            if (date != null) {
                stmt.setString(1, date)
            }
            
            val rs = stmt.executeQuery()
            val result = mutableListOf<AppUsageInfo>()
            
            while (rs.next()) {
                val appName = rs.getString("app_name")
                val windowTitle = rs.getString("window_title")
                val totalDuration = rs.getInt("total_duration")
                var category = rs.getString("category") ?: "Unknown"
                
                // Double-check for default categories - ensure every app has a category
                if (category == "Unknown") {
                    // Try to get a category for this app
                    val appCategory = getAppCategory(appName)
                    if (appCategory != "Unknown") {
                        // Update the database
                        setAppCategory(appName, appCategory)
                        // Update the result
                        category = appCategory
                    }
                }
                
                result.add(AppUsageInfo(appName, windowTitle, totalDuration, category))
            }
            
            rs.close()
            stmt.close()
            
            return result
        } catch (e: Exception) {
            logger.severe("Error getting usage by app with category: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Get all applications with their categories
     */
    fun getAllAppCategories(): List<Pair<String, String>> {
        val conn = getConnection()
        
        try {
            val stmt = conn.prepareStatement(
                "SELECT app_name, category FROM app_categories ORDER BY app_name"
            )
            
            val rs = stmt.executeQuery()
            val result = mutableListOf<Pair<String, String>>()
            
            while (rs.next()) {
                val appName = rs.getString("app_name")
                val category = rs.getString("category")
                
                result.add(Pair(appName, category))
            }
            
            rs.close()
            stmt.close()
            
            return result
        } catch (e: Exception) {
            logger.severe("Error getting all app categories: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Update category for multiple applications
     */
    fun updateAppCategories(appCategories: Map<String, String>): Boolean {
        val conn = getConnection()
        
        try {
            conn.autoCommit = false
            
            val stmt = conn.prepareStatement(
                "INSERT OR REPLACE INTO app_categories (app_name, category) VALUES (?, ?)"
            )
            
            appCategories.forEach { (appName, category) ->
                stmt.setString(1, appName)
                stmt.setString(2, category)
                stmt.addBatch()
            }
            
            stmt.executeBatch()
            conn.commit()
            stmt.close()
            
            conn.autoCommit = true
            
            return true
        } catch (e: Exception) {
            logger.severe("Error updating app categories: ${e.message}")
            try {
                conn.rollback()
                conn.autoCommit = true
            } catch (rollbackEx: Exception) {
                logger.severe("Error rolling back transaction: ${rollbackEx.message}")
            }
            return false
        }
    }

    /**
     * Get applications by category
     */
    fun getAppsByCategory(category: String): List<String> {
        val conn = getConnection()
        
        try {
            val stmt = conn.prepareStatement(
                "SELECT app_name FROM app_categories WHERE category = ? ORDER BY app_name"
            )
            stmt.setString(1, category)
            
            val rs = stmt.executeQuery()
            val result = mutableListOf<String>()
            
            while (rs.next()) {
                val appName = rs.getString("app_name")
                result.add(appName)
            }
            
            rs.close()
            stmt.close()
            
            return result
        } catch (e: Exception) {
            logger.severe("Error getting apps by category: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Get all available categories
     */
    fun getAllAvailableCategories(): List<String> {
        val conn = getConnection()
        
        try {
            val stmt = conn.prepareStatement(
                "SELECT DISTINCT category FROM app_categories ORDER BY category"
            )
            
            val rs = stmt.executeQuery()
            val result = mutableListOf<String>()
            
            while (rs.next()) {
                val category = rs.getString("category")
                result.add(category)
            }
            
            rs.close()
            stmt.close()
            
            return result
        } catch (e: Exception) {
            logger.severe("Error getting all available categories: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Get app timeline data showing focus and background times
     */
    fun getAppTimelineData(appName: String, days: Int = 7): List<AppTimelineEntry> {
        val conn = getConnection()
        
        try {
            val stmt = conn.prepareStatement("""
                SELECT app_name, window_title, start_time, end_time, duration,
                      CASE WHEN id IN (
                          SELECT id FROM activities a2
                          WHERE app_name = ?
                          ORDER BY end_time DESC
                          LIMIT 1
                      ) THEN 1 ELSE 0 END as is_foreground
                FROM activities
                WHERE app_name = ? 
                AND date(start_time) >= date('now', '-$days days')
                ORDER BY start_time DESC
            """)
            
            stmt.setString(1, appName)
            stmt.setString(2, appName)
            
            val rs = stmt.executeQuery()
            val result = mutableListOf<AppTimelineEntry>()
            
            while (rs.next()) {
                val windowTitle = rs.getString("window_title")
                val startTime = rs.getString("start_time")
                val endTime = rs.getString("end_time")
                val duration = rs.getInt("duration")
                val isForeground = rs.getInt("is_foreground") == 1
                
                result.add(AppTimelineEntry(
                    appName = appName,
                    windowTitle = windowTitle,
                    startTime = startTime,
                    endTime = endTime,
                    duration = duration,
                    isForeground = isForeground
                ))
            }
            
            rs.close()
            stmt.close()
            
            return result
        } catch (e: Exception) {
            logger.severe("Error getting app timeline data: ${e.message}")
            return emptyList()
        }
    }
    
    /**
     * Get currently focused app and background apps with their categories
     */
    fun getCurrentActivityStatus(): Pair<AppUsageInfo?, List<AppUsageInfo>> {
        val conn = getConnection()
        
        try {
            // Get the most recent active app
            val focusedAppStmt = conn.prepareStatement("""
                SELECT a.app_name, a.window_title, a.duration, 
                      COALESCE(c.category, a.category, 'Unknown') as category
                FROM activities a
                LEFT JOIN app_categories c ON a.app_name = c.app_name
                ORDER BY a.end_time DESC
                LIMIT 1
            """)
            
            val focusedRs = focusedAppStmt.executeQuery()
            val focusedApp = if (focusedRs.next()) {
                AppUsageInfo(
                    focusedRs.getString("app_name"),
                    focusedRs.getString("window_title"),
                    focusedRs.getInt("duration"),
                    focusedRs.getString("category")
                )
            } else {
                null
            }
            
            focusedRs.close()
            focusedAppStmt.close()
            
            // Get recent background apps (excluding the focused app)
            val backgroundAppsStmt = conn.prepareStatement("""
                SELECT a.app_name, a.window_title, SUM(a.duration) as total_duration, 
                      COALESCE(c.category, a.category, 'Unknown') as category
                FROM activities a
                LEFT JOIN app_categories c ON a.app_name = c.app_name
                WHERE a.app_name != ? 
                AND datetime(a.end_time) >= datetime('now', '-1 hour')
                GROUP BY a.app_name
                ORDER BY MAX(a.end_time) DESC
                LIMIT 20
            """)
            
            backgroundAppsStmt.setString(1, focusedApp?.appName ?: "")
            
            val backgroundRs = backgroundAppsStmt.executeQuery()
            val backgroundApps = mutableListOf<AppUsageInfo>()
            
            while (backgroundRs.next()) {
                backgroundApps.add(
                    AppUsageInfo(
                        backgroundRs.getString("app_name"),
                        backgroundRs.getString("window_title"),
                        backgroundRs.getInt("total_duration"),
                        backgroundRs.getString("category")
                    )
                )
            }
            
            backgroundRs.close()
            backgroundAppsStmt.close()
            
            return Pair(focusedApp, backgroundApps)
        } catch (e: Exception) {
            logger.severe("Error getting current activity status: ${e.message}")
            return Pair(null, emptyList())
        }
    }

    /**
     * Close the database connection
     */
    fun close() {
        try {
            connection?.close()
            connection = null
        } catch (e: Exception) {
            logger.severe("Error closing database connection: ${e.message}")
        }
    }
}




