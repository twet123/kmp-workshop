package org.example.air_workshop.ui.screens.expenseentry

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.air_workshop.model.Expense
import org.example.air_workshop.model.Currency
import org.example.air_workshop.ui.components.TagChip
import org.example.air_workshop.ui.components.TagPicker
import org.example.air_workshop.ui.theme.AppTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseEntryScreen(
    store: ExpenseEntryStore = koinInject(),
    editExpense: Expense? = null,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by store.stateFlow.collectAsState()
    val effectCollector = remember(store) { store.effects }

    // Populate form if editing
    LaunchedEffect(editExpense) {
        editExpense?.let { store.setExpenseToEdit(it) }
    }

    LaunchedEffect(Unit) {
        effectCollector.collect { effect ->
            when (effect) {
                is ExpenseEntryEffect.DismissScreen -> onDismiss()
                is ExpenseEntryEffect.ShowValidationToast -> {
                    // Toast handling by parent
                }
                is ExpenseEntryEffect.ShowSuccessToast -> {
                    // Toast handling by parent
                }
            }
        }
    }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = if (state.isEditMode) "Edit Expense" else "New Expense"
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (state.isEditMode) {
                            IconButton(onClick = { store.onIntent(ExpenseEntryIntent.Delete) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                if (state.isFormValid && !state.isLoading) {
                    FloatingActionButton(
                        onClick = { store.onIntent(ExpenseEntryIntent.Submit) }
                    ) {
                        if (state.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Save")
                        }
                    }
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Amount field
                OutlinedTextField(
                    value = state.amount,
                    onValueChange = { store.onIntent(ExpenseEntryIntent.UpdateAmount(it)) },
                    label = { Text("Amount") },
                    placeholder = { Text("0.00") },
                    leadingIcon = {
                        Text(
                            text = state.selectedCurrency.symbol,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    isError = state.validationError != null && state.amount.isNotBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Currency selector
                ExposedDropdownMenuBox(
                    expanded = false,
                    onExpandedChange = {}
                ) {
                    OutlinedTextField(
                        value = state.selectedCurrency.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Currency") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = false)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                }

                // Tag picker
                Column {
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    if (state.availableTags.isEmpty()) {
                        Text(
                            text = "No categories available. Add one in Settings.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        TagPicker(
                            tags = state.availableTags,
                            selectedTagId = state.selectedTagId,
                            onTagSelected = { tag ->
                                store.onIntent(ExpenseEntryIntent.SelectTag(tag.id))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Date field
                OutlinedTextField(
                    value = state.selectedDate,
                    onValueChange = { store.onIntent(ExpenseEntryIntent.SelectDate(it)) },
                    label = { Text("Date") },
                    placeholder = { Text("yyyy-MM-dd") },
                    isError = state.validationError != null && state.selectedDate.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Note field
                OutlinedTextField(
                    value = state.note,
                    onValueChange = { store.onIntent(ExpenseEntryIntent.UpdateNote(it)) },
                    label = { Text("Note (optional)") },
                    placeholder = { Text("What was this expense for?") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )

                // Recurring toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Recurring",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Text(
                            text = "Auto-generate next month",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = state.isRecurring,
                        onCheckedChange = { store.onIntent(ExpenseEntryIntent.ToggleRecurring(it)) }
                    )
                }

                // Validation error
                state.validationError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
