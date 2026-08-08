package com.multaihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multaihub.app.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/** Coordinates destructive local-data actions from Settings. */
class SettingsViewModel(private val repository: AiRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    /**
     * Clears user-owned local data while preserving the built-in AI catalog.
     * // WHY: The old implementation left notes/prompts behind and exposed raw database errors.
     */
    fun clearAllData() {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = null
            try {
                repository.deleteAllTabs()
                repository.deleteAllNotes()
                repository.deleteAllPrompts()
                repository.deleteCustomProviders()
                _message.value = "Your saved tabs, notes, prompts, and custom AIs were cleared."
            } catch (_: Exception) {
                _message.value = "Could not clear all saved data. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Indicates that data export is not implemented yet. */
    fun exportData() {
        _message.value = "Export is not available yet."
    }

    /** Indicates that data import is not implemented yet. */
    fun importData() {
        _message.value = "Import is not available yet."
    }

    /** Clears the current Settings message. */
    fun clearMessage() {
        _message.value = null
    }

    class Factory(private val repository: AiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                return SettingsViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
