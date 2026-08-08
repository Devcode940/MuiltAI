package com.multaihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.repository.AiRepository
import com.multaihub.app.utils.UrlValidator
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Owns provider catalog presentation state and user actions from the home screen.
 */
class HomeViewModel(private val repository: AiRepository) : ViewModel() {
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val providers: StateFlow<List<AiProvider>> = combine(
        repository.getAllVisibleProviders(),
        _selectedCategory,
        _searchQuery
    ) { list, category, query ->
        list.filter { provider ->
            val matchesCategory = category == "All" ||
                provider.category.equals(category, ignoreCase = true)
            val matchesSearch = query.isBlank() ||
                provider.name.contains(query, ignoreCase = true) ||
                provider.category.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val recentProviders: StateFlow<List<AiProvider>> = repository.getRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Changes the selected provider category. */
    fun selectCategory(category: String) {
        _selectedCategory.value = category.trim().ifBlank { "All" }
    }

    /** Updates the provider search query. */
    fun updateSearch(query: String) {
        _searchQuery.value = query.take(100)
    }

    /** Records provider usage without crashing the UI when persistence fails. */
    fun markAsUsed(id: String) {
        viewModelScope.launch {
            runCatching { repository.updateLastUsed(id) }
                .onFailure { _error.value = "Could not update recent AI history." }
        }
    }

    /** Toggles a provider's favorite state. */
    fun toggleFavorite(provider: AiProvider) {
        viewModelScope.launch {
            runCatching {
                repository.toggleFavorite(provider.id, !provider.isFavorite)
            }.onFailure {
                _error.value = "Could not update favorites. Please try again."
            }
        }
    }

    /**
     * Adds a custom AI provider after validating and normalizing the URL.
     * // WHY: This is an untrusted input boundary; raw names, URLs, and exception messages must not enter persistence or UI.
     */
    fun addCustomAi(name: String, url: String, category: String = "Custom") {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val cleanName = name.trim()
                val cleanCategory = category.trim().ifBlank { "Custom" }
                if (cleanName.isBlank()) {
                    _error.value = "Enter a name for the AI provider."
                    return@launch
                }
                if (cleanName.length > 80) {
                    _error.value = "AI provider names must be 80 characters or fewer."
                    return@launch
                }
                if (cleanCategory.length > 32) {
                    _error.value = "Category names must be 32 characters or fewer."
                    return@launch
                }

                val validatedUrl = UrlValidator.validateAndEnforceHttps(
                    url,
                    enforceHttps = true
                )
                if (validatedUrl == null) {
                    _error.value = "Enter a valid HTTPS website address."
                    return@launch
                }

                // WHY: Check only the requested URL instead of collecting the entire provider table.
                if (repository.getProviderByUrl(validatedUrl) != null) {
                    _error.value = "This AI provider already exists."
                    return@launch
                }

                val provider = AiProvider(
                    id = UUID.randomUUID().toString(),
                    name = cleanName,
                    url = validatedUrl,
                    category = cleanCategory,
                    isCustom = true,
                    sortOrder = Int.MAX_VALUE
                )
                repository.addCustomProvider(provider)
            } catch (_: Exception) {
                // WHY: Internal SQLite/Room details are not actionable and may expose implementation information.
                _error.value = "Could not add the AI provider. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Deletes a custom provider; built-in providers are protected. */
    fun deleteCustomAi(provider: AiProvider) {
        if (!provider.isCustom) return
        viewModelScope.launch {
            runCatching { repository.deleteProvider(provider) }
                .onFailure { _error.value = "Could not remove the AI provider. Please try again." }
        }
    }

    /** Clears the current user-facing error. */
    fun clearError() {
        _error.value = null
    }

    class Factory(private val repository: AiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
