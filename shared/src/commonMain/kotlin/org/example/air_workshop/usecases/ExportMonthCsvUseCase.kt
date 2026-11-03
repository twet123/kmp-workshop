package org.example.air_workshop.usecases

import org.example.air_workshop.model.Expense
import org.example.air_workshop.repository.ExpenseRepository

class ExportMonthCsvUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend fun execute(year: String, month: String): String {
        val expenses = expenseRepository.getExpensesByMonth(year, month)

        val sb = StringBuilder()
        sb.appendLine("Date,Amount,Currency,Tag ID,Note,Recurring")

        expenses.forEach { expense ->
            val date = expense.timestamp.toString()
            val amount = expense.amount
            val currency = escapeCsvField(expense.currencyCode)
            val tagId = expense.tagId
            val note = escapeCsvField(expense.note)
            val recurring = expense.isRecurring
            sb.appendLine("$date,$amount,$currency,$tagId,$note,$recurring")
        }

        return sb.toString()
    }

    private fun escapeCsvField(value: String): String {
        val needsQuoting = value.contains(',') || value.contains('"') || value.contains('\n')
        return if (needsQuoting) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
