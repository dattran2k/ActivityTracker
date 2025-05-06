package com.dat.activity_tracker.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Status bar component displayed at the bottom of the application
 */
@Composable
fun StatusBar(statusMessage: String) {
    Surface(
        color = MaterialTheme.colors.primaryVariant,
        modifier = Modifier.fillMaxWidth().height(24.dp)
    ) {
        Box(
            contentAlignment = Alignment.CenterStart,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = statusMessage,
                style = MaterialTheme.typography.caption,
                color = MaterialTheme.colors.onPrimary
            )
        }
    }
}