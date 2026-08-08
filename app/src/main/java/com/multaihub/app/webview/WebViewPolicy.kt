package com.multaihub.app.webview

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import com.multaihub.app.utils.UrlValidator
import com.multaihub.app.utils.UserAgent

/** Central security and performance policy for every application WebView. */
object WebViewPolicy {
    /** Applies the baseline production configuration to a WebView. */
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
            mixedContentMode = WebSettings.MIXED_CONTENT_NEVER_ALLOW
            mediaPlaybackRequiresUserGesture = true
            allowFileAccess = false
            allowContentAccess = false
            setSupportMultipleWindows(false)
            javaScriptCanOpenWindowsAutomatically = false
            userAgentString = UserAgent.get(desktopMode)
        }
    }

    /** Returns a URL safe to load, or null when navigation must be rejected. */
    fun safeUrl(url: String): String? =
        UrlValidator.validateAndEnforceHttps(url, enforceHttps = true)
}
