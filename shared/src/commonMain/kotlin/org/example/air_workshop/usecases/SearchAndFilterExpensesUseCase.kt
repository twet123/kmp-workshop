package org.example.air_workshop.usecases

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlin.time.Instant
import org.example.air_workshop.model.Expense
import org.example.air_workshop.repository.ExpenseRepository

data class SearchFilterParams(
    val query: String = "",
    val tagIds: List<Long> = emptyList(),
    val startTimestamp: Instant? = null,
    val endTimestamp: Instant? = null
)

class SearchAndFilterExpensesUseCase(
    private val expenseRepository: ExpenseRepository
) {
    fun observe(query: String): Flow<List<Expense>> =
        expenseRepository.searchExpenses(query)

    suspend fun execute(params: SearchFilterParams): List<Expense> {
        val expenses = if (params.query.isNotBlank()) {
            expenseRepository.searchExpenses(params.query).first()
        } else {
            expenseRepository.observeAllExpenses().first()
        }

        return expenses.filter { expense ->
            val matchesTag = params.tagIds.isEmpty() || params.tagIds.contains(expense.tagId)
            val matchesStart = params.startTimestamp == null || expense.timestamp >= params.startTimestamp
            val matchesEnd = params.endTimestamp == null || expense.timestamp <= params.endTimestamp
            matchesTag && matchesStart && matchesEnd
        }
    }
}
