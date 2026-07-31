package com.multaihub.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
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
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as MultiAIApp

        setContent {
            MultiAIHubTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()

                    val homeViewModel: HomeViewModel = viewModel(
                        factory = HomeViewModel.Factory(app.repository)
                    )
                    val webViewModel: WebViewViewModel = viewModel(
                        factory = WebViewViewModel.Factory(app.repository)
                    )

                    // Temporary holder for selected provider (simple approach)
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
                                    navController.navigate("webview/${provider.id}")
                                },
                                onOpenComparison = {
                                    // Placeholder for comparison screen
                                    // navController.navigate("comparison")
                                }
                            )
                        }

                        composable(
                            route = "webview/{aiId}",
                            arguments = listOf(navArgument("aiId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val aiId = backStackEntry.arguments?.getString("aiId") ?: return@composable
                            val provider = selectedProvider

                            if (provider != null && provider.id == aiId) {
                                AiWebViewScreen(
                                    provider = provider,
                                    viewModel = webViewModel,
                                    onBack = { navController.popBackStack() },
                                    onOpenComparison = {
                                        // Future: open comparison
                                    }
                                )
                            } else {
                                // Fallback: load from DB
                                LaunchedEffect(aiId) {
                                    scope.launch {
                                        val p = app.repository.getProviderById(aiId)
                                        if (p != null) {
                                            selectedProvider = p
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
}
