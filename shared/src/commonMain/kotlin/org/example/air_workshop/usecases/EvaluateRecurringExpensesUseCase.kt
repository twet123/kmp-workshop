package org.example.air_workshop.usecases

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.Instant
import org.example.air_workshop.model.Expense
import org.example.air_workshop.repository.ExpenseRepository

class EvaluateRecurringExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend fun execute(currentYear: String, currentMonth: String): List<Expense> {
        val existingExpenses = expenseRepository.getExpensesByMonth(currentYear, currentMonth)
        val existingRecurringIds = existingExpenses
            .filter { expense -> expense.isRecurring }
            .mapNotNull { expense -> expense.id.takeIf { it > 0L } }
            .toSet()

        // Fetch all recurring expenses from previous months
        val allExpenses = expenseRepository.observeAllExpenses()
            .firstOrNull() ?: emptyList()

        val recurringExpenses = allExpenses.filter { expense ->
            expense.isRecurring && expense.id !in existingRecurringIds
        }

        val newExpenses = mutableListOf<Expense>()
        for (recurring in recurringExpenses) {
            val newExpense = Expense(
                id = 0L,
                amount = recurring.amount,
                currencyCode = recurring.currencyCode,
                timestamp = Instant.fromEpochSeconds(
                    recurring.timestamp.epochSeconds + 30L * 24 * 60 * 60
                ),
                tagId = recurring.tagId,
                note = recurring.note,
                isRecurring = true
            )
            newExpenses.add(newExpense)
            expenseRepository.insertExpense(newExpense)
        }

        return newExpenses
    }
}
