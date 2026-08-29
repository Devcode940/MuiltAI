package com.multaihub.app.utils

/**
 * Application-wide constants.
 *
 * Centralizing numeric and string constants here eliminates "magic numbers"
 * scattered through the codebase and makes it easy to adjust policies in one place.
 */
object AppConstants {

    // region Input validation limits
    /** Maximum length for a user-entered URL before it is rejected. */
    const val MAX_URL_LENGTH = 2048

    /** Maximum length for an AI provider display name. */
    const val MAX_PROVIDER_NAME_LENGTH = 80

    /** Maximum length for a category name. */
    const val MAX_CATEGORY_NAME_LENGTH = 32

    /** Maximum length for a search query sent to the database. */
    const val MAX_SEARCH_QUERY_LENGTH = 100

    /** Maximum length for a persisted tab title. */
    const val MAX_TAB_TITLE_LENGTH = 200

    /** Maximum length for a downloaded filename (Android DownloadManager limit). */
    const val MAX_DOWNLOAD_FILENAME_LENGTH = 180
    // endregion

    // region UI / UX timing
    /** Milliseconds to debounce search input before querying the database. */
    const val SEARCH_DEBOUNCE_MS = 150L

    /** Milliseconds to keep a StateFlow subscription alive after the last collector leaves. */
    const val FLOW_SHARING_TIMEOUT_MS = 5_000L
    // endregion

    // region Database
    /** Maximum number of recent providers shown on the home screen. */
    const val RECENT_PROVIDERS_LIMIT = 6

    /** Sort order value that places custom providers after built-in ones. */
    const val CUSTOM_PROVIDER_SORT_ORDER = Int.MAX_VALUE
    // endregion

    // region Network
    /** HTTPS URI scheme. */
    const val SCHEME_HTTPS = "https"

    /** HTTP URI scheme. */
    const val SCHEME_HTTP = "http"
    // endregion
}
