package org.example.air_workshop.ui.screens.expenseentry

import org.example.air_workshop.model.Currency
import org.example.air_workshop.model.Expense
import org.example.air_workshop.model.Tag

data class ExpenseEntryState(
    val amount: String = "",
    val selectedTagId: Long? = null,
    val selectedCurrency: Currency = Currency.EUR,
    val selectedDate: String = "",
    val note: String = "",
    val isRecurring: Boolean = false,
    val availableTags: List<Tag> = emptyList(),
    val isEditMode: Boolean = false,
    val editingExpense: Expense? = null,
    val isLoading: Boolean = false,
    val validationError: String? = null
) {
    val isFormValid: Boolean
        get() {
            val amountNum = amount.toDoubleOrNull()
            return amountNum != null && amountNum > 0 && selectedTagId != null
        }
}
