package com.multaihub.app.data.repository

import com.multaihub.app.data.local.AiProviderDao
import com.multaihub.app.data.local.NoteDao
import com.multaihub.app.data.local.PromptDao
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.model.Note
import com.multaihub.app.data.model.Prompt
import kotlinx.coroutines.flow.Flow

class AiRepository(
    private val aiProviderDao: AiProviderDao,
    private val promptDao: PromptDao,
    private val noteDao: NoteDao
) {

    // AI Providers
    fun getAllVisibleProviders(): Flow<List<AiProvider>> = aiProviderDao.getAllVisible()
    fun getAllProviders(): Flow<List<AiProvider>> = aiProviderDao.getAll()
    fun getProvidersByCategory(category: String): Flow<List<AiProvider>> = aiProviderDao.getByCategory(category)
    fun getFavorites(): Flow<List<AiProvider>> = aiProviderDao.getFavorites()
    fun getRecent(): Flow<List<AiProvider>> = aiProviderDao.getRecent()

    suspend fun getProviderById(id: String): AiProvider? = aiProviderDao.getById(id)

    suspend fun addCustomProvider(provider: AiProvider) = aiProviderDao.insert(provider)
    suspend fun updateProvider(provider: AiProvider) = aiProviderDao.update(provider)
    suspend fun deleteProvider(provider: AiProvider) = aiProviderDao.delete(provider)

    suspend fun updateLastUsed(id: String) = aiProviderDao.updateLastUsed(id)
    suspend fun toggleDesktopMode(id: String, isDesktop: Boolean) = aiProviderDao.updateDesktopMode(id, isDesktop)
    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = aiProviderDao.updateFavorite(id, isFavorite)
    suspend fun setHidden(id: String, isHidden: Boolean) = aiProviderDao.updateHidden(id, isHidden)

    // Prompts
    fun getAllPrompts(): Flow<List<Prompt>> = promptDao.getAll()
    suspend fun addPrompt(prompt: Prompt) = promptDao.insert(prompt)
    suspend fun updatePrompt(prompt: Prompt) = promptDao.update(prompt)
    suspend fun deletePrompt(prompt: Prompt) = promptDao.delete(prompt)

    // Notes
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAll()
    suspend fun addNote(note: Note) = noteDao.insert(note)
    suspend fun deleteNote(note: Note) = noteDao.delete(note)
}
