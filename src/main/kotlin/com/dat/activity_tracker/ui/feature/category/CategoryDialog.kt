package com.dat.activity_tracker.ui.feature.category

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Category selection dialog component with drag and drop support
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun CategoryDialog(
    showDialog: Boolean,
    selectedApp: String,
    currentCategory: String,
    categories: List<String>,
    onDismiss: () -> Unit,
    onCategorySelected: (String, String) -> Unit
) {
    if (showDialog) {
        // State for tracking which category is being dragged
        var draggedCategoryIndex by remember { mutableIntStateOf(-1) }
        // State for tracking hover target during drag
        var hoverCategoryIndex by remember { mutableIntStateOf(-1) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Change Category for: $selectedApp") },
            text = {
                Column {
                    Text("Current category: $currentCategory")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Select new category or drag to reorder:")
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyColumn(
                        modifier = Modifier.height(200.dp)
                    ) {
                        items(categories) { category ->
                            val index = categories.indexOf(category)
                            val isBeingDragged = index == draggedCategoryIndex
                            val isHoverTarget = index == hoverCategoryIndex && draggedCategoryIndex != -1

                            // Category item with drag support
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        when {
                                            isHoverTarget -> MaterialTheme.colors.primary.copy(alpha = 0.2f)
                                            isBeingDragged -> MaterialTheme.colors.primary.copy(alpha = 0.1f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .clickable {
                                        onCategorySelected(selectedApp, category)
                                    }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Category selection indicator
                                RadioButton(
                                    selected = category == currentCategory,
                                    onClick = null
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Make the category text draggable
                                AnimatedVisibility(
                                    visible = true, // Always visible, but with animation support
                                    enter = fadeIn() + scaleIn(),
                                    exit = fadeOut() + scaleOut()
                                ) {
                                    Text(
                                        text = category,
                                        modifier = Modifier
                                    )
                                }
                            }
                        }
                    }

                    // Instructions for drag and drop
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Tip: Drag categories or apps to reorder",
                        style = MaterialTheme.typography.caption,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onDismiss
                ) {
                    Text("Cancel")
                }
            },
            dismissButton = null
        )
    }
}