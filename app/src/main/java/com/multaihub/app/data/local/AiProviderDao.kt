package com.multaihub.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.multaihub.app.data.model.AiProvider
import kotlinx.coroutines.flow.Flow

/** Room queries for AI providers. */
@Dao
interface AiProviderDao {
    fun getAllVisible(): Flow<List<AiProvider>>

    @Query("SELECT * FROM ai_providers WHERE isHidden = 0 ORDER BY sortOrder ASC, name ASC")
    fun getAllVisible(): Flow<List<AiProvider>>

    @Query("SELECT * FROM ai_providers ORDER BY sortOrder ASC, name ASC")
    fun getAll(): Flow<List<AiProvider>>

    @Query("SELECT * FROM ai_providers WHERE id = :id")
    suspend fun getById(id: String): AiProvider?

    /** Finds a provider by normalized URL without loading the complete provider table. */
    @Query("SELECT * FROM ai_providers WHERE lower(url) = lower(:url) LIMIT 1")
    suspend fun getByUrl(url: String): AiProvider?

    @Query("SELECT * FROM ai_providers WHERE category = :category AND isHidden = 0 ORDER BY sortOrder ASC")
    fun getByCategory(category: String): Flow<List<AiProvider>>

    @Query("SELECT * FROM ai_providers WHERE isFavorite = 1 AND isHidden = 0 ORDER BY lastUsed DESC")
    fun getFavorites(): Flow<List<AiProvider>>

    @Query("SELECT * FROM ai_providers WHERE isHidden = 0 AND lastUsed > 0 ORDER BY lastUsed DESC LIMIT 6")
    fun getRecent(): Flow<List<AiProvider>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(provider: AiProvider)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(providers: List<AiProvider>)

    @Update
    suspend fun update(provider: AiProvider)

    @Delete
    suspend fun delete(provider: AiProvider)

    @Query("UPDATE ai_providers SET lastUsed = :time WHERE id = :id")
    suspend fun updateLastUsed(id: String, time: Long = System.currentTimeMillis())

    @Query("UPDATE ai_providers SET isDesktopMode = :isDesktop WHERE id = :id")
    suspend fun updateDesktopMode(id: String, isDesktop: Boolean)

    @Query("UPDATE ai_providers SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE ai_providers SET isHidden = :isHidden WHERE id = :id")
    suspend fun updateHidden(id: String, isHidden: Boolean)
}
