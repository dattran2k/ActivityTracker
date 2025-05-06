package com.dat.activity_tracker.ui.feature.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.dat.activity_tracker.domain.model.AppTimelineEntry
import com.dat.activity_tracker.domain.model.AppUsageInfo
import com.dat.activity_tracker.util.formatAppName
import com.dat.activity_tracker.util.formatDuration
import java.time.format.DateTimeFormatter


/**
 * Dialog showing detailed app information and usage timeline
 */
@Composable
fun AppDetailsDialog(
    app: AppUsageInfo,
    timelineData: List<AppTimelineEntry>,
    onDismiss: () -> Unit,
    onChangeCategory: (String, String) -> Unit
) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val totalFocusTime = timelineData.filter { it.isForeground }.sumOf { it.duration }
    val totalBackgroundTime = timelineData.filter { !it.isForeground }.sumOf { it.duration }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnClickOutside = true, dismissOnBackPress = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        formatAppName(app.appName),
                        style = MaterialTheme.typography.h5,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // App details
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    DetailRow("Category", app.category) {
                        IconButton(onClick = { onChangeCategory(app.appName, app.category) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Change category")
                        }
                    }

                    DetailRow("Total Usage", formatDuration(app.totalDuration.toLong()))
                    DetailRow("Focus Time", formatDuration(totalFocusTime.toLong()))
                    DetailRow("Background Time", formatDuration(totalBackgroundTime.toLong()))

                    if (app.windowTitle.isNotBlank()) {
                        DetailRow("Last Window", app.windowTitle)
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Timeline
                Text(
                    "Usage Timeline",
                    style = MaterialTheme.typography.h6,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                if (timelineData.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No detailed timeline available",
                            color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        items(timelineData.sortedByDescending { it.startTime }) { entry ->
                            TimelineEntry(
                                startTime = entry.startTime.format(formatter),
                                endTime = entry.endTime.format(formatter),
                                duration = formatDuration(entry.duration.toLong()),
                                isForeground = entry.isForeground,
                                windowTitle = entry.windowTitle,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
/**
 * A single timeline entry in the app details
 */
@Composable
fun TimelineEntry(
    startTime: String,
    endTime: String,
    duration: String,
    isForeground: Boolean,
    windowTitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = 2.dp,
        backgroundColor = if (isForeground)
            MaterialTheme.colors.primary.copy(alpha = 0.1f)
        else
            MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (isForeground) "Focused" else "Background",
                    fontWeight = FontWeight.Bold,
                    color = if (isForeground) MaterialTheme.colors.primary else Color.Gray
                )

                Text(
                    text = duration,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "$startTime → $endTime",
                style = MaterialTheme.typography.caption
            )

            if (windowTitle.isNotBlank()) {
                Text(
                    text = windowTitle,
                    style = MaterialTheme.typography.body2,
                    modifier = Modifier.padding(top = 4.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * A single detail row for the app details section
 */
@Composable
fun DetailRow(
    label: String,
    value: String,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.subtitle1,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(150.dp)
        )

        Text(
            value,
            style = MaterialTheme.typography.body1,
            modifier = Modifier.weight(1f)
        )

        if (trailing != null) {
            trailing()
        }
    }
}
