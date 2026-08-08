package com.multaihub.app.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.multaihub.app.data.model.AiCategory
import com.multaihub.app.data.model.AiProvider
import com.multaihub.app.ui.components.AiCard
import com.multaihub.app.ui.components.CategoryChip
import com.multaihub.app.viewmodel.HomeViewModel

/** Production provider catalog with database-backed search, categories, favorites and recent items. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAiClick: (AiProvider) -> Unit,
    onOpenComparison: () -> Unit = {},
    onOpenNotes: () -> Unit = {},
    onOpenSettings: () -> Unit = {}
) {
    val providers by viewModel.providers.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val recent by viewModel.recentProviders.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newUrl by remember { mutableStateOf("") }
    var showMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MultiAI Hub", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onOpenComparison) { Icon(Icons.Default.Compare, "Compare") }
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "More") }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Notes") }, onClick = { showMenu = false; onOpenNotes() }, leadingIcon = { Icon(Icons.Default.Note, null) })
                        DropdownMenuItem(text = { Text("Settings") }, onClick = { showMenu = false; onOpenSettings() }, leadingIcon = { Icon(Icons.Default.Settings, null) })
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) { Icon(Icons.Default.Add, "Add AI") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            error?.let { message ->
                Surface(color = MaterialTheme.colorScheme.errorContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(message, color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = viewModel::updateSearch,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Search AIs...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                shape = RoundedCornerShape(16.dp)
            )

            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(AiCategory.entries) { category ->
                    CategoryChip(
                        text = category.displayName,
                        selected = selectedCategory == category.displayName,
                        onClick = { viewModel.selectCategory(category.displayName) }
                    )
                }
            }

            if (recent.isNotEmpty() && selectedCategory == "All" && searchQuery.isBlank()) {
                Text("Recent", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(recent, key = { it.id }) { provider ->
                        SuggestionChip(onClick = { viewModel.markAsUsed(provider.id); onAiClick(provider) }, label = { Text(provider.name) })
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            if (providers.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        if (isLoading) "Loading AI providers..." else if (searchQuery.isNotBlank()) "No matching AIs" else "No AIs available",
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.55f)
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 140.dp),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(providers, key = { it.id }) { provider ->
                        AiCard(
                            provider = provider,
                            onClick = { viewModel.markAsUsed(provider.id); onAiClick(provider) },
                            onFavoriteClick = { viewModel.toggleFavorite(provider) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false; viewModel.clearError() },
            title = { Text("Add Custom AI") },
            text = {
                Column {
                    OutlinedTextField(value = newName, onValueChange = { newName = it.take(80); viewModel.clearError() }, label = { Text("Name") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = newUrl, onValueChange = { newUrl = it.take(2048); viewModel.clearError() }, label = { Text("HTTPS URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                    error?.let { Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp)) }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.addCustomAi(newName, newUrl)
                    newName = ""
                    newUrl = ""
                    showAddDialog = false
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAddDialog = false; viewModel.clearError() }) { Text("Cancel") } }
        )
    }
}
