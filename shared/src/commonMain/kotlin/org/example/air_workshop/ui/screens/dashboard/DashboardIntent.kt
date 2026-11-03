package org.example.air_workshop.ui.screens.dashboard

sealed interface DashboardIntent {
    data class SelectMonth(val year: Int, val month: Int) : DashboardIntent
    data object PreviousMonth : DashboardIntent
    data object NextMonth : DashboardIntent
    data object JumpToCurrentMonth : DashboardIntent
    data class DeleteExpense(val expense: org.example.air_workshop.model.Expense) : DashboardIntent
    data object Refresh : DashboardIntent
}

sealed interface DashboardEffect {
    data class NavigateToExpenseEntry(val editExpense: org.example.air_workshop.model.Expense? = null) : DashboardEffect
    data class ShowSnackbar(val message: String) : DashboardEffect
}
