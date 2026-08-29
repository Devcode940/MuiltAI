package com.multaihub.app.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Persistent definition of an AI provider.
 *
 * Indexes are added to columns frequently used in WHERE and ORDER BY clauses
 * to keep catalog queries responsive as the database grows.
 *
 * @property id Unique provider identifier (UUID for custom providers, stable slug for built-in).
 * @property name Display name shown in the UI.
 * @property url Normalized HTTPS URL loaded by the WebView.
 * @property iconUrl Optional URL for a provider icon (currently unused).
 * @property category UI grouping category.
 * @property isCustom `true` for user-added providers, `false` for built-in catalog entries.
 * @property isDesktopMode When `true`, the WebView uses a desktop user-agent.
 * @property isFavorite When `true`, the provider appears in the Favorites category.
 * @property isHidden When `true`, the provider is excluded from the main catalog.
 * @property sortOrder Display priority for built-in providers.
 * @property lastUsed Epoch-millis timestamp of the last time the provider was opened.
 */
@Entity(
    tableName = "ai_providers",
    indices = [
        Index(value = ["category"]),
        Index(value = ["isFavorite"]),
        Index(value = ["isHidden"]),
        Index(value = ["lastUsed"]),
        Index(value = ["url"], unique = false),
        Index(value = ["isHidden", "category"]),
        Index(value = ["isHidden", "isFavorite"]),
        Index(value = ["isHidden", "lastUsed"])
    ]
)
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

/**
 * Provider categories exposed by the home catalog.
 *
 * @property displayName Human-readable name shown in the UI.
 */
enum class AiCategory(val displayName: String) {
    ALL("All"),
    FAVORITES("Favorites"),
    CHAT("Chat"),
    CODING("Coding"),
    WRITING("Writing"),
    IMAGE("Image"),
    SEARCH("Search"),
    FREE("Free"),
    CUSTOM("Custom")
}
