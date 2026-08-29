package com.multaihub.app.webview

import android.annotation.SuppressLint
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import com.multaihub.app.utils.UrlValidator
import com.multaihub.app.utils.UserAgent

/**
 * Central security and performance policy for every application WebView.
 *
 * This object defines the production-hardened configuration applied to all WebView instances
 * used to render AI provider websites. Every setting here is chosen to reduce the attack
 * surface while preserving the functionality required by modern web applications.
 */
object WebViewPolicy {

    /**
     * Configures per-provider WebView data isolation.
     *
     * **Must be called before any WebView instance is created** — typically from
     * [android.app.Application.onCreate]. This ensures that cookies, localStorage, and
     * other WebView data are siloed per provider, preventing cross-site tracking.
     *
     * @param suffix Unique identifier for the isolation context (e.g., provider ID).
     *   Must only contain alphanumeric characters, underscores, and hyphens.
     * @return `true` if the suffix was applied successfully; `false` on API levels that
     *   do not support this feature or if the suffix contains invalid characters.
     */
    fun setDataDirectorySuffix(suffix: String): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) return false
        if (!suffix.matches(Regex("[A-Za-z0-9_-]+"))) return false
        return try {
            WebView.setDataDirectorySuffix(suffix)
            true
        } catch (_: IllegalStateException) {
            // WebView has already been created in this process; cannot change suffix.
            false
        }
    }

    /**
     * Applies the baseline production security configuration to a WebView.
     *
     * Disables file access, popups, mixed content, and other dangerous features while
     * enabling JavaScript and DOM storage (required for AI web apps to function).
     *
     * @param webView The WebView to configure.
     * @param desktopMode When `true`, uses a desktop user-agent string.
     */
    @SuppressLint("SetJavaScriptEnabled")
    fun apply(webView: WebView, desktopMode: Boolean) {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false

            // Security: never allow mixed HTTP/HTTPS content
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW

            // Security: media must be user-initiated
            mediaPlaybackRequiresUserGesture = true

            // Security: block access to local files and content providers
            allowFileAccess = false
            allowContentAccess = false

            // Security: disable popups and window creation
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false

            // Security: enable Safe Browsing protection against known threats
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                setSafeBrowsingEnabled(true)
            }

            // Security: disable form data retention
            saveFormData = false

            // Security: disable password saving (delegated to password managers)
            @Suppress("DEPRECATION")
            savePassword = false

            userAgentString = UserAgent.get(desktopMode)
        }
    }

    /**
     * Returns a URL safe to load in a WebView, or `null` when navigation must be rejected.
     *
     * Enforces HTTPS and rejects dangerous URI schemes.
     */
    fun safeUrl(url: String): String? =
        UrlValidator.validateAndEnforceHttps(url, enforceHttps = true)
}
