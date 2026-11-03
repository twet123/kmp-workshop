package org.example.air_workshop.ui.screens.expenseentry

import org.example.air_workshop.model.Currency
import org.example.air_workshop.model.Expense

sealed interface ExpenseEntryIntent {
    data class UpdateAmount(val amount: String) : ExpenseEntryIntent
    data class SelectTag(val tagId: Long) : ExpenseEntryIntent
    data class SelectCurrency(val currency: Currency) : ExpenseEntryIntent
    data class SelectDate(val date: String) : ExpenseEntryIntent
    data class UpdateNote(val note: String) : ExpenseEntryIntent
    data class ToggleRecurring(val isRecurring: Boolean) : ExpenseEntryIntent
    data object Submit : ExpenseEntryIntent
    data object Delete : ExpenseEntryIntent
    data object ClearValidationError : ExpenseEntryIntent
}

sealed interface ExpenseEntryEffect {
    data object DismissScreen : ExpenseEntryEffect
    data class ShowValidationToast(val message: String) : ExpenseEntryEffect
    data class ShowSuccessToast(val message: String) : ExpenseEntryEffect
}
