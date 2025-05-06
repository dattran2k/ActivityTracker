package com.dat.activity_tracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dat.activity_tracker.data.AppCategorizer
import com.dat.activity_tracker.domain.model.AppUsageInfo
import com.dat.activity_tracker.domain.model.AppTimelineEntry
import com.dat.activity_tracker.domain.repository.ActivityRepository
import com.dat.activity_tracker.domain.repository.CategoryRepository
import com.dat.activity_tracker.monitor.ActivityMonitor
import com.dat.activity_tracker.report.ReportGenerator
import com.dat.activity_tracker.ui.components.*
import com.dat.activity_tracker.ui.feature.category.CategoriesTab
import com.dat.activity_tracker.ui.feature.category.CategoryDialog
import com.dat.activity_tracker.ui.feature.monitor.ActivityMonitorTab
import com.dat.activity_tracker.util.LogManager
import com.dat.activity_tracker.util.formatDuration
import com.dat.activity_tracker.util.openReportFile
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.LocalDateTime
import java.util.logging.Level

/**
 * Main application UI component
 */
@Composable
fun ActivityTrackerApp(
    activityRepository: ActivityRepository,
    categoryRepository: CategoryRepository
) {
    val scope = rememberCoroutineScope()
    val activityMonitor = remember { ActivityMonitor(activityRepository, categoryRepository) }
    val reportGenerator = remember { ReportGenerator(activityRepository) }
    val appCategorizer = remember { AppCategorizer() }

    // State
    val isMonitoring by activityMonitor.isMonitoring.collectAsState()
    val backgroundApps by activityMonitor.backgroundAppsList.collectAsState()
    val logEntries by LogManager.logEntries.collectAsState()

    var currentApp by remember { mutableStateOf("No data") }
    var currentWindow by remember { mutableStateOf("") }
    var currentCategory by remember { mutableStateOf("") }
    var appStartTime by remember { mutableStateOf<LocalDateTime?>(null) }

    var totalMonitoringTime by remember { mutableStateOf(0L) }
    var currentAppTime by remember { mutableStateOf(0L) }
    var monitoringStartTime by remember { mutableStateOf<LocalDateTime?>(null) }

    var statusMessage by remember { mutableStateOf("Ready") }

    // Tab state
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabTitles = listOf("Activity Monitor", "Categories")

    // App usage data for category management
    var appUsageData by remember { mutableStateOf<List<AppUsageInfo>>(emptyList()) }
    var isLoadingData by remember { mutableStateOf(false) }
    var appTimelineData by remember { mutableStateOf<Map<String, List<AppTimelineEntry>>>(emptyMap()) }

    // Dialog state for category selection
    var showCategoryDialog by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf("") }
    var currentAppCategory by remember { mutableStateOf("") }

    // Timer for updating elapsed time
    LaunchedEffect(isMonitoring) {
        if (isMonitoring) {
            monitoringStartTime = LocalDateTime.now()
            while (isMonitoring) {
                val now = LocalDateTime.now()

                // Update total monitoring time
                if (monitoringStartTime != null) {
                    totalMonitoringTime = Duration.between(monitoringStartTime, now).seconds
                }

                // Update current app time
                if (appStartTime != null) {
                    currentAppTime = Duration.between(appStartTime, now).seconds
                }

                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }

    // Load app usage data when Categories tab is selected
    LaunchedEffect(selectedTabIndex) {
        if (selectedTabIndex == 1 && appUsageData.isEmpty()) {
            isLoadingData = true
            try {
                // Get app usage with categories
                appUsageData = activityRepository.getUsageByAppWithCategory()

                // Preload timeline data for common apps for better performance
                val timelineData = mutableMapOf<String, List<AppTimelineEntry>>()
                appUsageData.take(5).forEach { app ->
                    timelineData[app.appName] = activityRepository.getAppTimelineData(app.appName)
                }
                appTimelineData = timelineData

                isLoadingData = false
            } catch (e: Exception) {
                LogManager.addLogEntry(Level.SEVERE, "Failed to load app categories: ${e.message}")
                isLoadingData = false
            }
        }
    }

    // Main layout
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        // Tabs
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) },
                    icon = {
                        when (index) {
                            0 -> Icon(Icons.Default.Timeline, contentDescription = "Activity Monitor")
                            1 -> Icon(Icons.Default.Category, contentDescription = "Categories")
                        }
                    }
                )
            }
        }
        // Status bar
        StatusBar(statusMessage)
        Spacer(modifier = Modifier.height(16.dp))

        // Tab content
        when (selectedTabIndex) {
            0 -> {
                // Activity Monitor Tab
                ActivityMonitorTab(
                    currentApp = currentApp,
                    currentWindow = currentWindow,
                    currentCategory = currentCategory,
                    currentAppTime = formatDuration(currentAppTime),
                    totalMonitoringTime = formatDuration(totalMonitoringTime),
                    backgroundApps = backgroundApps,
                    isMonitoring = isMonitoring,
                    onStartMonitoring = {
                        scope.launch {
                            // Start monitoring in coroutine
                            activityMonitor.startMonitoring { app, window, category ->
                                currentApp = app
                                currentWindow = window
                                currentCategory = category
                                appStartTime = LocalDateTime.now()
                                currentAppTime = 0
                            }
                        }
                    },
                    onStopMonitoring = {
                        activityMonitor.stopMonitoring()
                    },
                    onGenerateDaily = {
                        scope.launch {
                            statusMessage = "Generating daily report..."
                            val reportPath = reportGenerator.generateDailyReport()
                            if (reportPath != null) {
                                statusMessage = "Daily report generated successfully"
                                openReportFile(reportPath)
                            } else {
                                statusMessage = "Failed to generate report: No data available"
                            }
                        }
                    },
                    onGenerateWeekly = {
                        scope.launch {
                            statusMessage = "Generating weekly report..."
                            val reportPath = reportGenerator.generateWeeklyReport()
                            if (reportPath != null) {
                                statusMessage = "Weekly report generated successfully"
                                openReportFile(reportPath)
                            } else {
                                statusMessage = "Failed to generate report: No data available"
                            }
                        }
                    },
                    onGenerateMonthly = {
                        scope.launch {
                            statusMessage = "Generating monthly report..."
                            val reportPath = reportGenerator.generateMonthlyReport()
                            if (reportPath != null) {
                                statusMessage = "Monthly report generated successfully"
                                openReportFile(reportPath)
                            } else {
                                statusMessage = "Failed to generate report: No data available"
                            }
                        }
                    },
                    logEntries = logEntries
                )
            }

            1 -> {
                // Categories Tab
                CategoriesTab(
                    isLoading = isLoadingData,
                    appUsageData = appUsageData,
                    appTimelineData = appTimelineData,
                    onRefresh = {
                        scope.launch {
                            isLoadingData = true
                            try {
                                appUsageData = activityRepository.getUsageByAppWithCategory()

                                // Refresh timeline data for common apps
                                val timelineData = mutableMapOf<String, List<AppTimelineEntry>>()
                                appUsageData.take(5).forEach { app ->
                                    timelineData[app.appName] = activityRepository.getAppTimelineData(app.appName)
                                }
                                appTimelineData = timelineData

                                isLoadingData = false
                                statusMessage = "App categories refreshed"
                            } catch (e: Exception) {
                                LogManager.addLogEntry(Level.SEVERE, "Failed to refresh app categories: ${e.message}")
                                isLoadingData = false
                                statusMessage = "Failed to refresh app categories"
                            }
                        }
                    },
                    onChangeCategory = { app, category ->
                        selectedApp = app
                        currentAppCategory = category
                        showCategoryDialog = true
                    },
                    onCreateCategory = { newCategory ->
                        scope.launch {
                            try {
                                // Create a default application if needed
                                categoryRepository.setAppCategory("New Application", newCategory)

                                // Refresh data to show the new category
                                appUsageData = activityRepository.getUsageByAppWithCategory()
                                statusMessage = "Created new category: $newCategory"
                            } catch (e: Exception) {
                                LogManager.addLogEntry(Level.SEVERE, "Failed to create category: ${e.message}")
                                statusMessage = "Failed to create category"
                            }
                        }
                    },
                    onMoveAppToCategory = { appName, targetCategory ->
                        scope.launch {
                            try {
                                // Update the app's category in the database
                                categoryRepository.setAppCategory(appName, targetCategory)
                                appCategorizer.addCategory(appName, targetCategory)

                                // Update the UI list
                                appUsageData = appUsageData.map {
                                    if (it.appName == appName) {
                                        it.copy(category = targetCategory)
                                    } else {
                                        it
                                    }
                                }

                                statusMessage = "Moved $appName to $targetCategory"
                            } catch (e: Exception) {
                                LogManager.addLogEntry(Level.SEVERE, "Failed to move app: ${e.message}")
                                statusMessage = "Failed to move app to category"
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }

    // Get available categories for selection dialog
    val categories = remember {
        mutableStateOf(emptyList<String>())
    }

    // Load all available categories
    LaunchedEffect(Unit) {
        categories.value = AppCategorizer.Companion::class.java.declaredFields
            .filter { it.name != "INSTANCE" && it.name != "UNKNOWN" }
            .map { it.name }
            .toList() + categoryRepository.getAllAvailableCategories()
    }

    // Category selection dialog
    CategoryDialog(
        showDialog = showCategoryDialog,
        selectedApp = selectedApp,
        currentCategory = currentAppCategory,
        categories = categories.value.distinct(),
        onDismiss = { showCategoryDialog = false },
        onCategorySelected = { app, category ->
            // Update category

            scope.launch {
                appCategorizer.addCategory(app, category)
                categoryRepository.setAppCategory(app, category)

                // Update the UI list
                appUsageData = appUsageData.map {
                    if (it.appName == app) {
                        it.copy(category = category)
                    } else {
                        it
                    }
                }

                // Update timeline data if we already have it loaded
                if (appTimelineData.containsKey(app)) {
                    val timelineData = activityRepository.getAppTimelineData(app)
                    appTimelineData = appTimelineData.toMutableMap().apply {
                        put(app, timelineData)
                    }
                }
            }



            statusMessage = "Updated category for $app to $category"
            showCategoryDialog = false
        }
    )
}