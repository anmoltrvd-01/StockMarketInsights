package com.example.stockmarketinsights.navigation

import java.net.URLDecoder
import java.net.URLEncoder

private fun enc(value: String): String =
    URLEncoder.encode(value.ifBlank { "-" }, "UTF-8")

fun dec(value: String?): String =
    if (value.isNullOrBlank()) "" else URLDecoder.decode(value, "UTF-8")

sealed class Screen(val route: String) {
    object Explore : Screen("explore")
    object Watchlist : Screen("watchlist")

    object Details : Screen(
        "details?symbol={symbol}&name={name}&price={price}&change={change}&changePercent={changePercent}&volume={volume}"
    ) {
        fun createRoute(
            symbol: String,
            name: String,
            price: String,
            change: String,
            changePercent: String,
            volume: String
        ) = "details?symbol=${enc(symbol)}&name=${enc(name)}&price=${enc(price)}" +
                "&change=${enc(change)}&changePercent=${enc(changePercent)}&volume=${enc(volume)}"
    }

    object ViewAll : Screen("view_all?title={title}&type={type}") {
        fun createRoute(title: String, type: String) =
            "view_all?title=${enc(title)}&type=${enc(type)}"
    }

    object SearchAll : Screen("search_all")

    object WatchlistDetail : Screen("watchlist_detail?watchlistName={watchlistName}") {
        fun createRoute(watchlistName: String) =
            "watchlist_detail?watchlistName=${enc(watchlistName)}"
    }

    object WatchlistSearch : Screen("watchlist_search?watchlistName={watchlistName}") {
        fun createRoute(watchlistName: String) =
            "watchlist_search?watchlistName=${enc(watchlistName)}"
    }
}