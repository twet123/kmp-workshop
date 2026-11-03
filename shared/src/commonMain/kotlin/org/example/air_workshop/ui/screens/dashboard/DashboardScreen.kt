package org.example.air_workshop.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.air_workshop.ui.components.*
import org.example.air_workshop.ui.theme.AppTheme
import org.koin.compose.koinInject

@Composable
fun DashboardScreen(
    store: DashboardStore = koinInject(),
    onNavigateToExpenseEntry: (editExpenseId: Long?) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by store.stateFlow.collectAsState()
    val effectCollector = remember(store) { store.effects }
    
    LaunchedEffect(Unit) {
        effectCollector.collect { effect ->
            when (effect) {
                is DashboardEffect.NavigateToExpenseEntry -> {
                    onNavigateToExpenseEntry(effect.editExpense?.id ?: -1L)
                }
                is DashboardEffect.ShowSnackbar -> {
                    // Snackbar handling would be done by the parent in a real app
                }
            }
        }
    }

    AppTheme {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { store.onIntent(DashboardIntent.Refresh) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add expense"
                    )
                }
            },
            modifier = modifier.fillMaxSize()
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MonthSelectorBar(
                    selectedYear = state.year,
                    selectedMonth = state.month,
                    onPreviousMonth = { store.onIntent(DashboardIntent.PreviousMonth) },
                    onNextMonth = { store.onIntent(DashboardIntent.NextMonth) },
                    onJumpToCurrentMonth = { store.onIntent(DashboardIntent.JumpToCurrentMonth) },
                    isCurrentMonth = isCurrentMonth(state.year, state.month),
                    modifier = Modifier.padding(top = 8.dp)
                )

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
                                Button(onClick = { store.onIntent(DashboardIntent.Refresh) }) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            // Goal progress
                            item {
                                GoalProgressBar(
                                    progress = state.goalProgress,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Tag breakdown chart
                            item {
                                TagExpenseChart(
                                    breakdown = state.tagBreakdown,
                                    currencyCode = state.currencyCode,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Section header
                            item {
                                Text(
                                    text = "Recent Transactions",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }

                            // Expense list
                            items(state.expenses) { expense ->
                                val tag = state.tags[expense.tagId]
                                Spacer(modifier = Modifier.height(4.dp))
                                ExpenseItemRow(
                                    expense = expense,
                                    tag = tag,
                                    currencyCode = state.currencyCode,
                                    onClick = {
                                        // Navigate to edit
                                        onNavigateToExpenseEntry(expense.id)
                                    },
                                    onSwipeToDelete = {
                                        store.onIntent(DashboardIntent.DeleteExpense(expense))
                                    },
                                    onSwipeToEdit = {
                                        onNavigateToExpenseEntry(expense.id)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Empty state
                            if (state.expenses.isEmpty()) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = androidx.compose.ui.Alignment.Center
                                    ) {
                                        Column(
                                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = "No expenses yet",
                                                style = MaterialTheme.typography.bodyLarge,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Tap + to add your first expense",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
    }
}
