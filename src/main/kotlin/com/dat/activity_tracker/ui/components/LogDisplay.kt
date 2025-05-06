package com.dat.activity_tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dat.activity_tracker.util.LogEntry
import java.time.format.DateTimeFormatter

/**
 * Log display component showing application log entries
 */
@Composable
fun LogDisplay(modifier: Modifier = Modifier, logEntries: List<LogEntry>) {
    Card(
        elevation = 4.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Activity Log", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))

            // Log entries display
            Surface(
                modifier = Modifier.fillMaxWidth().weight(1f),
                color = MaterialTheme.colors.surface.copy(alpha = 0.3f)
            ) {
                val scrollState = androidx.compose.foundation.rememberScrollState()

                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                ) {
                    for (entry in logEntries) {
                        val color = when (entry.level) {
                            "SEVERE" -> MaterialTheme.colors.error
                            "WARNING" -> MaterialTheme.colors.secondary
                            "INFO" -> MaterialTheme.colors.primary
                            else -> MaterialTheme.colors.onSurface
                        }

                        Text(
                            text = "[${entry.timestamp.format(DateTimeFormatter.ofPattern("HH:mm:ss"))}] ${entry.level}: ${entry.message}",
                            color = color,
                            style = MaterialTheme.typography.body2,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}