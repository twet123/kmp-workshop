package org.example.air_workshop.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.example.air_workshop.database.entities.ExpenseEntity

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :id")
    suspend fun deleteExpenseById(id: Long)

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Long): ExpenseEntity?

    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun observeAllExpenses(): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses " +
        "WHERE strftime('%Y', timestamp, 'unixepoch') = :year " +
        "AND strftime('%m', timestamp, 'unixepoch') = :month " +
        "ORDER BY timestamp DESC"
    )
    fun observeExpensesByMonth(year: String, month: String): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses WHERE tagId = :tagId ORDER BY timestamp DESC"
    )
    fun observeExpensesByTag(tagId: Long): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses " +
        "WHERE note LIKE '%' || :query || '%' " +
        "ORDER BY timestamp DESC"
    )
    fun searchExpenses(query: String): Flow<List<ExpenseEntity>>

    @Query(
        "SELECT * FROM expenses " +
        "WHERE strftime('%Y', timestamp, 'unixepoch') = :year " +
        "AND strftime('%m', timestamp, 'unixepoch') = :month " +
        "ORDER BY timestamp DESC"
    )
    suspend fun getExpensesByMonth(year: String, month: String): List<ExpenseEntity>

    @Query(
        "SELECT SUM(amount) FROM expenses " +
        "WHERE strftime('%Y', timestamp, 'unixepoch') = :year " +
        "AND strftime('%m', timestamp, 'unixepoch') = :month"
    )
    suspend fun getTotalSpentByMonth(year: String, month: String): Double?

    @Query(
        "SELECT * FROM expenses ORDER BY timestamp DESC LIMIT :limit"
    )
    suspend fun getRecentExpenses(limit: Int = 10): List<ExpenseEntity>
}
