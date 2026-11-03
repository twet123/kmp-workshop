package org.example.air_workshop.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.air_workshop.model.Expense
import org.example.air_workshop.ui.components.*
import org.example.air_workshop.ui.theme.AppTheme
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.koinInject

@Composable
fun HistoryScreen(
    store: HistoryStore = koinInject(),
    onNavigateToEditor: (expenseId: Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by store.stateFlow.collectAsState()
    val effectCollector = remember(store) { store.effects }

    LaunchedEffect(Unit) {
        effectCollector.collect { effect ->
            when (effect) {
                is HistoryEffect.NavigateToEditor -> onNavigateToEditor(effect.expenseId)
                is HistoryEffect.ShowSnackbar -> {
                    // Snackbar handling by parent
                }
            }
        }
    }

    AppTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Transaction History") },
                    actions = {
                        if (state.searchQuery.isNotBlank() || state.selectedTagIds.isNotEmpty()) {
                            IconButton(onClick = { store.onIntent(HistoryIntent.ClearFilters) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear filters"
                                )
                            }
                        }
                    }
                )
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Search bar
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { store.onIntent(HistoryIntent.SearchQueryChanged(it)) },
                    placeholder = { Text("Search expenses...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    },
                    trailingIcon = {
                        if (state.searchQuery.isNotBlank()) {
                            IconButton(onClick = { store.onIntent(HistoryIntent.SearchQueryChanged("")) }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Tag filter chips
                if (state.availableTags.isNotEmpty()) {
                    TagPicker(
                        tags = state.availableTags,
                        selectedTagId = null,
                        onTagSelected = { tag ->
                            store.onIntent(HistoryIntent.TagFilterToggled(tag.id))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Show active filter indicators
                    if (state.selectedTagIds.isNotEmpty()) {
                        val activeTags = state.selectedTagIds.mapNotNull { id -> state.tags[id] }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            activeTags.forEach { tag ->
                                AssistChip(
                                    onClick = { store.onIntent(HistoryIntent.TagFilterToggled(tag.id)) },
                                    label = { Text(tag.name) },
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                        }
                    }
                }

                when {
                    state.isLoading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    state.error != null -> {
                        val errorMsg = state.error ?: ""
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Error loading data",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMsg,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { store.onIntent(HistoryIntent.Refresh) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    state.expenses.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = androidx.compose.ui.Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (state.searchQuery.isNotBlank() || state.selectedTagIds.isNotEmpty())
                                        "No matching expenses"
                                    else
                                        "No expenses recorded",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    else -> {
                        val grouped = state.expenses.groupBy { expense ->
                            val local = expense.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
                            "${local.year}-${local.month.number.toString().padStart(2, '0')}-${local.day.toString().padStart(2, '0')}"
                        }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            grouped.forEach { (date, expensesForDate) ->
                                // Sticky date header
                                stickyHeader {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceContainer,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = formatDateHeader(date),
                                            style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(16.dp),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                items(expensesForDate, key = { it.id }) { expense ->
                                    val tag = state.tags[expense.tagId]
                                    Spacer(modifier = Modifier.height(4.dp))
                                    ExpenseItemRow(
                                        expense = expense,
                                        tag = tag,
                                        currencyCode = state.currencyCode,
                                        onClick = { onNavigateToEditor(expense.id) },
                                        onSwipeToDelete = {
                                            store.onIntent(HistoryIntent.DeleteExpense(expense))
                                        },
                                        onSwipeToEdit = { onNavigateToEditor(expense.id) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDateHeader(dateStr: String): String {
    return try {
        val parts = dateStr.split("-")
        val year = parts[0]
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        val monthNames = listOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        val monthName = if (month in 1..12) monthNames[month - 1] else ""
        "$day $monthName $year"
    } catch (e: Exception) {
        dateStr
    }
}
