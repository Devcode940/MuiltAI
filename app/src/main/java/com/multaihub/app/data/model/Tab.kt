package com.multaihub.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabs")
data class Tab(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val providerId: String,
    val title: String,
    val url: String,
    val isDesktopMode: Boolean = false,
    val canGoBack: Boolean = false,
    val canGoForward: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessed: Long = System.currentTimeMillis()
)
