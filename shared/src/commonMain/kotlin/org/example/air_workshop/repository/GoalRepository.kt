package org.example.air_workshop.repository

import kotlinx.coroutines.flow.Flow
import org.example.air_workshop.model.MonthlyGoal

interface GoalRepository {
    fun observeGoalByMonth(monthYear: String): Flow<MonthlyGoal?>
    suspend fun getGoalByMonth(monthYear: String): MonthlyGoal?
    fun observeAllGoals(): Flow<List<MonthlyGoal>>
    suspend fun upsertGoal(goal: MonthlyGoal)
    suspend fun deleteGoalByMonthYear(monthYear: String)
}
