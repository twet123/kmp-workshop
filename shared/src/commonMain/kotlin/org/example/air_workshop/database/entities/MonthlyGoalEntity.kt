package org.example.air_workshop.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "monthly_goals")
data class MonthlyGoalEntity(
    @PrimaryKey
    val monthYear: String, // format: "yyyy-MM"
    val targetAmount: Double,
    val currencyCode: String
)
