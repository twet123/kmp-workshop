package org.example.air_workshop.ui.screens.settings

import org.example.air_workshop.model.Currency
import org.example.air_workshop.model.Tag

sealed interface SettingsIntent {
    data class UpdateMonthlyGoal(val amount: String, val monthYear: String) : SettingsIntent
    data class SelectBaseCurrency(val currency: Currency) : SettingsIntent
    data class OpenNewTagDialog(val tag: Tag? = null) : SettingsIntent
    data object CloseNewTagDialog : SettingsIntent
    data class UpdateNewTagName(val name: String) : SettingsIntent
    data class UpdateNewTagColor(val color: String) : SettingsIntent
    data object CreateTag : SettingsIntent
    data object UpdateTag : SettingsIntent
    data class DeleteTag(val tag: Tag) : SettingsIntent
    data object OpenGoalDialog : SettingsIntent
    data object CloseGoalDialog : SettingsIntent
    data class UpdateGoalAmount(val amount: String) : SettingsIntent
    data class TriggerCsvExport(val year: String, val month: String) : SettingsIntent
    data object Refresh : SettingsIntent
}

sealed interface SettingsEffect {
    data class LaunchShareSheet(val fileName: String, val csvContent: String) : SettingsEffect
    data class ShowNotification(val message: String) : SettingsEffect
}
