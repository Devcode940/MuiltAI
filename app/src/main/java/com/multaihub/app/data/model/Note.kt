package com.multaihub.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val sourceAi: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
