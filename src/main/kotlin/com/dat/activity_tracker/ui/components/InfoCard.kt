package com.dat.activity_tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Information card component displaying current activity and status
 */
@Composable
fun InfoCard(
    currentApp: String,
    currentWindow: String,
    currentCategory: String,
    currentAppTime: String,
    totalMonitoringTime: String,
    backgroundApps: List<String>,
    isMonitoring: Boolean,
    onStartMonitoring: () -> Unit,
    onStopMonitoring: () -> Unit
) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Current activity info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Currently tracking:", style = MaterialTheme.typography.subtitle1)
                    Text(currentApp, style = MaterialTheme.typography.h6)
                    Text(currentWindow, style = MaterialTheme.typography.caption)

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Category: $currentCategory", style = MaterialTheme.typography.body2)
                    Text("Current usage time: $currentAppTime", style = MaterialTheme.typography.body2)
                    Text("Total monitoring time: $totalMonitoringTime", style = MaterialTheme.typography.body2)
                }

                // Start/Stop button
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (!isMonitoring) {
                        Button(
                            onClick = onStartMonitoring,
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start monitoring")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Start Monitoring")
                        }
                    } else {
                        Button(
                            onClick = onStopMonitoring,
                            colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.error)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop monitoring")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Stop Monitoring")
                        }
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 8.dp))

            // Active and background apps section
            Text("Activity Status:", style = MaterialTheme.typography.subtitle1)

            // Current app (focused)
            if (currentApp != "No data") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Focused",
                        tint = MaterialTheme.colors.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("Focused: $currentApp", style = MaterialTheme.typography.body1)
                        Text(currentCategory, style = MaterialTheme.typography.caption)
                    }
                }
            }

            // Background apps list
            Text("Background apps:", style = MaterialTheme.typography.subtitle2)
            if (backgroundApps.isEmpty()) {
                Text("None", style = MaterialTheme.typography.body2)
            } else {
                Column {
                    backgroundApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Apps,
                                contentDescription = "Background",
                                tint = MaterialTheme.colors.secondaryVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(app, style = MaterialTheme.typography.body2)
                        }
                    }
                }
            }
        }
    }
}