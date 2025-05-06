package com.dat.activity_tracker.data.repository

import com.dat.activity_tracker.data.local.DatabaseManager
import com.dat.activity_tracker.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger

/**
 * Implementation of CategoryRepository using SQLite database
 */
class CategoryRepositoryImpl(
    private val databaseManager: DatabaseManager
) : CategoryRepository {
    
    private val logger = Logger.getLogger("activity_tracker.repository")
    
    override suspend fun getAppCategory(appName: String): String = withContext(Dispatchers.IO) {
        databaseManager.getAppCategory(appName)
    }
    
    override suspend fun setAppCategory(appName: String, category: String) = withContext(Dispatchers.IO) {
        databaseManager.setAppCategory(appName, category)
    }
    
    override suspend fun updateAppCategories(appCategories: Map<String, String>): Boolean = withContext(Dispatchers.IO) {
        databaseManager.updateAppCategories(appCategories)
    }
    
    override suspend fun getAllAppCategories(): List<Pair<String, String>> = withContext(Dispatchers.IO) {
        databaseManager.getAllAppCategories()
    }
    
    override suspend fun getAppsByCategory(category: String): List<String> = withContext(Dispatchers.IO) {
        databaseManager.getAppsByCategory(category)
    }
    
    override suspend fun getAllAvailableCategories(): List<String> = withContext(Dispatchers.IO) {
        databaseManager.getAllAvailableCategories()
    }
}
