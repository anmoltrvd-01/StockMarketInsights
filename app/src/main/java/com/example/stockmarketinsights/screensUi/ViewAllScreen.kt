package com.example.stockmarketinsights.screensUi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.stockmarketinsights.componentsUi.SkeletonListItem
import com.example.stockmarketinsights.componentsUi.StockListItem
import com.example.stockmarketinsights.navigation.Screen
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.viewmodel.ExploreViewModel
import com.example.stockmarketinsights.viewmodel.ExploreViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewAllScreen(
    title: String,
    type: String,
    navController: NavController,
    db: AppDatabase
) {
    val context = LocalContext.current
    val viewModel: ExploreViewModel = viewModel(
        factory = ExploreViewModelFactory(context, db)
    )
    val state = viewModel.state

    val stocks = when (type) {
        "gainer" -> state.gainers
        "loser" -> state.losers
        "active" -> state.mostActive
        else -> emptyList()
    }
    val isLoading = when (type) {
        "gainer" -> state.isLoadingGainers
        "loser" -> state.isLoadingLosers
        "active" -> state.isLoadingActive
        else -> false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading && stocks.isEmpty()) {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(10) { SkeletonListItem() }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(stocks) { stock ->
                    StockListItem(
                        stock = stock,
                        onClick = {
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
