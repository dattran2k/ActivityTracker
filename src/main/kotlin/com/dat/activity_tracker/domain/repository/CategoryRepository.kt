package com.dat.activity_tracker.domain.repository

/**
 * Repository interface for app category operations
 */
interface CategoryRepository {
    
    /**
     * Get category for an application
     */
    suspend fun getAppCategory(appName: String): String
    
    /**
     * Set category for an application
     */
    suspend fun setAppCategory(appName: String, category: String)
    
    /**
     * Update categories for multiple applications
     */
    suspend fun updateAppCategories(appCategories: Map<String, String>): Boolean
    
    /**
     * Get all applications with their categories
     */
    suspend fun getAllAppCategories(): List<Pair<String, String>>
    
    /**
     * Get applications by category
     */
    suspend fun getAppsByCategory(category: String): List<String>
    
    /**
     * Get all available categories
     */
    suspend fun getAllAvailableCategories(): List<String>
}
