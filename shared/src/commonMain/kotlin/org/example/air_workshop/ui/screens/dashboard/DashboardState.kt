package org.example.air_workshop.ui.screens.dashboard

import org.example.air_workshop.model.Expense
import org.example.air_workshop.model.Tag
import org.example.air_workshop.usecases.GoalProgress
import org.example.air_workshop.usecases.TagBreakdown

data class DashboardState(
    val year: Int = 2026,
    val month: Int = 11,
    val isLoading: Boolean = true,
    val error: String? = null,
    val goalProgress: GoalProgress? = null,
    val tagBreakdown: List<TagBreakdown> = emptyList(),
    val expenses: List<Expense> = emptyList(),
    val tags: Map<Long, Tag> = emptyMap(),
    val currencyCode: String = "EUR"
)
