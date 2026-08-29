package com.multaihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.repository.AiRepository
import com.multaihub.app.utils.AppConstants.CUSTOM_PROVIDER_SORT_ORDER
import com.multaihub.app.utils.AppConstants.FLOW_SHARING_TIMEOUT_MS
import com.multaihub.app.utils.AppConstants.MAX_CATEGORY_NAME_LENGTH
import com.multaihub.app.utils.AppConstants.MAX_PROVIDER_NAME_LENGTH
import com.multaihub.app.utils.AppConstants.MAX_SEARCH_QUERY_LENGTH
import com.multaihub.app.utils.AppConstants.SEARCH_DEBOUNCE_MS
import com.multaihub.app.utils.UrlValidator
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Owns provider catalog presentation state and user actions from the home screen.
 *
 * All database queries are delegated to Room (rather than filtering in-memory) so work
 * stays proportional to the result set and Compose recompositions are minimized.
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

    /**
     * Provider catalog filtered by the selected category and search query.
     *
     * Queries SQLite directly instead of loading and filtering the entire catalog in Compose.
     */
    val providers: StateFlow<List<AiProvider>> = kotlinx.coroutines.flow.combine(
        _selectedCategory,
        _searchQuery.debounce(SEARCH_DEBOUNCE_MS)
    ) { category, query -> category to query.trim().take(MAX_SEARCH_QUERY_LENGTH) }
        .flatMapLatest { (category, query) ->
            when {
                category.equals("Favorites", ignoreCase = true) && query.isBlank() ->
                    repository.getFavorites()

                category.equals("Favorites", ignoreCase = true) ->
                    repository.searchProviders(query).let { flow ->
                        kotlinx.coroutines.flow.combine(flow, repository.getFavorites()) { matches, favorites ->
                            val ids = favorites.asSequence().map { it.id }.toSet()
                            matches.filter { it.id in ids }
                        }
                    }

                category.equals("All", ignoreCase = true) && query.isBlank() ->
                    repository.getAllVisibleProviders()

                category.equals("All", ignoreCase = true) ->
                    repository.searchProviders(query)

                query.isBlank() ->
                    repository.getProvidersByCategory(category)

                else ->
                    kotlinx.coroutines.flow.combine(
                        repository.searchProviders(query),
                        repository.getProvidersByCategory(category)
                    ) { matches, categorized ->
                        val ids = categorized.asSequence().map { it.id }.toSet()
                        matches.filter { it.id in ids }
                    }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_SHARING_TIMEOUT_MS), emptyList())

    /** Recently used providers (limited by the database query). */
    val recentProviders: StateFlow<List<AiProvider>> = repository.getRecent()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(FLOW_SHARING_TIMEOUT_MS), emptyList())

    /**
     * Selects a provider category for filtering.
     *
     * @param category The category name; blank values default to "All".
     */
    fun selectCategory(category: String) {
        _selectedCategory.value = category.trim().ifBlank { "All" }
    }

    /**
     * Updates the search query, truncating it to a safe maximum length.
     *
     * @param query The raw search input.
     */
    fun updateSearch(query: String) {
        _searchQuery.value = query.take(MAX_SEARCH_QUERY_LENGTH)
    }

    /**
     * Records provider usage without crashing the UI when persistence fails.
     *
     * @param id The provider identifier.
     */
    fun markAsUsed(id: String) {
        viewModelScope.launch {
            runCatching { repository.updateLastUsed(id) }
                .onFailure { _error.value = "Could not update recent AI history." }
        }
    }

    /**
     * Toggles a provider's favorite state.
     *
     * @param provider The provider to update.
     */
    fun toggleFavorite(provider: AiProvider) {
        viewModelScope.launch {
            runCatching { repository.toggleFavorite(provider.id, !provider.isFavorite) }
                .onFailure { _error.value = "Could not update favorites. Please try again." }
        }
    }

    /**
     * Adds a custom AI provider after validating and normalizing the URL.
     *
     * Validates name length, category length, and URL format before persisting.
     *
     * @param name Display name for the provider.
     * @param url Website URL for the provider.
     * @param category Category to group the provider under.
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
                if (cleanName.length > MAX_PROVIDER_NAME_LENGTH) {
                    _error.value = "AI provider names must be $MAX_PROVIDER_NAME_LENGTH characters or fewer."
                    return@launch
                }
                if (cleanCategory.length > MAX_CATEGORY_NAME_LENGTH) {
                    _error.value = "Category names must be $MAX_CATEGORY_NAME_LENGTH characters or fewer."
                    return@launch
                }

                val validatedUrl = UrlValidator.validateAndEnforceHttps(url, enforceHttps = true)
                if (validatedUrl == null) {
                    _error.value = "Enter a valid HTTPS website address."
                    return@launch
                }
                if (repository.getProviderByUrl(validatedUrl) != null) {
                    _error.value = "This AI provider already exists."
                    return@launch
                }

                repository.addCustomProvider(
                    AiProvider(
                        id = UUID.randomUUID().toString(),
                        name = cleanName,
                        url = validatedUrl,
                        category = cleanCategory,
                        isCustom = true,
                        sortOrder = CUSTOM_PROVIDER_SORT_ORDER
                    )
                )
            } catch (_: Exception) {
                _error.value = "Could not add the AI provider. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Deletes a custom AI provider. Built-in providers are protected from deletion.
     *
     * @param provider The provider to delete.
     */
    fun deleteCustomAi(provider: AiProvider) {
        if (!provider.isCustom) return
        viewModelScope.launch {
            runCatching { repository.deleteProvider(provider) }
                .onFailure { _error.value = "Could not remove the AI provider. Please try again." }
        }
    }

    /** Clears the current user-facing error message. */
    fun clearError() {
        _error.value = null
    }

    /**
     * Factory for creating [HomeViewModel] with its dependencies.
     *
     * @param repository The data repository to use.
     */
    class Factory(private val repository: AiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) return HomeViewModel(repository) as T
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
