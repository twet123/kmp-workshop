package org.example.air_workshop.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val DEFAULT_COLOR_PALETTE = listOf(
    "#6B5CE7", // Purple
    "#FF6B6B", // Red
    "#4ECDC4", // Teal
    "#45B7D1", // Blue
    "#96CEB4", // Green
    "#FFEAA7", // Yellow
    "#DDA0DD", // Plum
    "#FF8C69", // Coral
    "#98D8C8", // Mint
    "#F7DC6F", // Gold
    "#BB8FCE", // Lavender
    "#85C1E9", // Sky Blue
    "#82E0AA", // Light Green
    "#F1948A", // Salmon
    "#AED6F1", // Ice Blue
    "#D7BDE2", // Orchid
    "#A3E4D7", // Aqua
    "#FAD7A0", // Peach
    "#A9CCE3", // Periwinkle
    "#D5F5E3"  // Pale Green
)

@Composable
fun ColorPickerRow(
    selectedColor: String?,
    onColorSelected: (String) -> Unit,
    colors: List<String> = DEFAULT_COLOR_PALETTE,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(colors) { colorHex ->
            ColorPickerItem(
                colorHex = colorHex,
                isSelected = colorHex == selectedColor,
                onClick = { onColorSelected(colorHex) }
            )
        }
    }
}

@Composable
private fun ColorPickerItem(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = hexToColor(colorHex)
    
    Box(
        modifier = modifier
            .size(40.dp)
            .background(color, shape = CircleShape)
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = CircleShape
                    )
                }
            )
            .clickable(onClick = onClick)
    )
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
