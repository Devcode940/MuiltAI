package com.multaihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multaihub.app.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: AiRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun clearAllData() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _message.value = null
                repository.deleteAllTabs()
                repository.getAllProviders().first().filter { it.isCustom }.forEach { repository.deleteProvider(it) }
                _message.value = "All data cleared"
            } catch (e: Exception) {
                _message.value = "Failed: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun exportData() {
        viewModelScope.launch {
            _message.value = "Export coming soon"
        }
    }

    fun importData() {
        viewModelScope.launch {
            _message.value = "Import coming soon"
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    class Factory(private val repository: AiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(repository) as T
        }
    }
}
