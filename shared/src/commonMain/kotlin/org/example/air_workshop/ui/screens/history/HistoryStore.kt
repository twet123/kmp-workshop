package org.example.air_workshop.ui.screens.history

import kotlinx.coroutines.launch
import org.example.air_workshop.mvi.MviStore
import org.example.air_workshop.model.Expense
import org.example.air_workshop.repository.ExpenseRepository
import org.example.air_workshop.repository.SettingsRepository
import org.example.air_workshop.repository.TagRepository
import org.example.air_workshop.usecases.SearchAndFilterExpensesUseCase
import org.example.air_workshop.usecases.SearchFilterParams
import kotlin.time.Instant

class HistoryStore(
    private val expenseRepository: ExpenseRepository,
    private val tagRepository: TagRepository,
    private val settingsRepository: SettingsRepository,
    private val searchAndFilter: SearchAndFilterExpensesUseCase
) : MviStore<HistoryState, HistoryIntent, HistoryEffect>() {

    override fun initialState(): HistoryState = HistoryState(isLoading = true)

    override fun handleStart() {
        loadDefaultCurrency()
        loadTags()
        loadExpenses()
    }

    override fun reduce(intent: HistoryIntent): HistoryState? {
        return when (intent) {
            is HistoryIntent.SearchQueryChanged -> {
                val newState = stateFlow.value.copy(
                    searchQuery = intent.query,
                    isLoading = true
                )
                loadExpenses()
                newState
            }
            is HistoryIntent.TagFilterToggled -> {
                val current = stateFlow.value.selectedTagIds.toMutableList()
                if (current.contains(intent.tagId)) {
                    current.remove(intent.tagId)
                } else {
                    current.add(intent.tagId)
                }
                val newState = stateFlow.value.copy(
                    selectedTagIds = current,
                    isLoading = true
                )
                loadExpenses()
                newState
            }
            is HistoryIntent.DeleteExpense -> {
                deleteExpense(intent.expense)
                stateFlow.value.copy(error = null)
            }
            is HistoryIntent.Refresh -> {
                val newState = stateFlow.value.copy(isLoading = true, error = null)
                loadExpenses()
                newState
            }
            is HistoryIntent.ClearFilters -> {
                val newState = stateFlow.value.copy(
                    searchQuery = "",
                    selectedTagIds = emptyList(),
                    isLoading = true
                )
                loadExpenses()
                newState
            }
        }
    }

    private fun loadDefaultCurrency() {
        coroutineScope.launch {
            settingsRepository.observeDefaultCurrency().collect { currency ->
                setState { copy(currencyCode = currency.code) }
            }
        }
    }

    private fun loadTags() {
        coroutineScope.launch {
            tagRepository.observeAllTags().collect { tags ->
                setState {
                    copy(
                        availableTags = tags,
                        tags = tags.associateBy { it.id }
                    )
                }
            }
        }
    }

    private fun loadExpenses() {
        coroutineScope.launch {
            try {
                val state = stateFlow.value
                val params = SearchFilterParams(
                    query = state.searchQuery,
                    tagIds = state.selectedTagIds
                )
                val expenses = searchAndFilter.execute(params)
                // Sort by timestamp descending (newest first)
                val sorted = expenses.sortedByDescending { it.timestamp }
                setState {
                    copy(
                        expenses = sorted,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load expenses"
                    )
                }
            }
        }
    }

    private fun deleteExpense(expense: Expense) {
        coroutineScope.launch {
            try {
                expenseRepository.deleteExpense(expense)
                sendEffect(HistoryEffect.ShowSnackbar("Expense deleted"))
                loadExpenses()
            } catch (e: Exception) {
                sendEffect(HistoryEffect.ShowSnackbar("Failed to delete expense"))
            }
        }
    }
}
