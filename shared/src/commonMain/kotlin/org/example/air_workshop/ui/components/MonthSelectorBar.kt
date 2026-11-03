package org.example.air_workshop.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlin.time.Clock
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val MONTH_NAMES = listOf(
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
)

@Composable
fun MonthSelectorBar(
    selectedYear: Int,
    selectedMonth: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onJumpToCurrentMonth: () -> Unit,
    isCurrentMonth: Boolean,
    modifier: Modifier = Modifier
) {
    val monthName = if (selectedMonth in 1..12) MONTH_NAMES[selectedMonth - 1] else ""
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "Previous month"
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$monthName $selectedYear",
                style = MaterialTheme.typography.titleLarge
            )
            if (!isCurrentMonth) {
                TextButton(onClick = onJumpToCurrentMonth) {
                    Text("Today", style = MaterialTheme.typography.labelMedium)
                }
            }
        }

        IconButton(onClick = onNextMonth) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Next month"
            )
        }
    }
}

fun previousMonth(year: Int, month: Int): Pair<Int, Int> {
    return if (month == 1) year - 1 to 12 else year to month - 1
}

fun nextMonth(year: Int, month: Int): Pair<Int, Int> {
    return if (month == 12) year + 1 to 1 else year to month + 1
}

fun currentMonth(): Pair<Int, Int> {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return now.year to now.month.ordinal + 1
}

fun isCurrentMonth(year: Int, month: Int): Boolean {
    val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    return now.year == year && now.month.ordinal + 1 == month
}
