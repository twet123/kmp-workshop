package org.example.air_workshop.ui.components

import androidx.compose.foundation.background
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
import org.example.air_workshop.model.Tag

@Composable
fun TagChip(
    tag: Tag,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = hexToColor(tag.colorHex)
    
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = tagColor,
                            shape = CircleShape
                        )
                        .padding(end = 4.dp)
                )
                Text(text = tag.name)
            }
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = tagColor.copy(alpha = 0.2f),
            selectedLabelColor = tagColor,
            labelColor = MaterialTheme.colorScheme.onSurface,
            containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ),
        modifier = modifier.padding(end = 8.dp)
    )
}

@Composable
fun TagPicker(
    tags: List<Tag>,
    selectedTagId: Long?,
    onTagSelected: (Tag) -> Unit,
    modifier: Modifier = Modifier
) {
    if (tags.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No tags available. Create tags in Settings.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(tags) { tag ->
            TagChip(
                tag = tag,
                selected = tag.id == selectedTagId,
                onClick = { onTagSelected(tag) }
            )
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
