package com.example.stockmarketinsights.screensUi

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stockmarketinsights.dataModel.StockSummaryItem
import com.example.stockmarketinsights.navigation.Screen
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.viewmodel.WatchlistViewModel
import com.example.stockmarketinsights.viewmodel.WatchlistViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistDetailScreen(
    watchlistName: String,
    navController: NavController,
    db: AppDatabase
) {
    val viewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModelFactory(db)
    )

    LaunchedEffect(watchlistName) {
        viewModel.loadStocksInWatchlist(watchlistName)
    }

    val stocks = viewModel.currentWatchlistStocks

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(watchlistName, fontWeight = FontWeight.Bold)
                        Text(
                            "${stocks.size} stocks",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(Screen.WatchlistSearch.createRoute(watchlistName))
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = "Add stocks")
                    }
                }
            )
        }
    ) { padding ->
        if (stocks.isEmpty() && !viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "No stocks yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        navController.navigate(Screen.WatchlistSearch.createRoute(watchlistName))
                    }) {
                        Icon(Icons.Filled.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Stocks")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(stocks, key = { it.id }) { stockEntity ->
                    val stock = StockSummaryItem(
                        symbol = stockEntity.stockSymbol,
                        name = stockEntity.stockName,
                        price = stockEntity.price,
                        change = stockEntity.change,
                        changePercent = stockEntity.changePercent
                    )
                    var showDeleteConfirm by remember { mutableStateOf(false) }

                    SwipeToDismissBox(
                        state = rememberSwipeToDismissBoxState(
                            confirmValueChange = { dismissValue ->
                                if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                                    showDeleteConfirm = true
                                    false
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
                                    contentDescription = "Remove",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    ) {
                        ListItem(
                            headlineContent = {
                                Text(stock.symbol, fontWeight = FontWeight.SemiBold)
                            },
                            supportingContent = {
                                Text(stock.name, maxLines = 1)
                            },
                            trailingContent = {
                                if (stock.price.isNotBlank() && stock.price != "N/A") {
                                    Text(
                                        text = "$${stock.price}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            },
                            modifier = Modifier.clickable {
                                navController.navigate(
                                    Screen.Details.createRoute(
                                        symbol = stock.symbol,
                                        name = stock.name,
                                        price = stock.price,
                                        change = stock.change,
                                        changePercent = stock.changePercent,
                                        volume = "N/A"
                                    )
                                )
                            }
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    )

                    if (showDeleteConfirm) {
                        AlertDialog(
                            onDismissRequest = { showDeleteConfirm = false },
                            title = { Text("Remove Stock") },
                            text = { Text("Remove ${stock.symbol} from \"$watchlistName\"?") },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.removeStockFromWatchlist(watchlistName, stock.symbol)
                                    showDeleteConfirm = false
                                }) {
                                    Text("Remove", color = MaterialTheme.colorScheme.error)
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
}
