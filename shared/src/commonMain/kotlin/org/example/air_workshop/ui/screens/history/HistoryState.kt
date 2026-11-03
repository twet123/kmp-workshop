package org.example.air_workshop.ui.screens.history

import org.example.air_workshop.model.Expense
import org.example.air_workshop.model.Tag

data class HistoryState(
    val expenses: List<Expense> = emptyList(),
    val tags: Map<Long, Tag> = emptyMap(),
    val searchQuery: String = "",
    val selectedTagIds: List<Long> = emptyList(),
    val availableTags: List<Tag> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val currencyCode: String = "EUR"
)
