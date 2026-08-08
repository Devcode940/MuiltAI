package com.multaihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.model.Note
import com.multaihub.app.data.model.Prompt
import com.multaihub.app.data.model.Tab
import com.multaihub.app.data.repository.AiRepository
import com.multaihub.app.data.repository.RepositoryException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Coordinates WebView-related application state and local persistence.
 *
 * // WHY: WebView callbacks belong to the UI layer; persistent state changes belong here so
 * // database failures cannot crash a Composable coroutine or leave partially updated state.
 */
class WebViewViewModel(private val repository: AiRepository) : ViewModel() {
    private val _currentProvider = MutableStateFlow<AiProvider?>(null)
    val currentProvider: StateFlow<AiProvider?> = _currentProvider.asStateFlow()

    private val _currentTab = MutableStateFlow<Tab?>(null)
    val currentTab: StateFlow<Tab?> = _currentTab.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    val prompts: StateFlow<List<Prompt>> = repository.getAllPrompts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val tabs: StateFlow<List<Tab>> = repository.getAllTabs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val providerMutationMutex = Mutex()

    /** Sets the active provider and records its last-used timestamp. */
    fun setProvider(provider: AiProvider) {
        _currentProvider.value = provider
        viewModelScope.launch {
            runCatching { repository.updateLastUsed(provider.id) }
                .onFailure { _error.value = "Could not update recent AI history." }
        }
    }

    /** Sets the active tab and records its access time. */
    fun setTab(tab: Tab) {
        _currentTab.value = tab
        viewModelScope.launch {
            runCatching { repository.updateTabLastAccessed(tab.id) }
                .onFailure { _error.value = "Could not update tab history." }
        }
    }

    /**
     * Toggles the active provider's desktop/mobile mode atomically.
     * // WHY: A mutex prevents rapid taps from producing two competing database writes and stale UI state.
     */
    fun toggleDesktopMode() {
        viewModelScope.launch {
            providerMutationMutex.withLock {
                val provider = _currentProvider.value ?: return@withLock
                val newMode = !provider.isDesktopMode
                runCatching {
                    repository.toggleDesktopMode(provider.id, newMode)
                }.onSuccess {
                    _currentProvider.value = provider.copy(isDesktopMode = newMode)
                    _error.value = null
                }.onFailure {
                    _error.value = "Could not change display mode. Please try again."
                }
            }
        }
    }

    /** Saves a note without exposing persistence exceptions to the UI coroutine. */
    fun saveNote(content: String, sourceAi: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            runCatching {
                repository.addNote(Note(content = content.trim(), sourceAi = sourceAi.trim()))
            }.onFailure {
                _error.value = "Could not save the note. Please try again."
            }
        }
    }

    /** Adds a non-empty prompt to the local prompt library. */
    fun addPrompt(title: String, content: String) {
        val cleanTitle = title.trim()
        val cleanContent = content.trim()
        if (cleanTitle.isBlank() || cleanContent.isBlank()) return

        viewModelScope.launch {
            runCatching {
                repository.addPrompt(Prompt(title = cleanTitle, content = cleanContent))
            }.onFailure {
                _error.value = "Could not save the prompt. Please try again."
            }
        }
    }

    /** Deletes a saved prompt. */
    fun deletePrompt(prompt: Prompt) {
        viewModelScope.launch {
            runCatching { repository.deletePrompt(prompt) }
                .onFailure { _error.value = "Could not delete the prompt. Please try again." }
        }
    }

    /** Creates a new tab and returns its database identifier. */
    suspend fun createNewTab(provider: AiProvider): Result<Long> = runCatching {
        repository.addTab(
            Tab(
                providerId = provider.id,
                title = provider.name,
                url = provider.url,
                isDesktopMode = provider.isDesktopMode
            )
        )
    }.onFailure {
        _error.value = "Could not create a new tab. Please try again."
    }

    /** Closes a single tab. */
    fun closeTab(tab: Tab) {
        viewModelScope.launch {
            runCatching { repository.deleteTab(tab) }
                .onFailure { _error.value = "Could not close the tab. Please try again." }
        }
    }

    /** Closes all persisted tabs. */
    fun closeAllTabs() {
        viewModelScope.launch {
            runCatching { repository.deleteAllTabs() }
                .onFailure { _error.value = "Could not close all tabs. Please try again." }
        }
    }

    /** Persists browser navigation state for a tab. */
    fun updateTabNavigationState(tabId: Long, canGoBack: Boolean, canGoForward: Boolean) {
        viewModelScope.launch {
            runCatching {
                repository.updateTabNavigationState(tabId, canGoBack, canGoForward)
            }.onFailure {
                _error.value = "Could not save tab navigation state."
            }
        }
    }

    /** Clears the current user-facing error. */
    fun clearError() {
        _error.value = null
    }

    class Factory(private val repository: AiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(WebViewViewModel::class.java)) {
                return WebViewViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
