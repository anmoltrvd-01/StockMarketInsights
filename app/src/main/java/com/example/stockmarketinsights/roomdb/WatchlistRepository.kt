package com.example.stockmarketinsights.roomdb

class WatchlistRepository(private val dao: WatchlistDao) {

    suspend fun getAllWatchlists(): List<WatchlistEntity> = dao.getAllWatchlists()

    suspend fun createWatchlist(name: String) {
        dao.insertWatchlist(WatchlistEntity(watchlistName = name))
    }

    suspend fun deleteWatchlist(name: String) {
        dao.deleteWatchlistByName(name)
        dao.deleteAllStocksInWatchlist(name)
    }

    suspend fun getStocksInWatchlist(name: String): List<WatchlistStockEntity> =
        dao.getStocksInWatchlist(name)

    suspend fun getStockCount(name: String): Int =
        dao.getStockCountInWatchlist(name)

    suspend fun addStock(
        watchlistName: String,
        symbol: String,
        stockName: String,
        price: String = "",
        change: String = "",
        changePercent: String = ""
    ) {
        dao.addStockToWatchlist(
            WatchlistStockEntity(
                watchlistName = watchlistName,
                stockSymbol = symbol,
                stockName = stockName,
                price = price,
                change = change,
                changePercent = changePercent
            )
        )
    }

    suspend fun removeStock(watchlistName: String, symbol: String) {
        dao.removeStockFromWatchlist(watchlistName, symbol)
    }

    suspend fun isStockInWatchlist(watchlistName: String, symbol: String): Boolean =
        dao.isStockInWatchlist(watchlistName, symbol)
}