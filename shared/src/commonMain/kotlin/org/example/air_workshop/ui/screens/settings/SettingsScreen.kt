package org.example.air_workshop.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.air_workshop.model.Currency
import org.example.air_workshop.ui.components.*
import org.example.air_workshop.ui.theme.AppTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    store: SettingsStore = koinInject(),
    onCsvExport: (csvContent: String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by store.stateFlow.collectAsState()
    val effectCollector = remember(store) { store.effects }

    LaunchedEffect(Unit) {
        effectCollector.collect { effect ->
            when (effect) {
                is SettingsEffect.LaunchShareSheet -> onCsvExport(effect.csvContent)
                is SettingsEffect.ShowNotification -> {
                    // Notification handling by parent
                }
            }
        }
    }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Settings") })
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            when {
                state.isLoading && state.tags.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = androidx.compose.ui.Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Budget goal section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Monthly Budget Goal",
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = state.currentGoal?.let {
                                                "${Currency.fromCode(it.currencyCode).symbol}${"%.2f".format(it.targetAmount)}"
                                            } ?: "Not set",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    IconButton(
                                        onClick = { store.onIntent(SettingsIntent.OpenGoalDialog) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit goal"
                                        )
                                    }
                                }
                            }
                        }

                        // Currency section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Base Currency",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                ExposedDropdownMenuBox(
                                    expanded = false,
                                    onExpandedChange = {}
                                ) {
                                    OutlinedTextField(
                                        value = state.baseCurrency.displayName,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor()
                                    )
                                }
                            }
                        }

                        // Tags section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Categories",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    TextButton(
                                        onClick = { store.onIntent(SettingsIntent.OpenNewTagDialog()) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add category"
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Add")
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                if (state.tags.isEmpty()) {
                                    Text(
                                        text = "No categories yet. Add one to get started.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    state.tags.forEach { tag ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(16.dp)
                                                        .background(
                                                            color = hexToColor(tag.colorHex),
                                                            shape = MaterialTheme.shapes.small
                                                        )
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = tag.name,
                                                    style = MaterialTheme.typography.bodyLarge
                                                )
                                            }
                                            Row {
                                                IconButton(
                                                    onClick = { store.onIntent(SettingsIntent.OpenNewTagDialog(tag)) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Edit,
                                                        contentDescription = "Edit",
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                IconButton(
                                                    onClick = { store.onIntent(SettingsIntent.DeleteTag(tag)) }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Delete",
                                                        tint = MaterialTheme.colorScheme.error
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Export section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Data Export",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = "Export your expenses as CSV",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                Button(
                                    onClick = {
                                        val (year, month) = currentMonth()
                                        store.onIntent(
                                            SettingsIntent.TriggerCsvExport(
                                                year.toString(),
                                                month.toString().padStart(2, '0')
                                            )
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Export"
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Export Current Month")
                                }
                            }
                        }
                    }
                }
            }

            // Goal dialog
            if (state.isGoalDialogOpen) {
                AlertDialog(
                    onDismissRequest = { store.onIntent(SettingsIntent.CloseGoalDialog) },
                    title = { Text("Set Monthly Budget") },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = state.goalAmount,
                                onValueChange = { store.onIntent(SettingsIntent.UpdateGoalAmount(it)) },
                                label = { Text("Amount") },
                                placeholder = { Text("1000.00") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                store.onIntent(
                                    SettingsIntent.UpdateMonthlyGoal(
                                        state.goalAmount,
                                        state.goalMonthYear
                                    )
                                )
                            },
                            enabled = state.goalAmount.toDoubleOrNull() != null &&
                                    state.goalAmount.toDoubleOrNull()!! > 0
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { store.onIntent(SettingsIntent.CloseGoalDialog) }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Tag dialog
            if (state.isNewTagDialogOpen) {
                AlertDialog(
                    onDismissRequest = { store.onIntent(SettingsIntent.CloseNewTagDialog) },
                    title = {
                        Text(
                            text = if (state.editingTag != null) "Edit Category" else "New Category"
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = state.newTagName,
                                onValueChange = { store.onIntent(SettingsIntent.UpdateNewTagName(it)) },
                                label = { Text("Name") },
                                placeholder = { Text("e.g., Groceries") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Text(
                                text = "Color",
                                style = MaterialTheme.typography.labelMedium
                            )
                            ColorPickerRow(
                                selectedColor = state.newTagColor,
                                onColorSelected = { store.onIntent(SettingsIntent.UpdateNewTagColor(it)) }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (state.editingTag != null) {
                                    store.onIntent(SettingsIntent.UpdateTag)
                                } else {
                                    store.onIntent(SettingsIntent.CreateTag)
                                }
                            },
                            enabled = state.newTagName.isNotBlank()
                        ) {
                            Text(
                                text = if (state.editingTag != null) "Update" else "Create"
                            )
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { store.onIntent(SettingsIntent.CloseNewTagDialog) }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}

private fun hexToColor(hex: String): androidx.compose.ui.graphics.Color {
    return try {
        val hex = hex.trimStart('#')
        val colorLong = hex.toLong(16)
        androidx.compose.ui.graphics.Color(
            red = ((colorLong shr 16) and 0xFF).toFloat() / 255f,
            green = ((colorLong shr 8) and 0xFF).toFloat() / 255f,
            blue = (colorLong and 0xFF).toFloat() / 255f,
            alpha = if (hex.length == 8) ((colorLong shr 24) and 0xFF).toFloat() / 255f else 1f
        )
    } catch (e: Exception) {
        androidx.compose.ui.graphics.Color.Gray
    }
}
