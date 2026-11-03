package org.example.air_workshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.dp
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.example.air_workshop.model.Expense
import org.example.air_workshop.model.Tag
import kotlin.math.absoluteValue

@Composable
fun ExpenseItemRow(
    expense: Expense,
    tag: Tag?,
    currencyCode: String,
    onClick: () -> Unit,
    onSwipeToDelete: () -> Unit,
    onSwipeToEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    var swipeOffset by remember { mutableFloatStateOf(0f) }
    val maxSwipe = 100f

    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        // Background swipe actions
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
        ) {
            // Delete action (swipe left)
            IconButton(
                onClick = onSwipeToDelete,
                modifier = Modifier.align(Alignment.CenterStart).padding(start = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.error
                )
            }

            // Edit action (swipe right)
            IconButton(
                onClick = onSwipeToEdit,
                modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Main content card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .layout { measurable, constraints ->
                    val placeable = measurable.measure(constraints)
                    layout(placeable.width, placeable.height) {
                        placeable.place(swipeOffset.toInt(), 0)
                    }
                }
                .pointerInput(Unit) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (swipeOffset.absoluteValue > maxSwipe / 2) {
                                if (swipeOffset > 0) {
                                    swipeOffset = maxSwipe
                                } else {
                                    swipeOffset = -maxSwipe
                                }
                            } else {
                                swipeOffset = 0f
                            }
                        },
                        onHorizontalDrag = { _, dragAmount ->
                            swipeOffset = swipeOffset.coerceIn(-maxSwipe, maxSwipe) + dragAmount
                            if (swipeOffset == -maxSwipe) {
                                onSwipeToDelete()
                                swipeOffset = 0f
                            } else if (swipeOffset == maxSwipe) {
                                onSwipeToEdit()
                                swipeOffset = 0f
                            }
                        }
                    )
                },
            onClick = onClick,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Tag color indicator
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = tag?.let { hexToColor(it.colorHex) } ?: Color.Gray,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = tag?.name?.first()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Expense details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = expense.note.ifBlank { tag?.name ?: "Expense" },
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1
                        )
                        Text(
                            text = formatCurrency(expense.amount, expense.currencyCode),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatDate(expense.timestamp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (expense.isRecurring) {
                            AssistChip(
                                onClick = { },
                                label = {
                                    Text(
                                        text = "Recurring",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(timestamp: Instant): String {
    val localDateTime = timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
    val day = localDateTime.day
    val month = localDateTime.month.name.lowercase().replaceFirstChar { it.uppercase() }
    val year = localDateTime.year
    val hour = localDateTime.hour.toString().padStart(2, '0')
    val minute = localDateTime.minute.toString().padStart(2, '0')
    return "$day $month $year • $hour:$minute"
}

private fun hexToColor(hex: String): Color {
    return try {
        val hex = hex.trimStart('#')
        val colorLong = hex.toLong(16)
        Color(
            red = ((colorLong shr 16) and 0xFF).toFloat() / 255f,
            green = ((colorLong shr 8) and 0xFF).toFloat() / 255f,
            blue = (colorLong and 0xFF).toFloat() / 255f,
            alpha = if (hex.length == 8) ((colorLong shr 24) and 0xFF).toFloat() / 255f else 1f
        )
    } catch (e: Exception) {
        Color.Gray
    }
}
