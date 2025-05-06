package com.dat.activity_tracker.domain.model

/**
 * Data class for detailed app usage information
 */
data class AppUsageInfo(
    val appName: String,
    val windowTitle: String,
    val totalDuration: Int,
    val category: String
)
