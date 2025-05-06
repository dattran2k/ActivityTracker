package com.dat.activity_tracker.ui.feature.category

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dat.activity_tracker.domain.model.AppTimelineEntry
import com.dat.activity_tracker.domain.model.AppUsageInfo
import com.dat.activity_tracker.util.formatAppName
import com.mohamedrejeb.compose.dnd.reorder.ReorderContainer
import com.mohamedrejeb.compose.dnd.reorder.rememberReorderState

/**
 * Categories Tab - Fullscreen UI with grid-based categories, drag-and-drop, app details
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoriesTab(
    isLoading: Boolean,
    appUsageData: List<AppUsageInfo>,
    onRefresh: () -> Unit,
    onChangeCategory: (String, String) -> Unit,
    onCreateCategory: (String) -> Unit,
    onMoveAppToCategory: (String, String) -> Unit,
    appTimelineData: Map<String, List<AppTimelineEntry>> = emptyMap()
) {
    // State
    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var selectedApp by remember { mutableStateOf<AppUsageInfo?>(null) }
    // Drag and drop state
    var draggedApp by remember { mutableStateOf<AppUsageInfo?>(null) }
    var showAddCategoryPlaceholder by remember { mutableStateOf(false) }
    val reorderState = rememberReorderState<AppUsageInfo>()
    // Application state
    val groupedByCategory = appUsageData.groupBy { it.category }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with refresh button and new category button
        CategoryHeader({ show ->
            showNewCategoryDialog = show
        }, onRefresh, isLoading)

        // Loading indicator or content
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(48.dp))
            }
        } else {
            // Show Add Category placeholder during drag
            LaunchedEffect(reorderState.draggedItem) {
                showAddCategoryPlaceholder = reorderState.draggedItem != null
            }
            // Categories grid
            ReorderContainer(state = reorderState) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 300.dp),
                    modifier = Modifier.fillMaxSize().padding(8.dp),
                    contentPadding = PaddingValues(8.dp)
                ) {
                    items(groupedByCategory.keys.toList()) { category ->
                        CategoryCard(
                            category = category,
                            apps = groupedByCategory[category] ?: emptyList(),
                            onAppClick = { app ->
                                selectedApp = app
                            },
                            onDragEnter = { app,app2 ->
                                println("onDragEnter $app")
                            },
                            modifier = Modifier.padding(8.dp),
                            reorderState = reorderState
                        )
                    }

                }
            }
        }
    }

    // Render dialogs on top
    if (showNewCategoryDialog) {
        NewCategoryDialog(
            onDismiss = {
                showNewCategoryDialog = false
            },
            onCreateCategory = { categoryName ->
                onCreateCategory(categoryName)
                // If we have a dragged app, move it to the new category
                if (draggedApp != null) {
                    onMoveAppToCategory(draggedApp!!.appName, categoryName)
                }
                showNewCategoryDialog = false
            },
            draggedAppName = draggedApp?.appName
        )
    }

    if (selectedApp != null) {
        AppDetailsDialog(
            app = selectedApp!!,
            timelineData = appTimelineData[selectedApp!!.appName] ?: emptyList(),
            onDismiss = { selectedApp = null },
            onChangeCategory = { app, category ->
                onChangeCategory(app, category)
                selectedApp = null
            }
        )
    }
}

@Composable
private fun CategoryHeader(
    onShowCategoryDialog: (Boolean) -> Unit,
    onRefresh: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "App Categories",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )

            // Help text for drag-drop
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    Icons.Default.DragIndicator,
                    contentDescription = "Drag Indicator",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colors.primary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    "Kéo các ứng dụng để sắp xếp vào danh mục",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                )
            }
        }

        Row {
            Button(
                onClick = { onShowCategoryDialog(true) },
                modifier = Modifier.padding(end = 8.dp),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.secondary)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add category")
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Category")
            }

            Button(
                onClick = onRefresh,
                enabled = !isLoading
            ) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Refresh")
            }
        }
    }
}


/**
 * Dialog for creating a new category
 */
@Composable
fun NewCategoryDialog(
    onDismiss: () -> Unit,
    onCreateCategory: (String) -> Unit,
    draggedAppName: String? = null
) {
    var categoryName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Category") },
        text = {
            Column {
                // Show which app will be moved to this category if there's a dragged app
                if (draggedAppName != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colors.primary.copy(alpha = 0.1f))
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Apps,
                            contentDescription = null,
                            tint = MaterialTheme.colors.primary,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(Modifier.width(8.dp))

                        Column {
                            Text(
                                "Moving app to new category:",
                                style = MaterialTheme.typography.caption,
                                color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                formatAppName(draggedAppName),
                                style = MaterialTheme.typography.subtitle1,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Text("Enter a name for the new category:")
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = categoryName,
                    onValueChange = { categoryName = it },
                    label = { Text("Category Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreateCategory(categoryName) },
                enabled = categoryName.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


