package com.example.stockmarketinsights.screensUi

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stockmarketinsights.componentsUi.SkeletonStockCard
import com.example.stockmarketinsights.componentsUi.StatusMessage
import com.example.stockmarketinsights.componentsUi.StateType
import com.example.stockmarketinsights.componentsUi.StockCard
import com.example.stockmarketinsights.componentsUi.friendlyErrorFor
import com.example.stockmarketinsights.dataModel.StockSummaryItem
import com.example.stockmarketinsights.navigation.Screen
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.viewmodel.ExploreViewModel
import com.example.stockmarketinsights.viewmodel.ExploreViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(navController: NavController, db: AppDatabase) {
    val context = LocalContext.current
    val viewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModelFactory(context, db)
    )
    val state = viewModel.state

    val navigateToDetails = { stock: StockSummaryItem ->
        navController.navigate(
            Screen.Details.createRoute(
                symbol = stock.symbol,
                name = stock.name,
                price = stock.price,
                change = stock.change,
                changePercent = stock.changePercent,
                volume = stock.volume
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Market Insights",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.SearchAll.route) }) {
                        Icon(Icons.Filled.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { padding ->
        // Show a calm full-screen state only when we have NO cached/local data at all
        val hasAnyData = state.gainers.isNotEmpty() || state.losers.isNotEmpty() || state.mostActive.isNotEmpty()

        if (state.error != null && !hasAnyData &&
            !state.isLoadingGainers && !state.isLoadingLosers && !state.isLoadingActive
        ) {
            val (title, message) = friendlyErrorFor(state.error)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                StatusMessage(
                    type = StateType.NETWORK,
                    title = title,
                    message = message,
                    onRetry = { viewModel.loadMarketData() }
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // Small inline banner only when we DO have cached data but the
            // latest refresh failed — keeps existing content visible.
            if (state.error != null && hasAnyData) {
                item {
                    val (title, _) = friendlyErrorFor(state.error)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "$title · showing cached data",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.loadMarketData() }) {
                                Text("Retry", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Top Gainers
            item {
                SectionHeader(
                    title = "Top Gainers",
                    onSeeAll = {
                        navController.navigate(Screen.ViewAll.createRoute("Top Gainers", "gainer"))
                    }
                )
            }
            item {
                StockHorizontalList(
                    stocks = state.gainers,
                    isLoading = state.isLoadingGainers,
                    onClick = navigateToDetails
                )
            }

            // Top Losers
            item {
                SectionHeader(
                    title = "Top Losers",
                    onSeeAll = {
                        navController.navigate(Screen.ViewAll.createRoute("Top Losers", "loser"))
                    }
                )
            }
            item {
                StockHorizontalList(
                    stocks = state.losers,
                    isLoading = state.isLoadingLosers,
                    onClick = navigateToDetails
                )
            }

            // Most Active
            item {
                SectionHeader(
                    title = "Most Active",
                    onSeeAll = {
                        navController.navigate(Screen.ViewAll.createRoute("Most Active", "active"))
                    }
                )
            }
            item {
                StockHorizontalList(
                    stocks = state.mostActive,
                    isLoading = state.isLoadingActive,
                    onClick = navigateToDetails
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onSeeAll: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        TextButton(onClick = onSeeAll) {
            Text("See all", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun StockHorizontalList(
    stocks: List<StockSummaryItem>,
    isLoading: Boolean,
    onClick: (StockSummaryItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (isLoading && stocks.isEmpty()) {
            repeat(4) { SkeletonStockCard() }
        } else {
            stocks.take(10).forEach { stock ->
                StockCard(stock = stock, onClick = { onClick(stock) })
            }
        }
    }
    Spacer(modifier = Modifier.height(4.dp))
}