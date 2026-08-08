package com.multaihub.app.webview

import android.webkit.WebBackForwardList

/** Immutable snapshot of browser navigation state used by tab UI and persistence. */
data class WebViewTabState(
    val title: String,
    val url: String,
    val canGoBack: Boolean,
    val canGoForward: Boolean
) {
    companion object {
        /** Creates a state snapshot from a WebView history list. */
        fun fromHistory(history: WebBackForwardList, fallbackTitle: String, fallbackUrl: String): WebViewTabState {
            val current = history.currentItem
            return WebViewTabState(
                title = current?.title?.take(200).orEmpty().ifBlank { fallbackTitle },
                url = current?.url.orEmpty().ifBlank { fallbackUrl },
                canGoBack = history.currentIndex > 0,
                canGoForward = history.currentIndex in 0 until history.size - 1
            )
        }
    }
}
