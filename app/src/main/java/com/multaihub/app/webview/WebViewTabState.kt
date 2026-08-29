package com.multaihub.app.webview

import android.webkit.WebBackForwardList
import com.multaihub.app.utils.AppConstants.MAX_TAB_TITLE_LENGTH

/**
 * Immutable snapshot of browser navigation state used by tab UI and persistence.
 *
 * Captures the information needed to restore or display a tab without holding a
 * reference to the live WebView instance.
 *
 * @property title Current page title.
 * @property url Current page URL.
 * @property canGoBack Whether the WebView has back-history available.
 * @property canGoForward Whether the WebView has forward-history available.
 */
data class WebViewTabState(
    val title: String,
    val url: String,
    val canGoBack: Boolean,
    val canGoForward: Boolean
) {
    companion object {
        /**
         * Creates a state snapshot from a WebView history list.
         *
         * @param history The WebView's back/forward list.
         * @param fallbackTitle Title to use when the history item has no title.
         * @param fallbackUrl URL to use when the history item has no URL.
         */
        fun fromHistory(
            history: WebBackForwardList,
            fallbackTitle: String,
            fallbackUrl: String
        ): WebViewTabState {
            val current = history.currentItem
            return WebViewTabState(
                title = current?.title?.take(MAX_TAB_TITLE_LENGTH).orEmpty().ifBlank { fallbackTitle },
                url = current?.url.orEmpty().ifBlank { fallbackUrl },
                canGoBack = history.currentIndex > 0,
                canGoForward = history.currentIndex in 0 until history.size - 1
            )
        }
    }
}
