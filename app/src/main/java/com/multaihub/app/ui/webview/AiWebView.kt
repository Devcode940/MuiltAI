package com.multaihub.app.ui.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.webview.WebViewDownloadHandler
import com.multaihub.app.webview.WebViewEngine
import com.multaihub.app.webview.WebViewPolicy
import com.multaihub.app.viewmodel.WebViewViewModel

/** Displays an AI provider inside the production WebView engine. */
@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiWebViewScreen(
    provider: AiProvider,
    viewModel: WebViewViewModel,
    onBack: () -> Unit,
    onOpenComparison: () -> Unit = {}
) {
    val currentProvider by viewModel.currentProvider.collectAsState()
    val prompts by viewModel.prompts.collectAsState()
    val activeProvider = currentProvider ?: provider
    var isLoading by remember { mutableStateOf(true) }
    var progress by remember { mutableStateOf(0) }
    var canGoBack by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showPromptSheet by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var rendererCrashed by remember { mutableStateOf(false) }
    val engine = remember { WebViewEngine() }

    LaunchedEffect(provider.id) { viewModel.setProvider(provider) }

    BackHandler { if (canGoBack) webView?.goBack() else onBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(activeProvider.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            if (activeProvider.isDesktopMode) "Desktop" else "Mobile",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { if (canGoBack) webView?.goBack() else onBack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { webView?.reload() }) { Icon(Icons.Default.Refresh, "Refresh") }
                    IconButton(onClick = { showPromptSheet = true }) { Icon(Icons.Default.Lightbulb, "Prompts") }
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "More") }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text(if (activeProvider.isDesktopMode) "Switch to Mobile" else "Switch to Desktop") },
                            onClick = { showMenu = false; viewModel.toggleDesktopMode() },
                            leadingIcon = { Icon(if (activeProvider.isDesktopMode) Icons.Default.PhoneAndroid else Icons.Default.Computer, null) }
                        )
                        DropdownMenuItem(text = { Text("Comparison Mode") }, onClick = { showMenu = false; onOpenComparison() }, leadingIcon = { Icon(Icons.Default.Compare, null) })
                        DropdownMenuItem(text = { Text("Clear Cache") }, onClick = { webView?.clearCache(true); showMenu = false }, leadingIcon = { Icon(Icons.Default.Delete, null) })
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            if (rendererCrashed) {
                Column(Modifier.align(Alignment.Center).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("This AI page crashed. Reload to restart the browser renderer.")
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.material3.Button(onClick = { rendererCrashed = false; webView?.reload() }) { Text("Reload") }
                }
            } else {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            layoutParams = ViewGroup.LayoutParams(-1, -1)
                            WebViewPolicy.apply(this, activeProvider.isDesktopMode)
                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                    isLoading = true
                                    progress = 0
                                    canGoBack = view?.canGoBack() == true
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading = false
                                    progress = 100
                                    canGoBack = view?.canGoBack() == true
                                }

                                override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean =
                                    WebViewPolicy.safeUrl(request.url.toString()) == null

                                override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                                    // WHY: Renderer termination is recoverable; consuming the event prevents a process crash.
                                    rendererCrashed = true
                                    webView = null
                                    view.destroy()
                                    return true
                                }
                            }
                            webChromeClient = object : android.webkit.WebChromeClient() {
                                override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                    progress = newProgress.coerceIn(0, 100)
                                    isLoading = newProgress < 100
                                }
                            }
                            setDownloadListener { url, userAgent, contentDisposition, mimeType, _ ->
                                WebViewDownloadHandler(context).enqueue(url, userAgent, contentDisposition, mimeType)
                            }
                            WebViewPolicy.safeUrl(activeProvider.url)?.let(::loadUrl) ?: run { isLoading = false }
                            webView = this
                        }
                    },
                    update = { view ->
                        val desiredUa = com.multaihub.app.utils.UserAgent.get(activeProvider.isDesktopMode)
                        if (view.settings.userAgentString != desiredUa) {
                            view.settings.userAgentString = desiredUa
                            view.reload()
                        }
                    },
                    onRelease = { engine.destroy(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            if (isLoading && !rendererCrashed) {
                LinearProgressIndicator(progress = { progress / 100f }, modifier = Modifier.fillMaxWidth().align(Alignment.TopCenter))
            }
        }
    }

    if (showPromptSheet) {
        ModalBottomSheet(onDismissRequest = { showPromptSheet = false }) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Prompt Library", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(12.dp))
                if (prompts.isEmpty()) {
                    Text("No saved prompts yet.", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                } else {
                    prompts.forEach { prompt ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp), onClick = { showPromptSheet = false }) {
                            Column(Modifier.padding(12.dp)) {
                                Text(prompt.title, style = MaterialTheme.typography.titleSmall)
                                Text(prompt.content.take(120) + if (prompt.content.length > 120) "..." else "")
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}
