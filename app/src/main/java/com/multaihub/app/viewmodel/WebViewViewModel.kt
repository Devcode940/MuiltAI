package com.multaihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.model.Note
import com.multaihub.app.data.model.Prompt
import com.multaihub.app.data.model.Tab
import com.multaihub.app.data.repository.AiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tabs: StateFlow<List<Tab>> = repository.getAllTabs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setProvider(provider: AiProvider) {
        _currentProvider.value = provider
        viewModelScope.launch {
            repository.updateLastUsed(provider.id)
        }
    }

    fun setTab(tab: Tab) {
        _currentTab.value = tab
        viewModelScope.launch {
            repository.updateTabLastAccessed(tab.id)
        }
    }

    fun toggleDesktopMode() {
        val p = _currentProvider.value ?: return
        viewModelScope.launch {
            repository.toggleDesktopMode(p.id, !p.isDesktopMode)
            _currentProvider.value = p.copy(isDesktopMode = !p.isDesktopMode)
        }
    }

    fun saveNote(content: String, sourceAi: String) {
        viewModelScope.launch {
            repository.addNote(Note(content = content, sourceAi = sourceAi))
        }
    }

    fun addPrompt(title: String, content: String) {
        viewModelScope.launch {
            repository.addPrompt(Prompt(title = title, content = content))
        }
    }

    fun deletePrompt(prompt: Prompt) {
        viewModelScope.launch {
            repository.deletePrompt(prompt)
        }
    }

    fun createNewTab(provider: AiProvider): Flow<Long> = flow {
        val tab = Tab(
            providerId = provider.id,
            title = provider.name,
            url = provider.url,
            isDesktopMode = provider.isDesktopMode
        )
        emit(repository.addTab(tab))
    }

    fun closeTab(tab: Tab) {
        viewModelScope.launch {
            repository.deleteTab(tab)
        }
    }

    fun closeAllTabs() {
        viewModelScope.launch {
            repository.deleteAllTabs()
        }
    }

    fun updateTabNavigationState(tabId: Long, canGoBack: Boolean, canGoForward: Boolean) {
        viewModelScope.launch {
            repository.updateTabNavigationState(tabId, canGoBack, canGoForward)
        }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(private val repository: AiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return WebViewViewModel(repository) as T
        }
    }
}
