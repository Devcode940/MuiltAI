package com.multaihub.app.data.local

import androidx.room.*
import com.multaihub.app.data.model.Tab
import kotlinx.coroutines.flow.Flow

@Dao
interface TabDao {

    @Query("SELECT * FROM tabs ORDER BY lastAccessed DESC")
    fun getAll(): Flow<List<Tab>>

    @Query("SELECT * FROM tabs WHERE id = :id")
    suspend fun getById(id: Long): Tab?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tab: Tab): Long

    @Update
    suspend fun update(tab: Tab)

    @Delete
    suspend fun delete(tab: Tab)

    @Query("DELETE FROM tabs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM tabs")
    suspend fun deleteAll()

    @Query("UPDATE tabs SET lastAccessed = :time WHERE id = :id")
    suspend fun updateLastAccessed(id: Long, time: Long = System.currentTimeMillis())

    @Query("UPDATE tabs SET title = :title, url = :url WHERE id = :id")
    suspend fun updateTitleAndUrl(id: Long, title: String, url: String)

    @Query("UPDATE tabs SET canGoBack = :canGoBack, canGoForward = :canGoForward WHERE id = :id")
    suspend fun updateNavigationState(id: Long, canGoBack: Boolean, canGoForward: Boolean)
}
