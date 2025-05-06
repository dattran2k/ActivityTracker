package com.dat.activity_tracker.data.repository

import com.dat.activity_tracker.data.local.DatabaseManager
import com.dat.activity_tracker.domain.model.AppTimelineEntry
import com.dat.activity_tracker.domain.model.AppUsageInfo
import com.dat.activity_tracker.domain.model.CategoryUsageInfo
import com.dat.activity_tracker.domain.model.UsageInfo
import com.dat.activity_tracker.domain.repository.ActivityRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.logging.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Implementation of ActivityRepository using SQLite database
 */
class ActivityRepositoryImpl(
    private val databaseManager: DatabaseManager
) : ActivityRepository {
    
    private val logger = Logger.getLogger("activity_tracker.repository")
    
    override suspend fun logActivity(
        appName: String, 
        windowTitle: String, 
        startTime: LocalDateTime, 
        endTime: LocalDateTime, 
        duration: Int, 
        category: String
    ) = withContext(Dispatchers.IO) {
        databaseManager.logActivity(appName, windowTitle, startTime, endTime, duration, category)
    }

    override suspend fun logSystemEvent(eventType: String) = withContext(Dispatchers.IO) {
        databaseManager.logSystemEvent(eventType)
    }

    override suspend fun getDailyUsage(date: String?): List<UsageInfo> = withContext(Dispatchers.IO) {
        databaseManager.getDailyUsage(date)
    }

    override suspend fun getWeeklyUsage(): List<UsageInfo> = withContext(Dispatchers.IO) {
        databaseManager.getWeeklyUsage()
    }

    override suspend fun getMonthlyUsage(): List<UsageInfo> = withContext(Dispatchers.IO) {
        databaseManager.getMonthlyUsage()
    }

    override suspend fun getUsageByCategory(startDate: String?, endDate: String?): List<CategoryUsageInfo> = withContext(Dispatchers.IO) {
        databaseManager.getUsageByCategory(startDate, endDate)
    }

    override suspend fun getUsageByAppWithCategory(date: String?): List<AppUsageInfo> = withContext(Dispatchers.IO) {
        databaseManager.getUsageByAppWithCategory(date)
    }

    override suspend fun getAppTimelineData(appName: String, days: Int): List<AppTimelineEntry> = withContext(Dispatchers.IO) {
        databaseManager.getAppTimelineData(appName, days)
    }

    override suspend fun getCurrentActivityStatus(): Pair<AppUsageInfo?, List<AppUsageInfo>> = withContext(Dispatchers.IO) {
        databaseManager.getCurrentActivityStatus()
    }
}
