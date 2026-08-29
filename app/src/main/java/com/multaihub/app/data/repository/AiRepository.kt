package com.multaihub.app.data.repository

import com.multaihub.app.data.local.AiProviderDao
import com.multaihub.app.data.local.NoteDao
import com.multaihub.app.data.local.PromptDao
import com.multaihub.app.data.local.TabDao
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.data.model.Note
import com.multaihub.app.data.model.Prompt
import com.multaihub.app.data.model.Tab
import com.multaihub.app.utils.AppConstants.MAX_SEARCH_QUERY_LENGTH
import com.multaihub.app.utils.AppConstants.MAX_TAB_TITLE_LENGTH
import com.multaihub.app.utils.UrlValidator
import kotlinx.coroutines.flow.Flow

/**
 * Single data-access boundary for the application.
 *
 * All persistence operations route through this class so that database failures are
 * consistently wrapped in [RepositoryException] and reach the presentation layer
 * instead of being silently lost or crashing the UI coroutine.
 *
 * @property aiProviderDao DAO for AI provider operations.
 * @property promptDao DAO for prompt library operations.
 * @property noteDao DAO for note operations.
 * @property tabDao DAO for browser tab operations.
 */
class AiRepository(
    private val aiProviderDao: AiProviderDao,
    private val promptDao: PromptDao,
    private val noteDao: NoteDao,
    private val tabDao: TabDao
) {

    /** Returns a flow of all visible providers. */
    fun getAllVisibleProviders(): Flow<List<AiProvider>> = aiProviderDao.getAllVisible()

    /** Returns a flow of every provider, including hidden ones. */
    fun getAllProviders(): Flow<List<AiProvider>> = aiProviderDao.getAll()

    /**
     * Searches providers by name or category.
     *
     * @param query The search term; truncated to a safe maximum length.
     */
    fun searchProviders(query: String): Flow<List<AiProvider>> =
        aiProviderDao.search(query.trim().take(MAX_SEARCH_QUERY_LENGTH))

    /**
     * Returns providers filtered by category.
     *
     * @param category The category name to filter by.
     */
    fun getProvidersByCategory(category: String): Flow<List<AiProvider>> =
        aiProviderDao.getByCategory(category)

    /** Returns favorited providers ordered by most recent use. */
    fun getFavorites(): Flow<List<AiProvider>> = aiProviderDao.getFavorites()

    /** Returns the most recently used providers. */
    fun getRecent(): Flow<List<AiProvider>> = aiProviderDao.getRecent()

    /**
     * Finds a provider by its identifier.
     *
     * @param id The provider identifier.
     * @throws RepositoryException if the database operation fails.
     */
    suspend fun getProviderById(id: String): AiProvider? = try {
        aiProviderDao.getById(id)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to load provider", cause)
    }

    /**
     * Checks whether a provider with the given URL already exists.
     *
     * @param url The normalized URL to check.
     * @throws RepositoryException if the database operation fails.
     */
    suspend fun getProviderByUrl(url: String): AiProvider? = try {
        aiProviderDao.getByUrl(url)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to check provider URL", cause)
    }

    /**
     * Adds a custom provider after enforcing the WebView URL policy.
     *
     * Validates the URL, upgrades to HTTPS, and checks for duplicates before insertion.
     *
     * @param provider The provider to add.
     * @throws RepositoryException if validation fails or the database operation fails.
     */
    suspend fun addCustomProvider(provider: AiProvider) = try {
        val safeUrl = UrlValidator.validateAndEnforceHttps(provider.url, enforceHttps = true)
            ?: throw IllegalArgumentException("Invalid provider URL")
        if (aiProviderDao.getByUrl(safeUrl) != null) {
            throw IllegalArgumentException("Provider URL already exists")
        }
        aiProviderDao.insert(provider.copy(url = safeUrl, isCustom = true))
    } catch (cause: Exception) {
        throw RepositoryException("Failed to add provider", cause)
    }

    /**
     * Updates a provider after validating its URL.
     *
     * @param provider The provider to update.
     * @throws RepositoryException if validation fails or the database operation fails.
     */
    suspend fun updateProvider(provider: AiProvider) = try {
        val safeUrl = UrlValidator.validateAndEnforceHttps(provider.url, enforceHttps = true)
            ?: throw IllegalArgumentException("Invalid provider URL")
        aiProviderDao.update(provider.copy(url = safeUrl))
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update provider", cause)
    }

    /**
     * Deletes a provider.
     *
     * @param provider The provider to delete.
     * @throws RepositoryException if the database operation fails.
     */
    suspend fun deleteProvider(provider: AiProvider) = try {
        aiProviderDao.delete(provider)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to delete provider", cause)
    }

    /**
     * Deletes all user-created custom providers.
     *
     * @throws RepositoryException if the database operation fails.
     */
    suspend fun deleteCustomProviders() = try {
        aiProviderDao.deleteCustomProviders()
    } catch (cause: Exception) {
        throw RepositoryException("Failed to clear custom providers", cause)
    }

    /**
     * Updates the last-used timestamp for a provider.
     *
     * @param id The provider identifier.
     * @throws RepositoryException if the database operation fails.
     */
    suspend fun updateLastUsed(id: String) = try {
        aiProviderDao.updateLastUsed(id)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update provider usage", cause)
    }

    /**
     * Updates the desktop/mobile display mode for a provider.
     *
     * @param id The provider identifier.
     * @param isDesktop `true` for desktop mode, `false` for mobile.
     * @throws RepositoryException if the database operation fails.
     */
    suspend fun toggleDesktopMode(id: String, isDesktop: Boolean) = try {
        aiProviderDao.updateDesktopMode(id, isDesktop)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update display mode", cause)
    }

    /**
     * Updates the favorite state for a provider.
     *
     * @param id The provider identifier.
     * @param isFavorite `true` to mark as favorite.
     * @throws RepositoryException if the database operation fails.
     */
    suspend fun toggleFavorite(id: String, isFavorite: Boolean) = try {
        aiProviderDao.updateFavorite(id, isFavorite)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update favorite state", cause)
    }

    /**
     * Updates the visibility of a provider.
     *
     * @param id The provider identifier.
     * @param isHidden `true` to hide the provider.
     * @throws RepositoryException if the database operation fails.
     */
    suspend fun setHidden(id: String, isHidden: Boolean) = try {
        aiProviderDao.updateHidden(id, isHidden)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update visibility", cause)
    }

    // region Prompt operations
    /** Returns all saved prompts. */
    fun getAllPrompts(): Flow<List<Prompt>> = promptDao.getAll()

    /** Adds a new prompt. */
    suspend fun addPrompt(prompt: Prompt) = try {
        promptDao.insert(prompt)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to add prompt", cause)
    }

    /** Updates an existing prompt. */
    suspend fun updatePrompt(prompt: Prompt) = try {
        promptDao.update(prompt)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update prompt", cause)
    }

    /** Deletes a prompt. */
    suspend fun deletePrompt(prompt: Prompt) = try {
        promptDao.delete(prompt)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to delete prompt", cause)
    }

    /** Deletes all saved prompts. */
    suspend fun deleteAllPrompts() = try {
        promptDao.deleteAll()
    } catch (cause: Exception) {
        throw RepositoryException("Failed to clear prompts", cause)
    }
    // endregion

    // region Note operations
    /** Returns all saved notes. */
    fun getAllNotes(): Flow<List<Note>> = noteDao.getAll()

    /** Adds a new note. */
    suspend fun addNote(note: Note) = try {
        noteDao.insert(note)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to add note", cause)
    }

    /** Deletes a note. */
    suspend fun deleteNote(note: Note) = try {
        noteDao.delete(note)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to delete note", cause)
    }

    /** Deletes all saved notes. */
    suspend fun deleteAllNotes() = try {
        noteDao.deleteAll()
    } catch (cause: Exception) {
        throw RepositoryException("Failed to clear notes", cause)
    }
    // endregion

    // region Tab operations
    /** Returns all open tabs. */
    fun getAllTabs(): Flow<List<Tab>> = tabDao.getAll()

    /** Finds a tab by its identifier. */
    suspend fun getTabById(id: Long): Tab? = try {
        tabDao.getById(id)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to load tab", cause)
    }

    /** Creates a new tab and returns its database identifier. */
    suspend fun addTab(tab: Tab): Long = try {
        tabDao.insert(tab)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to add tab", cause)
    }

    /** Updates an existing tab. */
    suspend fun updateTab(tab: Tab) = try {
        tabDao.update(tab)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update tab", cause)
    }

    /** Closes (deletes) a single tab. */
    suspend fun deleteTab(tab: Tab) = try {
        tabDao.delete(tab)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to delete tab", cause)
    }

    /** Closes (deletes) all persisted tabs. */
    suspend fun deleteAllTabs() = try {
        tabDao.deleteAll()
    } catch (cause: Exception) {
        throw RepositoryException("Failed to delete all tabs", cause)
    }

    /** Updates the last-accessed timestamp for a tab. */
    suspend fun updateTabLastAccessed(id: Long) = try {
        tabDao.updateLastAccessed(id)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update tab access time", cause)
    }

    /** Updates the back/forward navigation state for a tab. */
    suspend fun updateTabNavigationState(id: Long, canGoBack: Boolean, canGoForward: Boolean) = try {
        tabDao.updateNavigationState(id, canGoBack, canGoForward)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to update tab navigation state", cause)
    }

    /**
     * Updates the current page title and URL for a tab.
     *
     * The URL is re-validated and the title is truncated to a safe maximum length.
     */
    suspend fun updateTabPage(id: Long, title: String, url: String) = try {
        val safeUrl = UrlValidator.validateAndEnforceHttps(url, enforceHttps = true)
            ?: throw IllegalArgumentException("Invalid tab URL")
        tabDao.updateTitleAndUrl(id, title.trim().take(MAX_TAB_TITLE_LENGTH), safeUrl)
    } catch (cause: Exception) {
        throw RepositoryException("Failed to save tab page", cause)
    }
    // endregion
}

/**
 * Safe application-level wrapper around persistence failures.
 *
 * Presentation code catches this exception type rather than raw database exceptions,
 * keeping the error boundary explicit and preventing crashes from transient IO failures.
 *
 * @param message Human-readable error description.
 * @param cause The underlying failure, if available.
 */
class RepositoryException(message: String, cause: Throwable? = null) : Exception(message, cause)
