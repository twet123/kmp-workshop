package org.example.air_workshop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.example.air_workshop.usecases.BudgetStatus
import org.example.air_workshop.usecases.GoalProgress

@Composable
fun GoalProgressBar(
    progress: GoalProgress?,
    modifier: Modifier = Modifier
) {
    if (progress == null || !progress.hasGoal) {
        Card(
            modifier = modifier.padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No budget goal set for this month",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        return
    }

    Card(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monthly Budget",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${progress.percentage.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = getProgressColor(progress.status)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { (progress.percentage.coerceAtMost(100.0) / 100.0).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = getProgressColor(progress.status),
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                gapSize = 0.dp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Spent",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(progress.spentAmount, progress.currencyCode),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Remaining",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(progress.remainingAmount, progress.currencyCode),
                        style = MaterialTheme.typography.titleMedium,
                        color = if (progress.remainingAmount >= 0) 
                            MaterialTheme.colorScheme.onSurface 
                        else 
                            MaterialTheme.colorScheme.error
                    )
                }
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Goal",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatCurrency(progress.targetAmount, progress.currencyCode),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@Composable
private fun getProgressColor(status: BudgetStatus): Color {
    return when (status) {
        BudgetStatus.NORMAL -> MaterialTheme.colorScheme.primary
        BudgetStatus.WARNING -> Color(0xFFFFA000) // Amber
        BudgetStatus.EXCEEDED -> MaterialTheme.colorScheme.error
    }
}
