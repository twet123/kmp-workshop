package org.example.air_workshop.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val amount: Double,
    val currencyCode: String,
    val timestamp: Long, // stored as epoch seconds
    val tagId: Long,
    val note: String = "",
    val isRecurring: Boolean = false
)
