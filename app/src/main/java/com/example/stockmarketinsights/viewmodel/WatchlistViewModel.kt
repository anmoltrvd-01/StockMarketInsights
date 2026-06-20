package com.example.stockmarketinsights.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.roomdb.WatchlistEntity
import com.example.stockmarketinsights.roomdb.WatchlistRepository
import com.example.stockmarketinsights.roomdb.WatchlistStockEntity
import kotlinx.coroutines.launch

class WatchlistViewModel(db: AppDatabase) : ViewModel() {

    private val repository = WatchlistRepository(db.watchlistDao())

    var watchlists by mutableStateOf<List<WatchlistEntity>>(emptyList())
        private set

    /** watchlistName -> stock count, kept in sync whenever watchlists or their contents change */
    var watchlistCounts by mutableStateOf<Map<String, Int>>(emptyMap())
        private set

    var currentWatchlistStocks by mutableStateOf<List<WatchlistStockEntity>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    init {
        loadWatchlists()
    }

    fun loadWatchlists() {
        viewModelScope.launch {
            isLoading = true
            val lists = repository.getAllWatchlists()
            watchlists = lists

            // Load counts for every watchlist so the UI shows real numbers
            val counts = mutableMapOf<String, Int>()
            lists.forEach { wl ->
                counts[wl.watchlistName] = repository.getStockCount(wl.watchlistName)
            }
            watchlistCounts = counts

            isLoading = false
        }
    }

    fun createWatchlist(name: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            repository.createWatchlist(name)
            loadWatchlists()
            onDone()
        }
    }

    fun deleteWatchlist(name: String) {
        viewModelScope.launch {
            repository.deleteWatchlist(name)
            loadWatchlists()
        }
    }

    fun loadStocksInWatchlist(watchlistName: String) {
        viewModelScope.launch {
            isLoading = true
            currentWatchlistStocks = repository.getStocksInWatchlist(watchlistName)
            isLoading = false
        }
    }

    fun addStockToWatchlist(
        watchlistName: String,
        symbol: String,
        stockName: String,
        price: String = "",
        change: String = "",
        changePercent: String = ""
    ) {
        viewModelScope.launch {
            repository.addStock(watchlistName, symbol, stockName, price, change, changePercent)
            loadStocksInWatchlist(watchlistName)
            // Keep the count badge on the Watchlists screen in sync too
            val updatedCount = repository.getStockCount(watchlistName)
            watchlistCounts = watchlistCounts.toMutableMap().apply {
                put(watchlistName, updatedCount)
            }
        }
    }

    fun removeStockFromWatchlist(watchlistName: String, symbol: String) {
        viewModelScope.launch {
            repository.removeStock(watchlistName, symbol)
            loadStocksInWatchlist(watchlistName)
            val updatedCount = repository.getStockCount(watchlistName)
            watchlistCounts = watchlistCounts.toMutableMap().apply {
                put(watchlistName, updatedCount)
            }
        }
    }

    suspend fun isStockInWatchlist(watchlistName: String, symbol: String): Boolean {
        return repository.isStockInWatchlist(watchlistName, symbol)
    }
}

class WatchlistViewModelFactory(private val db: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WatchlistViewModel(db) as T
    }
}