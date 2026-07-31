package com.multaihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.repository.AiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: AiRepository) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

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
            val provider = AiProvider(
                id = "custom_${System.currentTimeMillis()}",
                name = name,
                url = url,
                category = category,
                isCustom = true,
                sortOrder = 999
            )
            repository.addCustomProvider(provider)
        }
    }

    fun deleteCustomAi(provider: AiProvider) {
        if (provider.isCustom) {
            viewModelScope.launch {
                repository.deleteProvider(provider)
            }
        }
    }

    class Factory(private val repository: AiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(repository) as T
        }
    }
}
