package org.example.air_workshop.ui.screens.settings

import kotlinx.coroutines.launch
import org.example.air_workshop.mvi.MviStore
import org.example.air_workshop.model.Currency
import org.example.air_workshop.model.MonthlyGoal
import org.example.air_workshop.model.Tag
import org.example.air_workshop.repository.GoalRepository
import org.example.air_workshop.repository.SettingsRepository
import org.example.air_workshop.repository.TagRepository
import org.example.air_workshop.ui.components.currentMonth
import org.example.air_workshop.usecases.ExportMonthCsvUseCase

class SettingsStore(
    private val tagRepository: TagRepository,
    private val goalRepository: GoalRepository,
    private val settingsRepository: SettingsRepository,
    private val exportCsv: ExportMonthCsvUseCase
) : MviStore<SettingsState, SettingsIntent, SettingsEffect>() {

    override fun initialState(): SettingsState {
        val (year, month) = currentMonth()
        return SettingsState(
            goalMonthYear = "$year-${month.toString().padStart(2, '0')}",
            isLoading = true
        )
    }

    override fun handleStart() {
        loadTags()
        loadDefaultCurrency()
        loadCurrentGoal()
    }

    override fun reduce(intent: SettingsIntent): SettingsState? {
        return when (intent) {
            is SettingsIntent.UpdateMonthlyGoal -> {
                updateGoal(intent.amount, intent.monthYear)
                stateFlow.value.copy(isGoalDialogOpen = false)
            }
            is SettingsIntent.SelectBaseCurrency -> {
                setCurrency(intent.currency)
                stateFlow.value.copy(baseCurrency = intent.currency)
            }
            is SettingsIntent.OpenNewTagDialog -> {
                val tag = intent.tag
                if (tag != null) {
                    stateFlow.value.copy(
                        isNewTagDialogOpen = true,
                        editingTag = tag,
                        newTagName = tag.name,
                        newTagColor = tag.colorHex
                    )
                } else {
                    stateFlow.value.copy(
                        isNewTagDialogOpen = true,
                        editingTag = null,
                        newTagName = "",
                        newTagColor = "#6B5CE7"
                    )
                }
            }
            is SettingsIntent.CloseNewTagDialog -> {
                stateFlow.value.copy(
                    isNewTagDialogOpen = false,
                    editingTag = null,
                    newTagName = "",
                    newTagColor = "#6B5CE7"
                )
            }
            is SettingsIntent.UpdateNewTagName -> {
                stateFlow.value.copy(newTagName = intent.name)
            }
            is SettingsIntent.UpdateNewTagColor -> {
                stateFlow.value.copy(newTagColor = intent.color)
            }
            is SettingsIntent.CreateTag -> {
                createTag()
                stateFlow.value.copy(isLoading = true)
            }
            is SettingsIntent.UpdateTag -> {
                updateTag()
                stateFlow.value.copy(isLoading = true)
            }
            is SettingsIntent.DeleteTag -> {
                deleteTag(intent.tag)
                stateFlow.value.copy(isLoading = true)
            }
            is SettingsIntent.OpenGoalDialog -> {
                val current = stateFlow.value.currentGoal
                stateFlow.value.copy(
                    isGoalDialogOpen = true,
                    goalAmount = current?.targetAmount?.toString() ?: "",
                    goalMonthYear = current?.monthYear ?: stateFlow.value.goalMonthYear
                )
            }
            is SettingsIntent.CloseGoalDialog -> {
                stateFlow.value.copy(isGoalDialogOpen = false)
            }
            is SettingsIntent.UpdateGoalAmount -> {
                stateFlow.value.copy(goalAmount = intent.amount)
            }
            is SettingsIntent.TriggerCsvExport -> {
                exportCsv(intent.year, intent.month)
                stateFlow.value.copy(isLoading = true)
            }
            is SettingsIntent.Refresh -> {
                val newState = stateFlow.value.copy(isLoading = true, error = null)
                loadTags()
                loadCurrentGoal()
                newState
            }
        }
    }

    private fun loadTags() {
        coroutineScope.launch {
            tagRepository.observeAllTags().collect { tags ->
                setState {
                    copy(
                        tags = tags.sortedBy { it.name.lowercase() },
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun loadDefaultCurrency() {
        coroutineScope.launch {
            settingsRepository.observeDefaultCurrency().collect { currency ->
                setState { copy(baseCurrency = currency) }
            }
        }
    }

    private fun loadCurrentGoal() {
        coroutineScope.launch {
            val (year, month) = currentMonth()
            val monthYear = "$year-${month.toString().padStart(2, '0')}"
            goalRepository.observeGoalByMonth(monthYear).collect { goal ->
                setState { copy(currentGoal = goal) }
            }
        }
    }

    private fun setCurrency(currency: Currency) {
        coroutineScope.launch {
            try {
                settingsRepository.setDefaultCurrency(currency)
                sendEffect(SettingsEffect.ShowNotification("Currency updated to ${currency.displayName}"))
            } catch (e: Exception) {
                sendEffect(SettingsEffect.ShowNotification("Failed to update currency"))
            }
        }
    }

    private fun updateGoal(amount: String, monthYear: String) {
        coroutineScope.launch {
            try {
                val amountValue = amount.toDoubleOrNull()
                if (amountValue == null || amountValue <= 0) {
                    sendEffect(SettingsEffect.ShowNotification("Invalid amount"))
                    return@launch
                }
                val goal = MonthlyGoal(
                    monthYear = monthYear,
                    targetAmount = amountValue,
                    currencyCode = stateFlow.value.baseCurrency.code
                )
                goalRepository.upsertGoal(goal)
                sendEffect(SettingsEffect.ShowNotification("Goal updated"))
                setState { copy(isLoading = false) }
            } catch (e: Exception) {
                sendEffect(SettingsEffect.ShowNotification("Failed to update goal"))
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun createTag() {
        coroutineScope.launch {
            try {
                val name = stateFlow.value.newTagName.trim()
                if (name.isEmpty()) {
                    sendEffect(SettingsEffect.ShowNotification("Tag name cannot be empty"))
                    return@launch
                }
                val tag = Tag(
                    name = name,
                    colorHex = stateFlow.value.newTagColor
                )
                tagRepository.insertTag(tag)
                sendEffect(SettingsEffect.ShowNotification("Tag created"))
                setState {
                    copy(
                        isNewTagDialogOpen = false,
                        newTagName = "",
                        newTagColor = "#6B5CE7",
                        editingTag = null,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                sendEffect(SettingsEffect.ShowNotification("Failed to create tag"))
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun updateTag() {
        coroutineScope.launch {
            try {
                val editing = stateFlow.value.editingTag
                if (editing == null) return@launch
                val name = stateFlow.value.newTagName.trim()
                if (name.isEmpty()) {
                    sendEffect(SettingsEffect.ShowNotification("Tag name cannot be empty"))
                    return@launch
                }
                val updated = editing.copy(
                    name = name,
                    colorHex = stateFlow.value.newTagColor
                )
                tagRepository.updateTag(updated)
                sendEffect(SettingsEffect.ShowNotification("Tag updated"))
                setState {
                    copy(
                        isNewTagDialogOpen = false,
                        newTagName = "",
                        newTagColor = "#6B5CE7",
                        editingTag = null,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                sendEffect(SettingsEffect.ShowNotification("Failed to update tag"))
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun deleteTag(tag: Tag) {
        coroutineScope.launch {
            try {
                tagRepository.deleteTag(tag)
                sendEffect(SettingsEffect.ShowNotification("Tag deleted"))
                setState { copy(isLoading = false) }
            } catch (e: Exception) {
                sendEffect(SettingsEffect.ShowNotification("Failed to delete tag"))
                setState { copy(isLoading = false) }
            }
        }
    }

    private fun exportCsv(year: String, month: String) {
        coroutineScope.launch {
            try {
                val csv = exportCsv.execute(year, month)
                sendEffect(SettingsEffect.LaunchShareSheet(csv))
                setState { copy(isLoading = false) }
            } catch (e: Exception) {
                sendEffect(SettingsEffect.ShowNotification("Failed to export CSV"))
                setState { copy(isLoading = false) }
            }
        }
    }
}
