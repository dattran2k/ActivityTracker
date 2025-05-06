package com.dat.activity_tracker.ui.feature.monitor

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dat.activity_tracker.ui.components.InfoCard
import com.dat.activity_tracker.ui.components.LogDisplay
import com.dat.activity_tracker.ui.components.ReportButtons
import com.dat.activity_tracker.util.LogEntry

/**
 * Activity Monitor Tab
 */
@Composable
fun ActivityMonitorTab(
    currentApp: String,
    currentWindow: String,
    currentCategory: String,
    currentAppTime: String,
    totalMonitoringTime: String,
    backgroundApps: List<String>,
    isMonitoring: Boolean,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit,
    onGenerateDaily: () -> Unit,
    onGenerateWeekly: () -> Unit,
    onGenerateMonthly: () -> Unit,
    logEntries: List<LogEntry>
) {
    Column {
        // App info and controls
        InfoCard(
            currentApp = currentApp,
            currentWindow = currentWindow,
            currentCategory = currentCategory,
            currentAppTime = currentAppTime,
            totalMonitoringTime = totalMonitoringTime,
            backgroundApps = backgroundApps,
            isMonitoring = isMonitoring,
            onStartMonitoring = onStartMonitoring,
            onStopMonitoring = onStopMonitoring
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Report generation buttons
        ReportButtons(
            onGenerateDaily = onGenerateDaily,
            onGenerateWeekly = onGenerateWeekly,
            onGenerateMonthly = onGenerateMonthly
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Log display
        LogDisplay(Modifier.fillMaxWidth().weight(1f), logEntries)
    }
}