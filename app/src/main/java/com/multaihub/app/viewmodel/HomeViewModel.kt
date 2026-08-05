package com.multaihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.repository.AiRepository
import com.multaihub.app.utils.UrlValidator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
            val matchesCategory = category == "All" || provider.category.equals(category, ignoreCase = true)
            val matchesSearch = query.isBlank() ||
                    provider.name.contains(query, ignoreCase = true) ||
                    provider.category.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentProviders: StateFlow<List<AiProvider>> = repository.getRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun markAsUsed(id: String) {
        viewModelScope.launch {
            repository.updateLastUsed(id)
        }
    }

    fun toggleFavorite(provider: AiProvider) {
        viewModelScope.launch {
            repository.toggleFavorite(provider.id, !provider.isFavorite)
        }
    }

    fun addCustomAi(name: String, url: String, category: String = "Custom") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val validatedUrl = UrlValidator.validateAndEnforceHttps(url, enforceHttps = true)
                if (validatedUrl == null) {
                    _error.value = "Please enter a valid URL"
                    _isLoading.value = false
                    return@launch
                }
                val existing = repository.getAllProviders().firstOrNull()?.find {
                    it.url.equals(validatedUrl, ignoreCase = true)
                }
                if (existing != null) {
                    _error.value = "This AI provider already exists"
                    _isLoading.value = false
                    return@launch
                }
                val provider = AiProvider(
                    id = "custom_${System.currentTimeMillis()}",
                    name = name.trim(),
                    url = validatedUrl,
                    category = category,
                    isCustom = true,
                    sortOrder = 999
                )
                repository.addCustomProvider(provider)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed to add AI: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteCustomAi(provider: AiProvider) {
        if (provider.isCustom) {
            viewModelScope.launch {
                repository.deleteProvider(provider)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(private val repository: AiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
