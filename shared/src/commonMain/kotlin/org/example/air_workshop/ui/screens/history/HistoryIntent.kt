package org.example.air_workshop.ui.screens.history

import org.example.air_workshop.model.Expense

sealed interface HistoryIntent {
    data class SearchQueryChanged(val query: String) : HistoryIntent
    data class TagFilterToggled(val tagId: Long) : HistoryIntent
    data class DeleteExpense(val expense: Expense) : HistoryIntent
    data object Refresh : HistoryIntent
    data object ClearFilters : HistoryIntent
}

sealed interface HistoryEffect {
    data class NavigateToEditor(val expenseId: Long) : HistoryEffect
    data class ShowSnackbar(val message: String) : HistoryEffect
}
