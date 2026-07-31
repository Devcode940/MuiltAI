package com.multaihub.app.ui.webview

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Message
import android.view.ViewGroup
import android.webkit.*
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.utils.UserAgent
import com.multaihub.app.viewmodel.WebViewViewModel

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
    var canGoBack by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var showPromptSheet by remember { mutableStateOf(false) }
    var webView by remember { mutableStateOf<WebView?>(null) }

    LaunchedEffect(provider) {
        viewModel.setProvider(provider)
    }

    BackHandler {
        if (canGoBack) {
            webView?.goBack()
        } else {
            onBack()
        }
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
                                viewModel.toggleDesktopMode()
                                showMenu = false
                                // Force reload with new UA
                                webView?.settings?.userAgentString =
                                    UserAgent.get(!activeProvider.isDesktopMode)
                                webView?.reload()
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
                            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            mediaPlaybackRequiresUserGesture = false
                            allowFileAccess = true
                            allowContentAccess = true
                            setSupportMultipleWindows(true)
                            javaScriptCanOpenWindowsAutomatically = true
                            userAgentString = UserAgent.get(activeProvider.isDesktopMode)
                        }

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                                canGoBack = view?.canGoBack() == true
                            }

                            override fun shouldOverrideUrlLoading(
                                view: WebView?,
                                request: WebResourceRequest?
                            ): Boolean = false
                        }

                        webChromeClient = object : WebChromeClient() {
                            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                                isLoading = newProgress < 100
                            }

                            // Route target="_blank" / new-window links back into this WebView
                            // instead of silently dropping them (setSupportMultipleWindows = true).
                            override fun onCreateWindow(
                                view: WebView?,
                                isDialog: Boolean,
                                isUserGesture: Boolean,
                                resultMsg: Message?
                            ): Boolean {
                                val sourceView = view ?: return false
                                val popupWebView = WebView(sourceView.context).apply {
                                    webViewClient = object : WebViewClient() {
                                        override fun shouldOverrideUrlLoading(
                                            v: WebView?,
                                            request: WebResourceRequest?
                                        ): Boolean {
                                            request?.url?.let { sourceView.loadUrl(it.toString()) }
                                            return true
                                        }
                                    }
                                }
                                (resultMsg?.obj as? WebView.WebViewTransport)?.webView = popupWebView
                                resultMsg?.sendToTarget()
                                return true
                            }
                        }

                        loadUrl(activeProvider.url)
                        webView = this
                    }
                },
                update = { view ->
                    // Update UA when mode changes
                    val desiredUa = UserAgent.get(activeProvider.isDesktopMode)
                    if (view.settings.userAgentString != desiredUa) {
                        view.settings.userAgentString = desiredUa
                    }
                },
                onRelease = { it.destroy() },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                )
            }
        }
    }

    // Prompt bottom sheet
    if (showPromptSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPromptSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Prompt Library",
                    style = MaterialTheme.typography.titleLarge
                )
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
                                // In a full implementation we would inject the prompt
                                // into the WebView via JavaScript
                                showPromptSheet = false
                            }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(prompt.title, style = MaterialTheme.typography.titleSmall)
                                Text(
                                    prompt.content.take(80) + if (prompt.content.length > 80) "..." else "",
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
