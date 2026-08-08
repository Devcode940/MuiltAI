package com.multaihub.app.utils

import android.net.Uri
import android.util.Patterns

/** Validates URLs before they are persisted or loaded by WebView. */
object UrlValidator {
    private val allowedSchemes = setOf("http", "https")

    /**
     * Normalizes a user-entered URL and optionally upgrades HTTP to HTTPS.
     * // WHY: Custom providers are an untrusted input boundary; only HTTP(S) URLs are allowed.
     */
    fun validateAndEnforceHttps(url: String, enforceHttps: Boolean = true): String? {
        val trimmed = url.trim()
        if (trimmed.isBlank() || trimmed.length > 2048) return null

        return try {
            val candidate = if (Uri.parse(trimmed).scheme.isNullOrBlank()) {
                "https://$trimmed"
            } else {
                trimmed
            }
            val uri = Uri.parse(candidate)
            val scheme = uri.scheme?.lowercase() ?: return null
            val host = uri.host?.trim()?.lowercase() ?: return null

            if (scheme !in allowedSchemes) return null
            if (host.isBlank() || !Patterns.DOMAIN_NAME.matcher(host).matches()) return null

            if (enforceHttps && scheme == "http") {
                uri.buildUpon().scheme("https").build().toString()
            } else {
                uri.toString()
            }
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    /** Returns true only for HTTPS URLs. */
    fun isHttps(url: String): Boolean = try {
        Uri.parse(url).scheme.equals("https", ignoreCase = true)
    } catch (_: IllegalArgumentException) {
        false
    }

    /** Converts an HTTP URL to HTTPS without string-level replacement. */
    fun toHttps(url: String): String = try {
        val uri = Uri.parse(url)
        if (uri.scheme.equals("http", ignoreCase = true)) {
            uri.buildUpon().scheme("https").build().toString()
        } else {
            url
        }
    } catch (_: IllegalArgumentException) {
        url
    }

    /** Checks whether the supplied value is a valid HTTP(S) URL. */
    fun isValidUrl(url: String): Boolean =
        validateAndEnforceHttps(url, enforceHttps = false) != null

    /** Returns the normalized host, or null for an invalid URL. */
    fun getDomain(url: String): String? =
        validateAndEnforceHttps(url, enforceHttps = false)?.let { Uri.parse(it).host }

    /**
     * Checks whether a URL is safe for WebView navigation.
     * // WHY: Blocks javascript:, file:, content:, data:, intent:, and custom schemes.
     */
    fun isSafeWebNavigation(url: String): Boolean =
        validateAndEnforceHttps(url, enforceHttps = false) != null
}
