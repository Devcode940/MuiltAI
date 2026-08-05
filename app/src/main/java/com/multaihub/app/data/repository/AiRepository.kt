package com.multaihub.app.data.repository

import com.multaihub.app.data.local.AiProviderDao
import com.multaihub.app.data.local.NoteDao
import com.multaihub.app.data.local.PromptDao
import com.multaihub.app.data.local.TabDao
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.model.Note
import com.multaihub.app.data.model.Prompt
import com.multaihub.app.data.model.Tab
import kotlinx.coroutines.flow.Flow

class AiRepository(
    private val aiProviderDao: AiProviderDao,
    private val promptDao: PromptDao,
    private val noteDao: NoteDao,
    private val tabDao: TabDao
) {

    // AI Providers
    fun getAllVisibleProviders(): Flow<List<AiProvider>> = aiProviderDao.getAllVisible()
    fun getAllProviders(): Flow<List<AiProvider>> = aiProviderDao.getAll()
    fun getProvidersByCategory(category: String): Flow<List<AiProvider>> = aiProviderDao.getByCategory(category)
    fun getFavorites(): Flow<List<AiProvider>> = aiProviderDao.getFavorites()
    fun getRecent(): Flow<List<AiProvider>> = aiProviderDao.getRecent()

    suspend fun getProviderById(id: String): AiProvider? = try {
        aiProviderDao.getById(id)
    } catch (e: Exception) {
        null
    }

    suspend fun addCustomProvider(provider: AiProvider) = try {
        aiProviderDao.insert(provider)
    } catch (e: Exception) {
        throw RepositoryException("Failed to add provider", e)
    }

    suspend fun updateProvider(provider: AiProvider) = try {
        aiProviderDao.update(provider)
    } catch (e: Exception) {
        throw RepositoryException("Failed to update provider", e)
    }

    suspend fun deleteProvider(provider: AiProvider) = try {
        aiProviderDao.delete(provider)
    } catch (e: Exception) {
        throw RepositoryException("Failed to delete provider", e)
    }

    suspend fun updateLastUsed(id: String) = try {
        aiProviderDao.updateLastUsed(id)
    } catch (e: Exception) {}

    suspend fun toggleDesktopMode(id: String, isDesktop: Boolean) = try {
        aiProviderDao.updateDesktopMode(id, isDesktop)
    } catch (e: Exception) {}

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = try {
        aiProviderDao.updateFavorite(id, isFavorite)
    } catch (e: Exception) {}

    suspend fun setHidden(id: String, isHidden: Boolean) = try {
        aiProviderDao.updateHidden(id, isHidden)
    } catch (e: Exception) {}

    // Prompts
    fun getAllPrompts(): Flow<List<Prompt>> = promptDao.getAll()
    suspend fun addPrompt(prompt: Prompt) = try {
        promptDao.insert(prompt)
    } catch (e: Exception) {
        throw RepositoryException("Failed to add prompt", e)
    }
    suspend fun updatePrompt(prompt: Prompt) = try {
        promptDao.update(prompt)
    } catch (e: Exception) {
        throw RepositoryException("Failed to update prompt", e)
    }
    suspend fun deletePrompt(prompt: Prompt) = try {
        promptDao.delete(prompt)
    } catch (e: Exception) {
        throw RepositoryException("Failed to delete prompt", e)
    }

    // Notes
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAll()
    suspend fun addNote(note: Note) = try {
        noteDao.insert(note)
    } catch (e: Exception) {
        throw RepositoryException("Failed to add note", e)
    }
    suspend fun deleteNote(note: Note) = try {
        noteDao.delete(note)
    } catch (e: Exception) {
        throw RepositoryException("Failed to delete note", e)
    }

    // Tabs
    fun getAllTabs(): Flow<List<Tab>> = tabDao.getAll()
    suspend fun getTabById(id: Long): Tab? = try {
        tabDao.getById(id)
    } catch (e: Exception) {
        null
    }
    suspend fun addTab(tab: Tab): Long = try {
        tabDao.insert(tab)
    } catch (e: Exception) {
        throw RepositoryException("Failed to add tab", e)
    }
    suspend fun updateTab(tab: Tab) = try {
        tabDao.update(tab)
    } catch (e: Exception) {
        throw RepositoryException("Failed to update tab", e)
    }
    suspend fun deleteTab(tab: Tab) = try {
        tabDao.delete(tab)
    } catch (e: Exception) {
        throw RepositoryException("Failed to delete tab", e)
    }
    suspend fun deleteAllTabs() = try {
        tabDao.deleteAll()
    } catch (e: Exception) {
        throw RepositoryException("Failed to delete all tabs", e)
    }
    suspend fun updateTabLastAccessed(id: Long) = try {
        tabDao.updateLastAccessed(id)
    } catch (e: Exception) {}
    suspend fun updateTabNavigationState(id: Long, canGoBack: Boolean, canGoForward: Boolean) = try {
        tabDao.updateNavigationState(id, canGoBack, canGoForward)
    } catch (e: Exception) {}
}

class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
