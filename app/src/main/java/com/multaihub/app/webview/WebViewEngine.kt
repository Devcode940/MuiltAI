package com.multaihub.app.webview

import android.os.Bundle
import android.webkit.WebView

/** Owns reusable WebView lifecycle operations used by browser screens and tabs. */
class WebViewEngine {
    /** Saves navigation state into a caller-owned bundle. */
    fun saveState(webView: WebView, outState: Bundle): Boolean = webView.saveState(outState) != null

    /** Restores navigation state when a compatible saved state exists. */
    fun restoreState(webView: WebView, state: Bundle?): Boolean = state != null && webView.restoreState(state) != null

    /** Releases renderer, clients, callbacks, and native resources deterministically. */
    fun destroy(webView: WebView) {
        webView.stopLoading()
        webView.loadUrl("about:blank")
        webView.clearHistory()
        webView.removeAllViews()
        webView.webChromeClient = null
        webView.webViewClient = null
        webView.destroy()
    }
}
