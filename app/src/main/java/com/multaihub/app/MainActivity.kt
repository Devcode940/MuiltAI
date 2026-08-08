package com.multaihub.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.ui.home.HomeScreen
import com.multaihub.app.ui.theme.MultiAIHubTheme
import com.multaihub.app.ui.webview.AiWebViewScreen
import com.multaihub.app.viewmodel.HomeViewModel
import com.multaihub.app.viewmodel.WebViewViewModel

/** Main Android entry point and navigation host. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MultiAIApp

        setContent {
            MultiAIHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(app.repository)
                    )
                    val webViewModel: WebViewViewModel = viewModel(
                        factory = WebViewViewModel.Factory(app.repository)
                    )

                    var selectedProvider by remember { mutableStateOf<AiProvider?>(null) }

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {
                        composable("home") {
                            HomeScreen(
                                viewModel = homeViewModel,
                                onAiClick = { provider ->
                                    selectedProvider = provider
                                    // WHY: URL-encode the identifier so custom provider IDs cannot
                                    // corrupt the navigation route with '/', '?', or '#'.
                                    navController.navigate("webview/${Uri.encode(provider.id)}")
                                },
                                onOpenComparison = {
                                    // Comparison screen remains intentionally separate from the WebView lifecycle.
                                }
                            )
                        }

                        composable(
                            route = "webview/{aiId}",
                            arguments = listOf(navArgument("aiId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val aiId = backStackEntry.arguments?.getString("aiId")
                                ?: return@composable
                            val provider = selectedProvider

                            if (provider != null && provider.id == aiId) {
                                AiWebViewScreen(
                                    provider = provider,
                                    viewModel = webViewModel,
                                    onBack = { navController.popBackStack() },
                                    onOpenComparison = {}
                                )
                            } else {
                                // WHY: Route arguments are the recovery path after process death;
                                // the screen does not depend on an in-memory selectedProvider.
                                LaunchedEffect(aiId) {
                                    val persistedProvider = app.repository.getProviderById(aiId)
                                    if (persistedProvider != null) {
                                        selectedProvider = persistedProvider
                                    } else {
                                        navController.popBackStack()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
