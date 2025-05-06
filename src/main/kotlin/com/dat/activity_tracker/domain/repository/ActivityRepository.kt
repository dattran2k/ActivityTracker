package com.dat.activity_tracker.domain.repository

import com.dat.activity_tracker.domain.model.AppTimelineEntry
import com.dat.activity_tracker.domain.model.AppUsageInfo
import com.dat.activity_tracker.domain.model.CategoryUsageInfo
import com.dat.activity_tracker.domain.model.UsageInfo
import java.time.LocalDateTime

/**
 * Repository interface for activity tracking operations
 */
interface ActivityRepository {
    
    /**
     * Log an application activity
     */
    suspend fun logActivity(
        appName: String,
        windowTitle: String,
        startTime: LocalDateTime,
        endTime: LocalDateTime,
        duration: Int,
        category: String = "Unknown"
    )
    
    /**
     * Log a system event (startup/shutdown)
     */
    suspend fun logSystemEvent(eventType: String)
    
    /**
     * Get daily usage statistics
     */
    suspend fun getDailyUsage(date: String? = null): List<UsageInfo>
    
    /**
     * Get weekly usage statistics
     */
    suspend fun getWeeklyUsage(): List<UsageInfo>
    
    /**
     * Get monthly usage statistics
     */
    suspend fun getMonthlyUsage(): List<UsageInfo>
    
    /**
     * Get usage statistics by category
     */
    suspend fun getUsageByCategory(startDate: String? = null, endDate: String? = null): List<CategoryUsageInfo>
    
    /**
     * Get usage statistics with app categories
     */
    suspend fun getUsageByAppWithCategory(date: String? = null): List<AppUsageInfo>
    
    /**
     * Get app timeline data showing focus and background times
     */
    suspend fun getAppTimelineData(appName: String, days: Int = 7): List<AppTimelineEntry>
    
    /**
     * Get currently focused app and background apps with their categories
     */
    suspend fun getCurrentActivityStatus(): Pair<AppUsageInfo?, List<AppUsageInfo>>
}
