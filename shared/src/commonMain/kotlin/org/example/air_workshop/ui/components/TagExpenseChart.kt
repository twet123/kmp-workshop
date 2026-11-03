package org.example.air_workshop.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import org.example.air_workshop.model.Currency
import org.example.air_workshop.usecases.TagBreakdown
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TagExpenseChart(
    breakdown: List<TagBreakdown>,
    currencyCode: String,
    modifier: Modifier = Modifier
) {
    if (breakdown.isEmpty()) {
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
                    text = "No expenses recorded for this month",
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
            Text(
                text = "Spending by Category",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Donut chart
            val colors = remember(breakdown) {
                breakdown.map { hexToColor(it.tag.colorHex) }
            }
            
            val total = breakdown.sumOf { it.totalAmount }
            val currency = Currency.fromCode(currencyCode)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(
                    data = breakdown.map { it.totalAmount },
                    colors = colors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                )
                
                // Center text
                Text(
                    text = formatCurrency(total, currency, decimals = 0),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Legend
            LazyColumn {
                items(breakdown) { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .padding(end = 8.dp)
                                    .background(
                                        color = hexToColor(item.tag.colorHex),
                                        shape = MaterialTheme.shapes.small
                                    )
                            )
                            Text(
                                text = item.tag.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Row {
                            Text(
                                text = formatCurrency(item.totalAmount, currency),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text(
                                text = "${item.percentage.toInt()}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DonutChart(
    data: List<Double>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it }
    if (total == 0.0 || data.isEmpty()) return

    val density = LocalDensity.current
    val radiusPx = with(density) { 70.dp.toPx() }
    val strokeWidthPx = with(density) { 24.dp.toPx() }

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2, size.height / 2)
        var startAngle = -90f

        data.forEachIndexed { index, value ->
            val sweepAngle = ((value / total) * 360f).toFloat()
            val color = colors.getOrNull(index) ?: Color.Gray
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = strokeWidthPx),
                topLeft = Offset(
                    center.x - radiusPx,
                    center.y - radiusPx
                ),
                size = Size(radiusPx * 2, radiusPx * 2)
            )
            startAngle = startAngle + sweepAngle - 2f // small gap between segments
        }
    }
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
