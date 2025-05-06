package com.dat.activity_tracker.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferAction
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.DragAndDropTransferable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.dat.activity_tracker.domain.model.AppUsageInfo
import com.dat.activity_tracker.util.formatDuration

/**
 * Enhanced app item with modern drag and drop support
 */
@Composable
fun EnhancedAppItem(
    app: AppUsageInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Extract colors for drag decoration
    val primaryColor = MaterialTheme.colors.primary
    val surfaceColor = MaterialTheme.colors.surface
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(surfaceColor)
            .border(
                width = 1.dp,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(4.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        AppItemComponent(primaryColor, app)
    }
}

@Composable
private fun AppItemComponent(
    primaryColor: Color,
    app: AppUsageInfo
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(12.dp))

        // App info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                formatAppName(app.appName),
                style = MaterialTheme.typography.subtitle1,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                "Used: ${formatDuration(app.totalDuration.toLong())}",
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }

        // Drag handle icon
        Icon(
            Icons.Default.DragIndicator,
            contentDescription = "Drag app",
            tint = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.size(24.dp)
        )
    }
}

/**
 * Component that shows a visual preview of the item being dragged
 */
@Composable
fun DragPreviewItem(
    app: AppUsageInfo,
    appIcon: ImageBitmap?
) {
    val density = LocalDensity.current
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(70.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colors.surface)
            .border(
                width = 2.dp,
                color = MaterialTheme.colors.primary.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colors.primary.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon,
                        contentDescription = "${app.appName} icon",
                        modifier = Modifier.size(36.dp),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Icon(
                        Icons.Default.Apps,
                        contentDescription = null,
                        tint = MaterialTheme.colors.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // App info
            Column {
                Text(
                    formatAppName(app.appName),
                    style = MaterialTheme.typography.subtitle2,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Used: ${formatDuration(app.totalDuration.toLong())}",
                    style = MaterialTheme.typography.caption,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * Utility function to format app names to be more user-friendly
 */
private fun formatAppName(appName: String): String {
    // Extract only the file name from full path if present
    val fileName = appName.split("[/\\\\]".toRegex()).last()

    // Remove .exe extension if present
    val nameWithoutExt = if (fileName.endsWith(".exe", ignoreCase = true)) {
        fileName.substring(0, fileName.length - 4)
    } else {
        fileName
    }

    // Capitalize first letter of each word and replace underscores with spaces
    return nameWithoutExt.split("[_.\\s]+".toRegex())
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        }
}