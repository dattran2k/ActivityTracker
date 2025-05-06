package com.dat.activity_tracker.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Report generation buttons component
 */
@Composable
fun ReportButtons(
    onGenerateDaily: () -> Unit,
    onGenerateWeekly: () -> Unit,
    onGenerateMonthly: () -> Unit
) {
    Card(
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Generate Reports", style = MaterialTheme.typography.h6)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onGenerateDaily) {
                    Icon(Icons.Default.Today, contentDescription = "Daily report")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Daily Report")
                }

                Button(onClick = onGenerateWeekly) {
                    Icon(Icons.Default.DateRange, contentDescription = "Weekly report")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Weekly Report")
                }

                Button(onClick = onGenerateMonthly) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Monthly report")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Monthly Report")
                }
            }
        }
    }
}