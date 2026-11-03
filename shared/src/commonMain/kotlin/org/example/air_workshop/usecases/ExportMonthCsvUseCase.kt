package org.example.air_workshop.usecases

import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import org.example.air_workshop.repository.ExpenseRepository
import org.example.air_workshop.repository.TagRepository
import org.example.air_workshop.ui.components.toFixed

class ExportMonthCsvUseCase(
    private val expenseRepository: ExpenseRepository,
    private val tagRepository: TagRepository
) {
    suspend fun execute(year: String, month: String): String {
        val expenses = expenseRepository.getExpensesByMonth(year, month)
        val tagNames = tagRepository.getAllTags().associate { it.id to it.name }

        val sb = StringBuilder()
        sb.appendLine("Date,Time,Amount,Currency,Tag,Note,Recurring")

        expenses.sortedBy { it.timestamp }.forEach { expense ->
            val local = expense.timestamp.toLocalDateTime(TimeZone.currentSystemDefault())
            val date = "${local.year}-${local.month.number.toString().padStart(2, '0')}-${local.day.toString().padStart(2, '0')}"
            val time = "${local.hour.toString().padStart(2, '0')}:${local.minute.toString().padStart(2, '0')}"
            val amount = expense.amount.toFixed(2)
            val currency = escapeCsvField(expense.currencyCode)
            val tag = escapeCsvField(tagNames[expense.tagId] ?: "")
            val note = escapeCsvField(expense.note)
            val recurring = expense.isRecurring
            sb.appendLine("$date,$time,$amount,$currency,$tag,$note,$recurring")
        }

        return sb.toString()
    }

    private fun escapeCsvField(value: String): String {
        val needsQuoting = value.contains(',') || value.contains('"') || value.contains('\n') || value.contains('\r')
        return if (needsQuoting) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
