package com.multaihub.app.data.local

import androidx.room.*
import com.multaihub.app.data.model.Prompt
import kotlinx.coroutines.flow.Flow

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
}
