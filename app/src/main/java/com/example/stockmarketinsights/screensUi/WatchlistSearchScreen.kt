package com.example.stockmarketinsights.screensUi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stockmarketinsights.componentsUi.SkeletonListItem
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.ui.theme.GreenPrimary
import com.example.stockmarketinsights.viewmodel.SearchViewModel
import com.example.stockmarketinsights.viewmodel.SearchViewModelFactory
import com.example.stockmarketinsights.viewmodel.WatchlistViewModel
import com.example.stockmarketinsights.viewmodel.WatchlistViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistSearchScreen(
    watchlistName: String,
    navController: NavController,
    db: AppDatabase
) {
    val context = LocalContext.current
    val searchViewModel: SearchViewModel = viewModel(
        factory = SearchViewModelFactory(context, db)
    )
    val watchlistViewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModelFactory(db)
    )
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        watchlistViewModel.loadStocksInWatchlist(watchlistName)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = searchViewModel.query,
                        onValueChange = { searchViewModel.onQueryChange(it) },
                        placeholder = { Text("Search to add stocks...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                        trailingIcon = {
                            if (searchViewModel.query.isNotEmpty()) {
                                IconButton(onClick = { searchViewModel.onQueryChange("") }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "Clear")
                                }
                            }
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            when {
                searchViewModel.isLoading -> {
                    LazyColumn { items(6) { SkeletonListItem() } }
                }
                searchViewModel.query.isBlank() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Search for stocks to add to \"$watchlistName\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                searchViewModel.results.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results for \"${searchViewModel.query}\"",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                else -> {
                    LazyColumn {
                        items(searchViewModel.results) { stock ->
                            val isAdded = watchlistViewModel.currentWatchlistStocks
                                .any { it.stockSymbol == stock.symbol }

                            ListItem(
                                headlineContent = {
                                    Text(stock.symbol, fontWeight = FontWeight.SemiBold)
                                },
                                supportingContent = {
                                    Text(stock.name, maxLines = 1)
                                },
                                trailingContent = {
                                    if (isAdded) {
                                        Text(
                                            text = "Added",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GreenPrimary
                                        )
                                    } else {
                                        IconButton(onClick = {
                                            watchlistViewModel.addStockToWatchlist(
                                                watchlistName = watchlistName,
                                                symbol = stock.symbol,
                                                stockName = stock.name
                                            )
                                        }) {
                                            Icon(
                                                Icons.Filled.Add,
                                                contentDescription = "Add",
                                                tint = GreenPrimary
                                            )
                                        }
                                    }
                                }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            )
                        }
                    }
                }
            }
        }
    }
}
