package com.multaihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multaihub.app.data.model.Note
import com.multaihub.app.data.repository.AiRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class NotesViewModel(private val repository: AiRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allNotes: StateFlow<List<Note>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredNotes: StateFlow<List<Note>> = combine(allNotes, _searchQuery) { notes, query ->
        if (query.isBlank()) notes else notes.filter { n ->
            n.content.contains(query, ignoreCase = true) ||
            n.sourceAi.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearch(query: String) {
        _searchQuery.value = query
    }

    fun addNote(content: String, sourceAi: String = "") {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                if (content.isBlank()) {
                    _error.value = "Cannot be empty"
                    _isLoading.value = false
                    return@launch
                }
                repository.addNote(Note(content = content, sourceAi = sourceAi))
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun deleteNote(note: Note) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteNote(note)
                _isLoading.value = false
            } catch (e: Exception) {
                _error.value = "Failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    class Factory(private val repository: AiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotesViewModel(repository) as T
        }
    }
}
