package com.multaihub.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persistent browser tab state.
 *
 * Stores the navigation context for an open AI provider so it can be restored
 * across process death or configuration changes.
 *
 * @property id Auto-generated primary key.
 * @property providerId Reference to the associated [AiProvider.id].
 * @property title Current page title.
 * @property url Current page URL.
 * @property isDesktopMode Whether the tab is using a desktop user-agent.
 * @property canGoBack Whether the WebView has back-history.
 * @property canGoForward Whether the WebView has forward-history.
 * @property createdAt Epoch-millis when the tab was created.
 * @property lastAccessed Epoch-millis when the tab was last active.
 */
@Entity(
    tableName = "tabs",
    indices = [
        Index(value = ["providerId"]),
        Index(value = ["lastAccessed"])
    ]
)
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
