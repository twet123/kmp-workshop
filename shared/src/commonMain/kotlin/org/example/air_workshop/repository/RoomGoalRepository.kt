package org.example.air_workshop.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.example.air_workshop.database.dao.GoalDao
import org.example.air_workshop.database.entities.MonthlyGoalEntity
import org.example.air_workshop.model.MonthlyGoal

class RoomGoalRepository(
    private val goalDao: GoalDao
) : GoalRepository {

    override fun observeGoalByMonth(monthYear: String): Flow<MonthlyGoal?> =
        goalDao.observeGoalByMonth(monthYear).map { it?.toDomain() }

    override suspend fun getGoalByMonth(monthYear: String): MonthlyGoal? =
        goalDao.getGoalByMonth(monthYear)?.toDomain()

    override fun observeAllGoals(): Flow<List<MonthlyGoal>> =
        goalDao.observeAllGoals().map { entities -> entities.toDomainList() }

    override suspend fun upsertGoal(goal: MonthlyGoal) =
        goalDao.upsertGoal(goal.toEntity())

    override suspend fun deleteGoalByMonthYear(monthYear: String) =
        goalDao.deleteGoalByMonthYear(monthYear)

    private fun MonthlyGoal.toEntity(): MonthlyGoalEntity = MonthlyGoalEntity(
        monthYear = monthYear,
        targetAmount = targetAmount,
        currencyCode = currencyCode
    )

    private fun MonthlyGoalEntity.toDomain(): MonthlyGoal = MonthlyGoal(
        monthYear = monthYear,
        targetAmount = targetAmount,
        currencyCode = currencyCode
    )

    private fun List<MonthlyGoalEntity>.toDomainList(): List<MonthlyGoal> =
        this.map { it.toDomain() }
}
