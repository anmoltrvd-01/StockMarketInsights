package com.example.stockmarketinsights.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.stockmarketinsights.dataModel.StockSummaryItem
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.screensUi.*

data class BottomNavItem(val label: String, val icon: ImageVector, val route: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    val bottomNavItems = listOf(
        BottomNavItem("Explore", Icons.Filled.Search, Screen.Explore.route),
        BottomNavItem("Watchlist", Icons.Filled.Star, Screen.Watchlist.route)
    )

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(Screen.Explore.route, Screen.Watchlist.route)

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Explore.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Explore.route) {
                ExploreScreen(navController = navController, db = db)
            }

            composable(Screen.Watchlist.route) {
                WatchlistScreen(navController = navController, db = db)
            }

            composable(
                route = Screen.Details.route
            ) { backStackEntry ->
                val symbol = backStackEntry.arguments?.getString("symbol") ?: ""
                val name = backStackEntry.arguments?.getString("name") ?: ""
                val price = backStackEntry.arguments?.getString("price") ?: "0"
                val change = backStackEntry.arguments?.getString("change") ?: "0"
                val changePercent = backStackEntry.arguments?.getString("changePercent") ?: "0%"
                val volume = backStackEntry.arguments?.getString("volume") ?: "0"

                val stock = StockSummaryItem(
                    symbol = symbol,
                    name = name,
                    price = price,
                    change = change,
                    changePercent = changePercent,
                    volume = volume
                )
                DetailsScreen(stock = stock, navController = navController, db = db)
            }

            composable(
                route = Screen.ViewAll.route
            ) { backStackEntry ->
                val title = backStackEntry.arguments?.getString("title") ?: ""
                val type = backStackEntry.arguments?.getString("type") ?: ""
                ViewAllScreen(title = title, type = type, navController = navController, db = db)
            }

            composable(Screen.SearchAll.route) {
                SearchAllStocksScreen(navController = navController, db = db)
            }

            composable(
                route = Screen.WatchlistDetail.route
            ) { backStackEntry ->
                val watchlistName = backStackEntry.arguments?.getString("watchlistName") ?: ""
                WatchlistDetailScreen(
                    watchlistName = watchlistName,
                    navController = navController,
                    db = db
                )
            }

            composable(
                route = Screen.WatchlistSearch.route
            ) { backStackEntry ->
                val watchlistName = backStackEntry.arguments?.getString("watchlistName") ?: ""
                WatchlistSearchScreen(
                    watchlistName = watchlistName,
                    navController = navController,
                    db = db
                )
            }
        }
    }
}
