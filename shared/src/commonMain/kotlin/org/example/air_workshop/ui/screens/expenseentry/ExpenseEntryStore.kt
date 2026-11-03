package org.example.air_workshop.ui.screens.expenseentry

import kotlinx.coroutines.launch
import org.example.air_workshop.mvi.MviStore
import org.example.air_workshop.model.Expense
import org.example.air_workshop.repository.ExpenseRepository
import org.example.air_workshop.repository.TagRepository
import org.example.air_workshop.repository.SettingsRepository
import org.example.air_workshop.model.Currency
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toInstant

class ExpenseEntryStore(
    private val expenseRepository: ExpenseRepository,
    private val tagRepository: TagRepository,
    private val settingsRepository: SettingsRepository
) : MviStore<ExpenseEntryState, ExpenseEntryIntent, ExpenseEntryEffect>() {

    override fun initialState(): ExpenseEntryState {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val dateStr = "${now.year}-${now.monthNumber.toString().padStart(2, '0')}-${now.day.toString().padStart(2, '0')}"
        return ExpenseEntryState(
            selectedDate = dateStr
        )
    }

    override fun handleStart() {
        loadTags()
        loadDefaultCurrency()
    }

    override fun reduce(intent: ExpenseEntryIntent): ExpenseEntryState? {
        return when (intent) {
            is ExpenseEntryIntent.UpdateAmount -> {
                stateFlow.value.copy(
                    amount = intent.amount,
                    validationError = null
                )
            }
            is ExpenseEntryIntent.SelectTag -> {
                stateFlow.value.copy(
                    selectedTagId = intent.tagId,
                    validationError = null
                )
            }
            is ExpenseEntryIntent.SelectCurrency -> {
                stateFlow.value.copy(
                    selectedCurrency = intent.currency
                )
            }
            is ExpenseEntryIntent.SelectDate -> {
                stateFlow.value.copy(
                    selectedDate = intent.date
                )
            }
            is ExpenseEntryIntent.UpdateNote -> {
                stateFlow.value.copy(
                    note = intent.note
                )
            }
            is ExpenseEntryIntent.ToggleRecurring -> {
                stateFlow.value.copy(
                    isRecurring = intent.isRecurring
                )
            }
            is ExpenseEntryIntent.Submit -> {
                submitExpense()
                stateFlow.value.copy(isLoading = true)
            }
            is ExpenseEntryIntent.Delete -> {
                deleteExpense()
                stateFlow.value.copy(isLoading = true)
            }
            is ExpenseEntryIntent.ClearValidationError -> {
                stateFlow.value.copy(validationError = null)
            }
        }
    }

    private fun loadTags() {
        coroutineScope.launch {
            tagRepository.observeAllTags().collect { tags ->
                setState { copy(availableTags = tags) }
            }
        }
    }

    private fun loadDefaultCurrency() {
        coroutineScope.launch {
            val currency = settingsRepository.getDefaultCurrency()
            setState { copy(selectedCurrency = currency) }
        }
    }

    private fun populateForm(expense: Expense) {
        setState {
            copy(
                amount = "%.2f".format(expense.amount),
                selectedTagId = expense.tagId,
                selectedCurrency = Currency.fromCode(expense.currencyCode),
                note = expense.note,
                isRecurring = expense.isRecurring,
                isEditMode = true,
                editingExpense = expense
            )
        }
    }

    fun setExpenseToEdit(expense: Expense) {
        populateForm(expense)
    }

    private fun submitExpense() {
        val state = stateFlow.value
        val amount = state.amount.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            setState { copy(validationError = "Please enter a valid amount", isLoading = false) }
            sendEffect(ExpenseEntryEffect.ShowValidationToast("Invalid amount"))
            return
        }

        if (state.selectedTagId == null) {
            setState { copy(validationError = "Please select a tag", isLoading = false) }
            sendEffect(ExpenseEntryEffect.ShowValidationToast("Select a tag"))
            return
        }

        coroutineScope.launch {
            try {
                val timestamp = parseDate(state.selectedDate) ?: Clock.System.now()
                
                if (state.isEditMode && state.editingExpense != null) {
                    val updated = state.editingExpense!!.copy(
                        amount = amount,
                        currencyCode = state.selectedCurrency.code,
                        timestamp = timestamp,
                        tagId = state.selectedTagId!!,
                        note = state.note,
                        isRecurring = state.isRecurring
                    )
                    expenseRepository.updateExpense(updated)
                    sendEffect(ExpenseEntryEffect.ShowSuccessToast("Expense updated"))
                } else {
                    val newExpense = Expense(
                        amount = amount,
                        currencyCode = state.selectedCurrency.code,
                        timestamp = timestamp,
                        tagId = state.selectedTagId!!,
                        note = state.note,
                        isRecurring = state.isRecurring
                    )
                    expenseRepository.insertExpense(newExpense)
                    sendEffect(ExpenseEntryEffect.ShowSuccessToast("Expense added"))
                }
                sendEffect(ExpenseEntryEffect.DismissScreen)
            } catch (e: Exception) {
                setState { copy(isLoading = false, validationError = e.message) }
                sendEffect(ExpenseEntryEffect.ShowValidationToast("Failed to save expense"))
            }
        }
    }

    private fun deleteExpense() {
        val state = stateFlow.value
        if (!state.isEditMode || state.editingExpense == null) {
            sendEffect(ExpenseEntryEffect.DismissScreen)
            return
        }

        coroutineScope.launch {
            try {
                expenseRepository.deleteExpense(state.editingExpense!!)
                sendEffect(ExpenseEntryEffect.ShowSuccessToast("Expense deleted"))
                sendEffect(ExpenseEntryEffect.DismissScreen)
            } catch (e: Exception) {
                sendEffect(ExpenseEntryEffect.ShowValidationToast("Failed to delete expense"))
            }
        }
    }

    private fun parseDate(dateStr: String): Instant? {
        return try {
            // Expected format: yyyy-MM-dd
            val parts = dateStr.split("-")
            if (parts.size != 3) return null
            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull() ?: return null
            val day = parts[2].toIntOrNull() ?: return null
            
            // Create an Instant from the date components
            kotlinx.datetime.LocalDateTime(year, month, day, 0, 0, 0)
                .toInstant(TimeZone.currentSystemDefault())
        } catch (e: Exception) {
            null
        }
    }
}
