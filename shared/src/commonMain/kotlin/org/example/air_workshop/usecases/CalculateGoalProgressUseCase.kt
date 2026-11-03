package org.example.air_workshop.usecases

import org.example.air_workshop.repository.ExpenseRepository
import org.example.air_workshop.repository.GoalRepository

enum class BudgetStatus {
    NORMAL,
    WARNING,
    EXCEEDED
}

data class GoalProgress(
    val targetAmount: Double,
    val spentAmount: Double,
    val remainingAmount: Double,
    val percentage: Double,
    val status: BudgetStatus,
    val hasGoal: Boolean
)

class CalculateGoalProgressUseCase(
    private val expenseRepository: ExpenseRepository,
    private val goalRepository: GoalRepository
) {
    companion object {
        private const val WARNING_THRESHOLD = 80.0
    }

    suspend fun execute(year: String, month: String): GoalProgress {
        val goal = goalRepository.getGoalByMonth("$year-$month")
        if (goal == null) {
            return GoalProgress(
                targetAmount = 0.0,
                spentAmount = 0.0,
                remainingAmount = 0.0,
                percentage = 0.0,
                status = BudgetStatus.NORMAL,
                hasGoal = false
            )
        }

        val spent = expenseRepository.getTotalSpentByMonth(year, month)
        val target = goal.targetAmount
        val remaining = target - spent
        val percentage = if (target > 0) (spent / target) * 100.0 else 0.0

        val status = when {
            percentage >= 100.0 -> BudgetStatus.EXCEEDED
            percentage >= WARNING_THRESHOLD -> BudgetStatus.WARNING
            else -> BudgetStatus.NORMAL
        }

        return GoalProgress(
            targetAmount = target,
            spentAmount = spent,
            remainingAmount = remaining,
            percentage = percentage,
            status = status,
            hasGoal = true
        )
    }
}
