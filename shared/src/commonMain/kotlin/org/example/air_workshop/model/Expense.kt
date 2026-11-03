package org.example.air_workshop.model

import kotlin.time.Instant

data class Expense(
    val id: Long = 0L,
    val amount: Double,
    val currencyCode: String,
    val timestamp: Instant,
    val tagId: Long,
    val note: String = "",
    val isRecurring: Boolean = false
)
