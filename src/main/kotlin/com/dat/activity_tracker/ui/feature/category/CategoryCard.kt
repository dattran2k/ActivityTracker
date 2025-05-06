package com.dat.activity_tracker.ui.feature.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.dat.activity_tracker.domain.model.AppUsageInfo
import com.dat.activity_tracker.ui.components.EnhancedAppItem
import com.mohamedrejeb.compose.dnd.drag.DropStrategy
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.reorder.ReorderState
import com.mohamedrejeb.compose.dnd.reorder.ReorderableItem

/**
 * Represents a single category card with its apps
 */
@ExperimentalFoundationApi
@Composable
fun CategoryCard(
    category: String,
    apps: List<AppUsageInfo>,
    onAppClick: (AppUsageInfo) -> Unit,
    onDragEnter : (draggingApp :AppUsageInfo,targetDrop : Any) -> Unit,
    modifier: Modifier = Modifier,
    reorderState: ReorderState<AppUsageInfo>
) {
    val isDropTarget = reorderState.hoveredDropTargetKey == category
    Card(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 200.dp, max = 350.dp)
            .shadow(
                elevation = if (isDropTarget) 8.dp else 4.dp,
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = if (isDropTarget) 2.dp else 0.dp,
                color = if (isDropTarget) MaterialTheme.colors.primary else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
            ,
        elevation = if (isDropTarget) 8.dp else 4.dp,
        backgroundColor = if (isDropTarget) MaterialTheme.colors.primary.copy(alpha = 0.1f) else MaterialTheme.colors.surface
    ) {
        Column {
            // Category header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colors.primary)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Folder,
                    contentDescription = category,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    category,
                    style = MaterialTheme.typography.h6,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "(${apps.size})",
                    style = MaterialTheme.typography.caption,
                    color = Color.White
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(8.dp)
            ) {
                items(apps, key = { app -> app.appName }) { app ->
                    ReorderableItem(
                        modifier = Modifier.dropTarget(
                            state = reorderState.dndState,
                            key = app.appName,
                            onDrop = { state ->

                            },
                            onDragEnter = { state->

                            }
                        ),
                        state = reorderState,
                        key = app.appName,
                        data = app,
                        dropStrategy = DropStrategy.Surface,
                        onDragEnter = { state ->
                            onDragEnter(state.data, app)
                        },
                    ) {
                        EnhancedAppItem(
                            app = app,
                            onClick = { onAppClick(app) },
                            modifier = Modifier.padding(vertical = 4.dp).graphicsLayer {
                                alpha = if (isDragging) 0f else 1f
                            }
                        )
                    }
                }
            }
        }
    }
}