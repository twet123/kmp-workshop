package org.example.air_workshop.usecases

import org.example.air_workshop.model.Tag
import org.example.air_workshop.repository.ExpenseRepository
import org.example.air_workshop.repository.TagRepository

data class TagBreakdown(
    val tag: Tag,
    val totalAmount: Double,
    val percentage: Double,
    val expenseCount: Int
)

class GetTagBreakdownUseCase(
    private val expenseRepository: ExpenseRepository,
    private val tagRepository: TagRepository
) {
    suspend fun execute(year: String, month: String): List<TagBreakdown> {
        val expenses = expenseRepository.getExpensesByMonth(year, month)
        if (expenses.isEmpty()) return emptyList()

        val tags = tagRepository.getAllTags()
        val tagMap = tags.associateBy { it.id }

        val totalSpent = expenses.sumOf { it.amount }
        if (totalSpent == 0.0) return emptyList()

        val grouped = expenses.groupBy { it.tagId }
        return grouped.map { (tagId, tagExpenses) ->
            val tag = tagMap[tagId] ?: Tag(0L, "Unknown", "#999999")
            val tagTotal = tagExpenses.sumOf { it.amount }
            val percentage = if (totalSpent > 0) (tagTotal / totalSpent) * 100.0 else 0.0
            TagBreakdown(
                tag = tag,
                totalAmount = tagTotal,
                percentage = percentage,
                expenseCount = tagExpenses.size
            )
        }.sortedByDescending { it.totalAmount }
    }
}
