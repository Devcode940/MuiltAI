package com.multaihub.app.utils

import android.net.Uri
import android.util.Patterns
import com.multaihub.app.utils.AppConstants.MAX_URL_LENGTH
import com.multaihub.app.utils.AppConstants.SCHEME_HTTP
import com.multaihub.app.utils.AppConstants.SCHEME_HTTPS

/**
 * Validates URLs before they are persisted or loaded by WebView.
 *
 * Custom providers are an untrusted input boundary; only HTTP(S) URLs are allowed,
 * and HTTP is transparently upgraded to HTTPS when enforcement is enabled.
 */
object UrlValidator {

    private val allowedSchemes = setOf(SCHEME_HTTP, SCHEME_HTTPS)

    /**
     * Normalizes a user-entered URL and optionally upgrades HTTP to HTTPS.
     *
     * @param url The raw URL string to validate.
     * @param enforceHttps When `true`, HTTP URLs are upgraded to HTTPS.
     * @return The normalized URL, or `null` if validation fails.
     */
    fun validateAndEnforceHttps(url: String, enforceHttps: Boolean = true): String? {
        val trimmed = url.trim()
        if (trimmed.isBlank() || trimmed.length > MAX_URL_LENGTH) return null

        return try {
            val candidate = if (Uri.parse(trimmed).scheme.isNullOrBlank()) {
                "$SCHEME_HTTPS://$trimmed"
            } else {
                trimmed
            }

            val uri = Uri.parse(candidate)
            val scheme = uri.scheme?.lowercase() ?: return null
            val host = uri.host?.trim()?.lowercase() ?: return null

            if (scheme !in allowedSchemes) return null
            if (host.isBlank() || !Patterns.DOMAIN_NAME.matcher(host).matches()) return null

            if (enforceHttps && scheme == SCHEME_HTTP) {
                uri.buildUpon().scheme(SCHEME_HTTPS).build().toString()
            } else {
                uri.toString()
            }
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    /**
     * Returns `true` only for HTTPS URLs.
     *
     * @param url The URL to check.
     */
    fun isHttps(url: String): Boolean = try {
        Uri.parse(url).scheme.equals(SCHEME_HTTPS, ignoreCase = true)
    } catch (_: IllegalArgumentException) {
        false
    }

    /**
     * Converts an HTTP URL to HTTPS without string-level replacement.
     *
     * @param url The URL to upgrade.
     * @return The HTTPS URL, or the original if it was already HTTPS or invalid.
     */
    fun toHttps(url: String): String = try {
        val uri = Uri.parse(url)
        if (uri.scheme.equals(SCHEME_HTTP, ignoreCase = true)) {
            uri.buildUpon().scheme(SCHEME_HTTPS).build().toString()
        } else {
            url
        }
    } catch (_: IllegalArgumentException) {
        url
    }

    /**
     * Checks whether the supplied value is a valid HTTP(S) URL.
     *
     * @param url The URL to validate.
     */
    fun isValidUrl(url: String): Boolean =
        validateAndEnforceHttps(url, enforceHttps = false) != null

    /**
     * Returns the normalized host, or `null` for an invalid URL.
     *
     * @param url The URL to extract the domain from.
     */
    fun getDomain(url: String): String? =
        validateAndEnforceHttps(url, enforceHttps = false)?.let { Uri.parse(it).host }

    /**
     * Checks whether a URL is safe for WebView navigation.
     *
     * Blocks `javascript:`, `file:`, `content:`, `data:`, `intent:`, and custom schemes.
     *
     * @param url The URL to check.
     */
    fun isSafeWebNavigation(url: String): Boolean =
        validateAndEnforceHttps(url, enforceHttps = false) != null
}
