package com.example.stockmarketinsights.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stockmarketinsights.screensUi.*

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Explore.route
    ) {
        composable(route = Screen.Explore.route) {
            ExploreScreen()
        }
        composable(route = Screen.Details.route) {
            DetailsScreen()
        }
        composable(route = Screen.ViewAll.route) {
            ViewAllScreen()
        }
        composable(route = Screen.Watchlist.route) {
            WatchlistScreen()
        }
        composable(route = Screen.WatchlistDetail.route) {
            WatchlistDetailScreen()
        }
        composable(route = Screen.WatchlistSearch.route) {
            WatchlistSearchScreen()
        }
        composable(route = Screen.SearchAllStocks.route) {
            SearchAllStocksScreen()
        }
    }
}
