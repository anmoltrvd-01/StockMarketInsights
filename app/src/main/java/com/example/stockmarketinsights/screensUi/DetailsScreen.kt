package com.example.stockmarketinsights.screensUi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stockmarketinsights.componentsUi.StockChart
import com.example.stockmarketinsights.dataModel.StockSummaryItem
import com.example.stockmarketinsights.dialogsUi.AddToWatchlistDialog
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.ui.theme.GreenPrimary
import com.example.stockmarketinsights.ui.theme.RedAccent
import com.example.stockmarketinsights.ui.theme.SurfaceDark
import com.example.stockmarketinsights.viewmodel.StockDetailsViewModel
import com.example.stockmarketinsights.viewmodel.StockDetailsViewModelFactory
import com.example.stockmarketinsights.viewmodel.WatchlistViewModel
import com.example.stockmarketinsights.viewmodel.WatchlistViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    stock: StockSummaryItem,
    navController: NavController,
    db: AppDatabase
) {
    val context = LocalContext.current

    val detailsViewModel: StockDetailsViewModel = viewModel(
        key = "details_${stock.symbol}",
        factory = StockDetailsViewModelFactory(context, db, stock.symbol)
    )
    val detailsState = detailsViewModel.state

    val watchlistViewModel: WatchlistViewModel = viewModel(
        factory = WatchlistViewModelFactory(db)
    )

    var showWatchlistDialog by remember { mutableStateOf(false) }

    val isPositive = !stock.change.startsWith("-")
    val changeColor = if (isPositive) GreenPrimary else RedAccent

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stock.symbol, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        watchlistViewModel.loadWatchlists()
                        showWatchlistDialog = true
                    }) {
                        Icon(Icons.Filled.BookmarkAdd, contentDescription = "Add to watchlist")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Stock header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = detailsState.companyInfo?.name?.ifBlank { stock.name } ?: stock.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stock.symbol,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (stock.price.isNotBlank() && stock.price != "N/A") "$${stock.price}" else "N/A",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isPositive) "+${stock.change}" else stock.change,
                                style = MaterialTheme.typography.bodyMedium,
                                color = changeColor,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = stock.changePercent,
                                style = MaterialTheme.typography.bodySmall,
                                color = changeColor
                            )
                        }
                    }
                    if (stock.volume != "N/A" && stock.volume.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Vol: ${stock.volume}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Chart section
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Intraday (1h)",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    when {
                        detailsState.isLoading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = GreenPrimary)
                            }
                        }
                        detailsState.intradayInfos.isNotEmpty() -> {
                            StockChart(
                                infos = detailsState.intradayInfos,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                graphColor = changeColor
                            )
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = detailsState.chartError
                                        ?: "Chart data unavailable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Company info section
            if (detailsState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = GreenPrimary, modifier = Modifier.size(24.dp))
                }
            } else if (detailsState.companyInfo != null) {
                val info = detailsState.companyInfo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        if (!info?.industry.isNullOrBlank()) {
                            InfoRow(label = "Industry", value = info!!.industry)
                        }
                        if (!info?.country.isNullOrBlank()) {
                            InfoRow(label = "Country", value = info!!.country)
                        }
                        if (!info?.description.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = info!!.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else if (detailsState.error != null) {
                Text(
                    text = detailsState.error ?: "",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Watchlist dialog
    if (showWatchlistDialog) {
        AddToWatchlistDialog(
            symbol = stock.symbol,
            stockName = stock.name,
            watchlists = watchlistViewModel.watchlists,
            onDismiss = { showWatchlistDialog = false },
            onAddToWatchlist = { watchlistName ->
                watchlistViewModel.addStockToWatchlist(
                    watchlistName = watchlistName,
                    symbol = stock.symbol,
                    stockName = stock.name,
                    price = stock.price,
                    change = stock.change,
                    changePercent = stock.changePercent
                )
                showWatchlistDialog = false
            },
            onCreateNew = { newName ->
                watchlistViewModel.createWatchlist(newName) {
                    watchlistViewModel.addStockToWatchlist(
                        watchlistName = newName,
                        symbol = stock.symbol,
                        stockName = stock.name,
                        price = stock.price,
                        change = stock.change,
                        changePercent = stock.changePercent
                    )
                }
                showWatchlistDialog = false
            }
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}