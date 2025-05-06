package com.dat.activity_tracker

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.dat.activity_tracker.ui.ActivityTrackerApp
import com.dat.activity_tracker.data.AppCategorizer
import com.dat.activity_tracker.data.local.DatabaseManager
import com.dat.activity_tracker.data.repository.ActivityRepositoryImpl
import com.dat.activity_tracker.data.repository.CategoryRepositoryImpl
import com.dat.activity_tracker.util.LogManager
import java.io.File
import java.util.logging.Level

fun main() = application {
    val state = rememberWindowState(width = 900.dp, height = 700.dp)

    // Set up directories
    val dataDir = File("data")
    if (!dataDir.exists()) {
        dataDir.mkdir()
    }

    val logsDir = File("logs")
    if (!logsDir.exists()) {
        logsDir.mkdir()
    }

    // Initialize logging
    LogManager.setupLogging()
    LogManager.addLogEntry(Level.INFO, "Starting Activity Tracker application")
    
    // Initialize database
    val databaseManager = DatabaseManager("data/activity_tracker.db")
    databaseManager.initializeDatabase()
    LogManager.addLogEntry(Level.INFO, "Database initialized")
    
    // Create repositories
    val activityRepository = ActivityRepositoryImpl(databaseManager)
    val categoryRepository = CategoryRepositoryImpl(databaseManager)
    
    // Initialize app categorizer
    val appCategorizer = AppCategorizer()
    LogManager.addLogEntry(Level.INFO, "App categorizer initialized")

    Window(
        onCloseRequest = {
            // Clean up before exit
            try {
                databaseManager.close()
                LogManager.addLogEntry(Level.INFO, "Database connection closed")
            } catch (e: Exception) {
                LogManager.addLogEntry(Level.SEVERE, "Error closing database: ${e.message}")
            }

            LogManager.addLogEntry(Level.INFO, "Exiting application")
            exitApplication()
        },
        title = "Activity Tracker",
        resizable = true,
        state = state
    ) {
        MaterialTheme(colors = darkColors()) {
            Surface(color = MaterialTheme.colors.background) {
                ActivityTrackerApp(
                    activityRepository = activityRepository,
                    categoryRepository = categoryRepository
                )
            }
        }
    }
}
