package com.example.stockmarketinsights.navigation

sealed class Screen(val route: String) {
    object Explore : Screen("explore")
    object Details : Screen("details")
    object ViewAll : Screen("view_all")
    object Watchlist : Screen("watchlist")
    object WatchlistDetail : Screen("watchlist_detail")
    object WatchlistSearch : Screen("watchlist_search")
    object SearchAllStocks : Screen("search_all_stocks")
}
