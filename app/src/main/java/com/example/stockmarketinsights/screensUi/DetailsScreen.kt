package com.example.stockmarketinsights.screensUi

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stockmarketinsights.componentsUi.StockChart
import com.example.stockmarketinsights.dataModel.StockSummaryItem
import com.example.stockmarketinsights.dialogsUi.AddToWatchlistDialog
import com.example.stockmarketinsights.repository.StockRepository
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.roomdb.WatchlistRepository
import com.example.stockmarketinsights.viewmodel.StockDetailsViewModelFactory
import com.example.stockmarketinsights.viewmodel.StockDetailsViewModel
import com.example.stockmarketinsights.viewmodel.WatchlistViewModel
import com.example.stockmarketinsights.viewmodel.WatchlistViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailsScreen(
    stock: StockSummaryItem,
    navController: NavController
) {
    val context = LocalContext.current
    val db      = remember { AppDatabase.getDatabase(context) }

    // ── Watchlist ViewModel (unchanged) ─────────────────────────────────────
    val watchlistRepository = remember { WatchlistRepository(db.watchlistDao()) }
    val watchlistViewModel: WatchlistViewModel =
        viewModel(factory = WatchlistViewModelFactory(watchlistRepository))
    val watchlistNames by watchlistViewModel.watchlistItems.collectAsState()

    // ── Details ViewModel (new - real company info + chart) ─────────────────
    val stockRepository = remember { StockRepository(context = context, db = db) }
    val detailsViewModel: StockDetailsViewModel = viewModel(
        factory = StockDetailsViewModelFactory(stockRepository, stock.symbol),
        key     = stock.symbol    // unique key per stock so VM reloads on navigate
    )
    val detailsState = detailsViewModel.state

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AddToWatchlistDialog(
            showDialog         = true,
            existingWatchlists = watchlistNames.map { it.watchlistName }.distinct(),
            onDismiss          = { showDialog = false },
            onAdd              = { watchlistName ->
                watchlistViewModel.addStockToWatchlist(watchlistName, stock)
                showDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stock Detail") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showDialog = true }) {
                        Icon(Icons.Default.Bookmark, contentDescription = "Bookmark")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // ── Stock header (your original UI) ───────────────────────────
            Row(
                verticalAlignment    = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier             = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(
                            text     = stock.symbol.take(2),
                            modifier = Modifier.align(Alignment.Center),
                            style    = MaterialTheme.typography.labelLarge
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(stock.name, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${stock.symbol} · Common Stock",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val isPositive = !stock.changePercent.contains("-")
                    val color      = if (isPositive) Color(0xFF27AE60) else Color(0xFFC0392B)
                    Text(text = stock.price,         color = color)
                    Text(text = stock.changePercent, color = color)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Chart section ─────────────────────────────────────────────
            Text("Today's Chart", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                when {
                    detailsState.isLoading -> {
                        CircularProgressIndicator()
                    }
                    detailsState.intradayInfos.isNotEmpty() -> {
                        StockChart(
                            infos    = detailsState.intradayInfos,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(8.dp),
                            graphColor = if (!stock.changePercent.contains("-"))
                                Color(0xFF27AE60) else Color(0xFFC0392B)
                        )
                    }
                    else -> {
                        Text(
                            "Chart unavailable",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Company info section ──────────────────────────────────────
            detailsState.companyInfo?.let { info ->
                if (info.industry.isNotBlank()) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoChip(label = "Industry", value = info.industry)
                        Spacer(modifier = Modifier.width(8.dp))
                        if (info.country.isNotBlank()) {
                            InfoChip(label = "Country", value = info.country)
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                Text(
                    text  = "About ${info.name.ifBlank { stock.name }}",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text  = info.description.ifBlank { "No description available." },
                    style = MaterialTheme.typography.bodySmall
                )
            } ?: run {
                if (!detailsState.isLoading) {
                    Text(
                        text  = "About ${stock.name}",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text  = "Company details unavailable.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Error state (non-blocking - shown below content)
            detailsState.error?.let { err ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text  = err,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text  = "$label: $value",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
