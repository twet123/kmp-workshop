package org.example.air_workshop.ui.screens.settings

import org.example.air_workshop.model.Currency
import org.example.air_workshop.model.MonthlyGoal
import org.example.air_workshop.model.Tag

data class SettingsState(
    val currentGoal: MonthlyGoal? = null,
    val baseCurrency: Currency = Currency.EUR,
    val tags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val isNewTagDialogOpen: Boolean = false,
    val isGoalDialogOpen: Boolean = false,
    val newTagName: String = "",
    val newTagColor: String = "#6B5CE7",
    val editingTag: Tag? = null,
    val goalAmount: String = "",
    val goalMonthYear: String = ""
)
