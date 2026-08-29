package com.multaihub.app.webview

import android.os.Bundle
import android.webkit.WebView

/**
 * Owns reusable WebView lifecycle operations used by browser screens and tabs.
 *
 * Centralizes the non-trivial work of saving, restoring, and destroying WebView
 * instances so every screen does it the same way and resource leaks are avoided.
 */
class WebViewEngine {

    /**
     * Saves WebView navigation state (back/forward history, scroll position) into a Bundle.
     *
     * @param webView The WebView to save state from.
     * @param outState The Bundle to write state into.
     * @return `true` if state was saved successfully.
     */
    fun saveState(webView: WebView, outState: Bundle): Boolean =
        webView.saveState(outState) != null

    /**
     * Restores WebView navigation state from a Bundle when compatible state exists.
     *
     * @param webView The WebView to restore state into.
     * @param state The Bundle containing previously saved state; may be `null`.
     * @return `true` if state was restored successfully.
     */
    fun restoreState(webView: WebView, state: Bundle?): Boolean =
        state != null && webView.restoreState(state) != null

    /**
     * Releases the renderer, clients, callbacks, and native resources deterministically.
     *
     * Must be called from the host Activity/Fragment's `onDestroy` to prevent WebView
     * resource leaks across configuration changes.
     *
     * @param webView The WebView to destroy.
     */
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
