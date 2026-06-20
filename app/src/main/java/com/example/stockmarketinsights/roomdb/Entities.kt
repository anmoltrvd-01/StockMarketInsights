package com.example.stockmarketinsights.roomdb

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watchlists")
data class WatchlistEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val watchlistName: String
)

@Entity(tableName = "watchlist_stocks")
data class WatchlistStockEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val watchlistName: String,
    val stockSymbol: String,
    val stockName: String,
    val price: String = "",
    val change: String = "",
    val changePercent: String = ""
)

@Entity(tableName = "cached_stocks")
data class StockEntity(
    @PrimaryKey val symbol: String,
    val name: String,
    val price: String,
    val change: String,
    val changePercent: String,
    val volume: String,
    val type: String,         // "gainer", "loser", "active"
    val cachedAt: Long = System.currentTimeMillis()
)
