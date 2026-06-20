package com.example.stockmarketinsights.screensUi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stockmarketinsights.componentsUi.WatchlistCard
import com.example.stockmarketinsights.navigation.Screen
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.viewmodel.WatchlistViewModel
import com.example.stockmarketinsights.viewmodel.WatchlistViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(navController: NavController, db: AppDatabase) {
    val viewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModelFactory(db)
    )

    var showCreateDialog by remember { mutableStateOf(false) }
    var newWatchlistName by remember { mutableStateOf("") }

    // Refresh counts every time this screen becomes visible again
    // (e.g. coming back from WatchlistDetailScreen after adding/removing stocks)
    LaunchedEffect(Unit) { viewModel.loadWatchlists() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Watchlists", fontWeight = FontWeight.Bold)
                },
                actions = {
                    IconButton(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = "Create watchlist")
                    }
                }
            )
        }
    ) { padding ->
        if (viewModel.watchlists.isEmpty() && !viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No watchlists yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { showCreateDialog = true }) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Watchlist")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(viewModel.watchlists, key = { it.id }) { watchlist ->
                    var showDeleteConfirm by remember { mutableStateOf(false) }
                    val stockCount = viewModel.watchlistCounts[watchlist.watchlistName] ?: 0

                    SwipeToDismissBox(
                        state = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    showDeleteConfirm = true
                                    false // don't dismiss yet
                                } else false
                            }
                        ),
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(end = 16.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    ) {
                        WatchlistCard(
                            name = watchlist.watchlistName,
                            stockCount = stockCount,
                            onClick = {
                                navController.navigate(
                                    Screen.WatchlistDetail.createRoute(watchlist.watchlistName)
                                )
                            }
                        )
                    }

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Delete Watchlist") },
                            text = { Text("Delete \"${watchlist.watchlistName}\"? This will also remove all stocks in it.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.deleteWatchlist(watchlist.watchlistName)
                                    showDeleteConfirm = false
                                }) {
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDeleteConfirm = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false; newWatchlistName = "" },
            title = { Text("New Watchlist") },
            text = {
                OutlinedTextField(
                    value = newWatchlistName,
                    onValueChange = { newWatchlistName = it },
                    placeholder = { Text("Watchlist name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newWatchlistName.isNotBlank()) {
                            viewModel.createWatchlist(newWatchlistName.trim())
                            showCreateDialog = false
                            newWatchlistName = ""
                        }
                    }
                ) { Text("Create") }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false; newWatchlistName = "" }) {
                    Text("Cancel")
                }
            }
        )
    }
}