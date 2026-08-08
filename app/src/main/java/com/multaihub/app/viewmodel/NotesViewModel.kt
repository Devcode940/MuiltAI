package com.multaihub.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.multaihub.app.data.model.Note
import com.multaihub.app.data.repository.AiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Owns note search and persistence state for the Notes screen. */
class NotesViewModel(private val repository: AiRepository) : ViewModel() {
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allNotes: StateFlow<List<Note>> = repository.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val filteredNotes: StateFlow<List<Note>> = combine(allNotes, _searchQuery) { notes, query ->
        if (query.isBlank()) notes else notes.filter { note ->
            note.content.contains(query, ignoreCase = true) ||
                note.sourceAi.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Updates the note search query with a bounded input length. */
    fun updateSearch(query: String) {
        _searchQuery.value = query.take(200)
    }

    /** Adds a non-empty note after trimming user input. */
    fun addNote(content: String, sourceAi: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val cleanContent = content.trim()
                if (cleanContent.isBlank()) {
                    _error.value = "Enter some note content first."
                    return@launch
                }
                if (cleanContent.length > 20_000) {
                    _error.value = "Notes must be 20,000 characters or fewer."
                    return@launch
                }
                repository.addNote(
                    Note(
                        content = cleanContent,
                        sourceAi = sourceAi.trim().take(80)
                    )
                )
            } catch (_: Exception) {
                _error.value = "Could not save the note. Please try again."
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Deletes a saved note. */
    fun deleteNote(note: Note) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteNote(note)
            } catch (_: Exception) {
                _error.value = "Could not delete the note. Please try again."
            } finally {
                _isLoading.value = false
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
            if (modelClass.isAssignableFrom(NotesViewModel::class.java)) {
                return NotesViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
