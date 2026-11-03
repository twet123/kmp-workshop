package org.example.air_workshop.usecases

import kotlinx.coroutines.flow.Flow
import org.example.air_workshop.model.Expense
import org.example.air_workshop.repository.ExpenseRepository

class GetMonthlyExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) {
    fun observe(year: String, month: String): Flow<List<Expense>> =
        expenseRepository.observeExpensesByMonth(year, month)

    suspend fun execute(year: String, month: String): List<Expense> =
        expenseRepository.getExpensesByMonth(year, month)
}
