package com.multaihub.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.utils.AppConstants.RECENT_PROVIDERS_LIMIT
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object for AI provider operations.
 *
 * Queries are designed to push filtering work into SQLite rather than returning
 * full catalogs for in-memory filtering. See [com.multaihub.app.data.model.AiProvider]
 * for the indexed columns that accelerate these queries.
 */
@Dao
interface AiProviderDao {

    /** Returns all visible providers ordered by sort priority and name. */
    @Query("SELECT * FROM ai_providers WHERE isHidden = 0 ORDER BY sortOrder ASC, name ASC")
    fun getAllVisible(): Flow<List<AiProvider>>

    /** Returns every provider in the database, including hidden ones. */
    @Query("SELECT * FROM ai_providers ORDER BY sortOrder ASC, name ASC")
    fun getAll(): Flow<List<AiProvider>>

    /**
     * Finds a single provider by its primary key.
     *
     * @param id The provider identifier.
     */
    @Query("SELECT * FROM ai_providers WHERE id = :id")
    suspend fun getById(id: String): AiProvider?

    /**
     * Finds a provider by normalized URL without loading the complete catalog.
     *
     * @param url The normalized URL to look up.
     */
    @Query("SELECT * FROM ai_providers WHERE lower(url) = lower(:url) LIMIT 1")
    suspend fun getByUrl(url: String): AiProvider?

    /**
     * Searches provider metadata in SQLite instead of filtering a full catalog in Compose.
     *
     * @param query The search term matched against name and category.
     */
    @Query(
        """
        SELECT * FROM ai_providers 
        WHERE isHidden = 0 
          AND (name LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') 
        ORDER BY sortOrder ASC, name ASC
        """
    )
    fun search(query: String): Flow<List<AiProvider>>

    /**
     * Returns providers in a specific category.
     *
     * @param category The category name to filter by.
     */
    @Query("SELECT * FROM ai_providers WHERE category = :category AND isHidden = 0 ORDER BY sortOrder ASC")
    fun getByCategory(category: String): Flow<List<AiProvider>>

    /** Returns favorited providers ordered by most recent use. */
    @Query("SELECT * FROM ai_providers WHERE isFavorite = 1 AND isHidden = 0 ORDER BY lastUsed DESC")
    fun getFavorites(): Flow<List<AiProvider>>

    /**
     * Returns the most recently used providers.
     *
     * Limited to [RECENT_PROVIDERS_LIMIT] entries.
     */
    @Query(
        "SELECT * FROM ai_providers WHERE isHidden = 0 AND lastUsed > 0 " +
        "ORDER BY lastUsed DESC LIMIT $RECENT_PROVIDERS_LIMIT"
    )
    fun getRecent(): Flow<List<AiProvider>>

    /** Inserts a single provider, replacing on conflict. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provider: AiProvider)

    /** Inserts multiple providers, replacing on conflict. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(providers: List<AiProvider>)

    /** Updates an existing provider. */
    @Update
    suspend fun update(provider: AiProvider)

    /** Deletes a provider. */
    @Delete
    suspend fun delete(provider: AiProvider)

    /** Deletes only user-created providers while preserving built-in catalog entries. */
    @Query("DELETE FROM ai_providers WHERE isCustom = 1")
    suspend fun deleteCustomProviders()

    /**
     * Updates the last-used timestamp for a provider.
     *
     * @param id The provider identifier.
     * @param time The timestamp in milliseconds; defaults to now.
     */
    @Query("UPDATE ai_providers SET lastUsed = :time WHERE id = :id")
    suspend fun updateLastUsed(id: String, time: Long = System.currentTimeMillis())

    /**
     * Updates the desktop/mobile display mode for a provider.
     *
     * @param id The provider identifier.
     * @param isDesktop `true` for desktop mode, `false` for mobile.
     */
    @Query("UPDATE ai_providers SET isDesktopMode = :isDesktop WHERE id = :id")
    suspend fun updateDesktopMode(id: String, isDesktop: Boolean)

    /**
     * Updates the favorite state for a provider.
     *
     * @param id The provider identifier.
     * @param isFavorite `true` to mark as favorite.
     */
    @Query("UPDATE ai_providers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    /**
     * Updates the visibility of a provider.
     *
     * @param id The provider identifier.
     * @param isHidden `true` to hide the provider from the catalog.
     */
    @Query("UPDATE ai_providers SET isHidden = :isHidden WHERE id = :id")
    suspend fun updateHidden(id: String, isHidden: Boolean)
}
