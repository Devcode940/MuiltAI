package com.multaihub.app.ui.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
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
import com.multaihub.app.utils.UrlValidator
import com.multaihub.app.utils.UserAgent
import com.multaihub.app.viewmodel.WebViewViewModel

/**
 * Displays an AI provider inside a hardened WebView.
 *
 * // WHY: WebView is the application's highest-risk component because provider content is
 * // remote and JavaScript-enabled. Navigation, storage, file access, and popup behavior are
 * // therefore deliberately constrained here instead of relying on individual AI providers.
 */
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

    LaunchedEffect(provider.id) {
        viewModel.setProvider(provider)
    }

    BackHandler {
        if (canGoBack) webView?.goBack() else onBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(activeProvider.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = if (activeProvider.isDesktopMode) "Desktop" else "Mobile",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (canGoBack) webView?.goBack() else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { webView?.reload() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                    IconButton(onClick = { showPromptSheet = true }) {
                        Icon(Icons.Default.Lightbulb, contentDescription = "Prompts")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (activeProvider.isDesktopMode) "Switch to Mobile"
                                    else "Switch to Desktop"
                                )
                            },
                            onClick = {
                                showMenu = false
                                viewModel.toggleDesktopMode()
                            },
                            leadingIcon = {
                                Icon(
                                    if (activeProvider.isDesktopMode) Icons.Default.PhoneAndroid
                                    else Icons.Default.Computer,
                                    contentDescription = null
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Comparison Mode") },
                            onClick = {
                                showMenu = false
                                onOpenComparison()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Compare, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear Cache") },
                            onClick = {
                                webView?.clearCache(true)
                                showMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null)
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )

                        settings.apply {
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
                            userAgentString = UserAgent.get(activeProvider.isDesktopMode)
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: Bitmap?
                            ) {
                                isLoading = true
                                progress = 0
                                canGoBack = view?.canGoBack() == true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                progress = 100
                                canGoBack = view?.canGoBack() == true
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView,
                                request: WebResourceRequest
                            ): Boolean {
                                val url = request.url.toString()

                                // WHY: Only HTTP(S) navigation stays inside the WebView. This
                                // blocks javascript:, file:, content:, data:, intent:, and
                                // arbitrary custom schemes from reaching the renderer.
                                return !UrlValidator.isSafeWebNavigation(url)
                            }
                        }

                        webChromeClient = object : android.webkit.WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                progress = newProgress.coerceIn(0, 100)
                                isLoading = newProgress < 100
                            }
                        }

                        // WHY: Validate even built-in provider URLs at the final load boundary;
                        // model/database validation alone must never be the security boundary.
                        val safeUrl = UrlValidator.validateAndEnforceHttps(
                            activeProvider.url,
                            enforceHttps = true
                        )
                        if (safeUrl != null) {
                            loadUrl(safeUrl)
                        } else {
                            isLoading = false
                        }
                        webView = this
                    }
                },
                update = { view ->
                    val desiredUa = UserAgent.get(activeProvider.isDesktopMode)
                    if (view.settings.userAgentString != desiredUa) {
                        // WHY: UA changes only take effect reliably after a reload, so perform
                        // the reload from the single Compose update path rather than the click handler.
                        view.settings.userAgentString = desiredUa
                        view.reload()
                    }

                    val desiredUrl = UrlValidator.validateAndEnforceHttps(
                        activeProvider.url,
                        enforceHttps = true
                    )
                    if (desiredUrl != null && view.url != desiredUrl && !isLoading) {
                        view.loadUrl(desiredUrl)
                    }
                },
                onRelease = {
                    // WHY: WebView owns native resources outside Compose's normal memory model;
                    // explicit destruction prevents renderer/context leaks when the screen leaves.
                    it.stopLoading()
                    it.webChromeClient = null
                    it.webViewClient = null
                    it.destroy()
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }

    if (showPromptSheet) {
        ModalBottomSheet(onDismissRequest = { showPromptSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Prompt Library", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))

                if (prompts.isEmpty()) {
                    Text(
                        "No saved prompts yet.\nYou can add prompts from Settings later.",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                } else {
                    prompts.forEach { prompt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            onClick = {
                                // WHY: Prompt injection is provider-specific. Never inject raw
                                // JavaScript into arbitrary pages without a provider adapter.
                                showPromptSheet = false
                            }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(prompt.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    prompt.content.take(80) +
                                        if (prompt.content.length > 80) "..." else "",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
