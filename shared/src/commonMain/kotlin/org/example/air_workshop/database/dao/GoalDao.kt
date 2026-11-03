package org.example.air_workshop.database.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.example.air_workshop.database.entities.MonthlyGoalEntity

@Dao
interface GoalDao {

    @Upsert
    suspend fun upsertGoal(goal: MonthlyGoalEntity)

    @Query("DELETE FROM monthly_goals WHERE monthYear = :monthYear")
    suspend fun deleteGoalByMonthYear(monthYear: String)

    @Query("SELECT * FROM monthly_goals WHERE monthYear = :monthYear")
    fun observeGoalByMonth(monthYear: String): Flow<MonthlyGoalEntity?>

    @Query("SELECT * FROM monthly_goals WHERE monthYear = :monthYear")
    suspend fun getGoalByMonth(monthYear: String): MonthlyGoalEntity?

    @Query("SELECT * FROM monthly_goals ORDER BY monthYear DESC")
    fun observeAllGoals(): Flow<List<MonthlyGoalEntity>>
}
