package com.dat.activity_tracker.domain.model

/**
 * Data class for app timeline entry
 */
data class AppTimelineEntry(
    val appName: String,
    val windowTitle: String,
    val startTime: String,
    val endTime: String,
    val duration: Int,
    val isForeground: Boolean
)