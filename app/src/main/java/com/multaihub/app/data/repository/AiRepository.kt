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

/**
 * Single data-access boundary for the application.
 * // WHY: Persistence failures must reach the presentation layer instead of being silently lost.
 */
class AiRepository(
    private val aiProviderDao: AiProviderDao,
    private val promptDao: PromptDao,
    private val noteDao: NoteDao,
    private val tabDao: TabDao
) {
    fun getAllVisibleProviders(): Flow<List<AiProvider>> = aiProviderDao.getAllVisible()
    fun getAllProviders(): Flow<List<AiProvider>> = aiProviderDao.getAll()
    fun getProvidersByCategory(category: String): Flow<List<AiProvider>> = aiProviderDao.getByCategory(category)
    fun getFavorites(): Flow<List<AiProvider>> = aiProviderDao.getFavorites()
    fun getRecent(): Flow<List<AiProvider>> = aiProviderDao.getRecent()

    suspend fun getProviderById(id: String): AiProvider? = try {
        aiProviderDao.getById(id)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to load provider", cause)
    }

    /** Looks up a provider by URL without loading the complete provider catalog. */
    suspend fun getProviderByUrl(url: String): AiProvider? = try {
        aiProviderDao.getByUrl(url)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to check provider URL", cause)
    }

    suspend fun addCustomProvider(provider: AiProvider) = try {
        aiProviderDao.insert(provider)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to add provider", cause)
    }

    suspend fun updateProvider(provider: AiProvider) = try {
        aiProviderDao.update(provider)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update provider", cause)
    }

    suspend fun deleteProvider(provider: AiProvider) = try {
        aiProviderDao.delete(provider)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to delete provider", cause)
    }

    /** Removes user-created providers without deleting the built-in provider catalog. */
    suspend fun deleteCustomProviders() = try {
        aiProviderDao.deleteCustomProviders()
    } catch (cause: Exception) {
        throw RepositoryException("Failed to clear custom providers", cause)
    }

    suspend fun updateLastUsed(id: String) = try {
        aiProviderDao.updateLastUsed(id)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update provider usage", cause)
    }

    suspend fun toggleDesktopMode(id: String, isDesktop: Boolean) = try {
        aiProviderDao.updateDesktopMode(id, isDesktop)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update display mode", cause)
    }

    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = try {
        aiProviderDao.updateFavorite(id, isFavorite)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update favorite state", cause)
    }

    suspend fun setHidden(id: String, isHidden: Boolean) = try {
        aiProviderDao.updateHidden(id, isHidden)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update visibility", cause)
    }

    fun getAllPrompts(): Flow<List<Prompt>> = promptDao.getAll()

    suspend fun addPrompt(prompt: Prompt) = try {
        promptDao.insert(prompt)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to add prompt", cause)
    }

    suspend fun updatePrompt(prompt: Prompt) = try {
        promptDao.update(prompt)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update prompt", cause)
    }

    suspend fun deletePrompt(prompt: Prompt) = try {
        promptDao.delete(prompt)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to delete prompt", cause)
    }

    /** Removes all saved prompts. */
    suspend fun deleteAllPrompts() = try {
        promptDao.deleteAll()
    } catch (cause: Exception) {
        throw RepositoryException("Failed to clear prompts", cause)
    }

    fun getAllNotes(): Flow<List<Note>> = noteDao.getAll()

    suspend fun addNote(note: Note) = try {
        noteDao.insert(note)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to add note", cause)
    }

    suspend fun deleteNote(note: Note) = try {
        noteDao.delete(note)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to delete note", cause)
    }

    /** Removes all saved notes. */
    suspend fun deleteAllNotes() = try {
        noteDao.deleteAll()
    } catch (cause: Exception) {
        throw RepositoryException("Failed to clear notes", cause)
    }

    fun getAllTabs(): Flow<List<Tab>> = tabDao.getAll()

    suspend fun getTabById(id: Long): Tab? = try {
        tabDao.getById(id)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to load tab", cause)
    }

    suspend fun addTab(tab: Tab): Long = try {
        tabDao.insert(tab)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to add tab", cause)
    }

    suspend fun updateTab(tab: Tab) = try {
        tabDao.update(tab)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update tab", cause)
    }

    suspend fun deleteTab(tab: Tab) = try {
        tabDao.delete(tab)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to delete tab", cause)
    }

    suspend fun deleteAllTabs() = try {
        tabDao.deleteAll()
    } catch (cause: Exception) {
        throw RepositoryException("Failed to delete all tabs", cause)
    }

    suspend fun updateTabLastAccessed(id: Long) = try {
        tabDao.updateLastAccessed(id)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update tab access time", cause)
    }

    suspend fun updateTabNavigationState(id: Long, canGoBack: Boolean, canGoForward: Boolean) = try {
        tabDao.updateNavigationState(id, canGoBack, canGoForward)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update tab navigation state", cause)
    }
}

/** Safe application-level wrapper around persistence failures. */
class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
