package org.example.air_workshop.ui.screens.dashboard

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.example.air_workshop.mvi.MviStore
import org.example.air_workshop.repository.ExpenseRepository
import org.example.air_workshop.repository.GoalRepository
import org.example.air_workshop.repository.SettingsRepository
import org.example.air_workshop.repository.TagRepository
import org.example.air_workshop.ui.components.currentMonth
import org.example.air_workshop.ui.components.isCurrentMonth
import org.example.air_workshop.ui.components.nextMonth
import org.example.air_workshop.ui.components.previousMonth
import org.example.air_workshop.usecases.CalculateGoalProgressUseCase
import org.example.air_workshop.usecases.EvaluateRecurringExpensesUseCase
import org.example.air_workshop.usecases.GetMonthlyExpensesUseCase
import org.example.air_workshop.usecases.GetTagBreakdownUseCase

class DashboardStore(
    private val getMonthlyExpenses: GetMonthlyExpensesUseCase,
    private val getTagBreakdown: GetTagBreakdownUseCase,
    private val calculateGoalProgress: CalculateGoalProgressUseCase,
    private val evaluateRecurring: EvaluateRecurringExpensesUseCase,
    private val tagRepository: TagRepository,
    private val expenseRepository: ExpenseRepository,
    private val settingsRepository: SettingsRepository
) : MviStore<DashboardState, DashboardIntent, DashboardEffect>() {

    override fun initialState(): DashboardState {
        val (year, month) = currentMonth()
        return DashboardState(
            year = year,
            month = month,
            isLoading = true
        )
    }

    override fun handleStart() {
        loadDefaultCurrency()
        loadTags()
        loadDashboardData(stateFlow.value.year, stateFlow.value.month)
    }

    override fun reduce(intent: DashboardIntent): DashboardState? {
        return when (intent) {
            is DashboardIntent.SelectMonth -> {
                val newState = stateFlow.value.copy(
                    year = intent.year,
                    month = intent.month,
                    isLoading = true,
                    error = null
                )
                loadDashboardData(intent.year, intent.month)
                newState
            }
            is DashboardIntent.PreviousMonth -> {
                val (year, month) = previousMonth(stateFlow.value.year, stateFlow.value.month)
                val newState = stateFlow.value.copy(
                    year = year,
                    month = month,
                    isLoading = true,
                    error = null
                )
                loadDashboardData(year, month)
                newState
            }
            is DashboardIntent.NextMonth -> {
                val (year, month) = nextMonth(stateFlow.value.year, stateFlow.value.month)
                val newState = stateFlow.value.copy(
                    year = year,
                    month = month,
                    isLoading = true,
                    error = null
                )
                loadDashboardData(year, month)
                newState
            }
            is DashboardIntent.JumpToCurrentMonth -> {
                val (year, month) = currentMonth()
                val newState = stateFlow.value.copy(
                    year = year,
                    month = month,
                    isLoading = true,
                    error = null
                )
                loadDashboardData(year, month)
                newState
            }
            is DashboardIntent.DeleteExpense -> {
                deleteExpense(intent.expense)
                stateFlow.value.copy(error = null)
            }
            is DashboardIntent.Refresh -> {
                val newState = stateFlow.value.copy(isLoading = true, error = null)
                loadDashboardData(stateFlow.value.year, stateFlow.value.month)
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
                setState { copy(tags = tags.associateBy { it.id }) }
            }
        }
    }

    private fun loadDashboardData(year: Int, month: Int) {
        coroutineScope.launch {
            val yearStr = year.toString()
            val monthStr = month.toString().padStart(2, '0')
            try {
                // Evaluate recurring expenses first
                evaluateRecurring.execute(yearStr, monthStr)

                // Load goal progress
                val goalProgress = calculateGoalProgress.execute(yearStr, monthStr)

                // Load tag breakdown
                val breakdown = getTagBreakdown.execute(yearStr, monthStr)

                // Load expenses
                val expenses = getMonthlyExpenses.execute(yearStr, monthStr)

                setState {
                    copy(
                        goalProgress = goalProgress,
                        tagBreakdown = breakdown,
                        expenses = expenses,
                        isLoading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                setState {
                    copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load data"
                    )
                }
            }
        }
    }

    private fun deleteExpense(expense: org.example.air_workshop.model.Expense) {
        coroutineScope.launch {
            try {
                expenseRepository.deleteExpense(expense)
                sendEffect(DashboardEffect.ShowSnackbar("Expense deleted"))
                // Refresh data
                loadDashboardData(stateFlow.value.year, stateFlow.value.month)
            } catch (e: Exception) {
                sendEffect(DashboardEffect.ShowSnackbar("Failed to delete expense"))
            }
        }
    }
}
