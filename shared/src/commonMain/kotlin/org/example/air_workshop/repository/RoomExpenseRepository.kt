package org.example.air_workshop.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Instant
import org.example.air_workshop.database.dao.ExpenseDao
import org.example.air_workshop.database.entities.ExpenseEntity
import org.example.air_workshop.model.Expense

class RoomExpenseRepository(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun observeAllExpenses(): Flow<List<Expense>> =
        expenseDao.observeAllExpenses().map { entities -> entities.toDomainList() }

    override fun observeExpensesByMonth(year: String, month: String): Flow<List<Expense>> =
        expenseDao.observeExpensesByMonth(year, month).map { entities -> entities.toDomainList() }

    override fun observeExpensesByTag(tagId: Long): Flow<List<Expense>> =
        expenseDao.observeExpensesByTag(tagId).map { entities -> entities.toDomainList() }

    override fun searchExpenses(query: String): Flow<List<Expense>> =
        expenseDao.searchExpenses(query).map { entities -> entities.toDomainList() }

    override suspend fun getExpenseById(id: Long): Expense? =
        expenseDao.getExpenseById(id)?.toDomain()

    override suspend fun getExpensesByMonth(year: String, month: String): List<Expense> =
        expenseDao.getExpensesByMonth(year, month).toDomainList()

    override suspend fun getTotalSpentByMonth(year: String, month: String): Double =
        expenseDao.getTotalSpentByMonth(year, month) ?: 0.0

    override suspend fun getRecentExpenses(limit: Int): List<Expense> =
        expenseDao.getRecentExpenses(limit).toDomainList()

    override suspend fun insertExpense(expense: Expense): Long =
        expenseDao.insertExpense(expense.toEntity())

    override suspend fun updateExpense(expense: Expense) =
        expenseDao.updateExpense(expense.toEntity())

    override suspend fun deleteExpense(expense: Expense) =
        expenseDao.deleteExpense(expense.toEntity())

    override suspend fun deleteExpenseById(id: Long) =
        expenseDao.deleteExpenseById(id)

    private fun Expense.toEntity(): ExpenseEntity = ExpenseEntity(
        id = id,
        amount = amount,
        currencyCode = currencyCode,
        timestamp = timestamp.epochSeconds,
        tagId = tagId,
        note = note,
        isRecurring = isRecurring
    )

    private fun ExpenseEntity.toDomain(): Expense = Expense(
        id = id,
        amount = amount,
        currencyCode = currencyCode,
        timestamp = Instant.fromEpochSeconds(timestamp),
        tagId = tagId,
        note = note,
        isRecurring = isRecurring
    )

    private fun List<ExpenseEntity>.toDomainList(): List<Expense> =
        this.map { it.toDomain() }
}
