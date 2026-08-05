package com.multaihub.app.ui.webview

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewClientCompat
import com.multaihub.app.utils.WebViewJavaScript

/**
 * Composable screen that displays an AI WebView for chat interaction.
 *
 * @param aiProviderUrl The URL of the AI provider to load
 */
@Composable
fun AiWebViewScreen(
    aiProviderUrl: String
) {
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0) }
    
    Column(modifier = Modifier.fillMaxSize()) {
        if (isLoading) {
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.fillMaxSize()
            )
        }
        
        AndroidView(
            factory = { ctx ->
                AiWebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    
                    webViewClient = object : WebViewClientCompat() {
                        override fun onPageStarted(view: android.webkit.WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                            super.onPageStarted(view, url, favicon)
                            progress = 0
                            isLoading = true
                        }
                        
                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                            super.onPageFinished(view, url)
                            progress = 100
                            isLoading = false
                            view?.evaluateJavascript(WebViewJavaScript.CHAT_INIT_SCRIPT) { _ -> }
                        }
                    }
                    
                    evaluateJavascript(WebViewJavaScript.CHAT_INIT_SCRIPT) { _ -> }
                    loadUrl(aiProviderUrl)
                }
            },
            update = { webView ->
                if (webView.url != aiProviderUrl) {
                    webView.loadUrl(aiProviderUrl)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
