package org.example.air_workshop.repository

import kotlinx.coroutines.flow.Flow
import org.example.air_workshop.model.Expense

interface ExpenseRepository {
    fun observeAllExpenses(): Flow<List<Expense>>
    fun observeExpensesByMonth(year: String, month: String): Flow<List<Expense>>
    fun observeExpensesByTag(tagId: Long): Flow<List<Expense>>
    fun searchExpenses(query: String): Flow<List<Expense>>
    suspend fun getExpenseById(id: Long): Expense?
    suspend fun getExpensesByMonth(year: String, month: String): List<Expense>
    suspend fun getTotalSpentByMonth(year: String, month: String): Double
    suspend fun getRecentExpenses(limit: Int = 10): List<Expense>
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expense: Expense)
    suspend fun deleteExpenseById(id: Long)
}
