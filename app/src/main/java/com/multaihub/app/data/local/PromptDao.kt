package com.multaihub.app.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.multaihub.app.data.model.Prompt
import kotlinx.coroutines.flow.Flow

/** Room persistence operations for saved prompts. */
@Dao
interface PromptDao {
    @Query("SELECT * FROM prompts ORDER BY createdAt DESC")
    fun getAll(): Flow<List<Prompt>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(prompt: Prompt): Long

    @Update
    suspend fun update(prompt: Prompt)

    @Delete
    suspend fun delete(prompt: Prompt)

    @Query("DELETE FROM prompts WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Removes all user-created prompts in one SQL operation. */
    @Query("DELETE FROM prompts")
    suspend fun deleteAll()
}
