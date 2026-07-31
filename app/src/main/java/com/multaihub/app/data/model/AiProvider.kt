package com.multaihub.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ai_providers")
data class AiProvider(
    @PrimaryKey
    val id: String,
    val name: String,
    val url: String,
    val iconUrl: String = "",
    val category: String = "Chat",
    val isCustom: Boolean = false,
    val isDesktopMode: Boolean = false,
    val isFavorite: Boolean = false,
    val isHidden: Boolean = false,
    val sortOrder: Int = 0,
    val lastUsed: Long = 0L
)

enum class AiCategory(val displayName: String) {
    ALL("All"),
    CHAT("Chat"),
    CODING("Coding"),
    WRITING("Writing"),
    IMAGE("Image"),
    SEARCH("Search"),
    FREE("Free"),
    CUSTOM("Custom")
}
