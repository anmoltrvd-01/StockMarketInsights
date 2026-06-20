package com.example.stockmarketinsights.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stockmarketinsights.dataModel.StockSummaryItem
import com.example.stockmarketinsights.repository.StockRepository
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.utils.Resource
import kotlinx.coroutines.launch

data class ExploreState(
    val gainers: List<StockSummaryItem> = emptyList(),
    val losers: List<StockSummaryItem> = emptyList(),
    val mostActive: List<StockSummaryItem> = emptyList(),
    val isLoadingGainers: Boolean = false,
    val isLoadingLosers: Boolean = false,
    val isLoadingActive: Boolean = false,
    val error: String? = null
)

class ExploreViewModel(context: Context, db: AppDatabase) : ViewModel() {

    private val repository = StockRepository(context = context, db = db)

    var state by mutableStateOf(ExploreState())
        private set

    init {
        loadMarketData()
    }

    fun loadMarketData() {
        viewModelScope.launch {
            state = state.copy(
                isLoadingGainers = true,
                isLoadingLosers = true,
                isLoadingActive = true,
                error = null
            )

            // Try cache first
            val cachedGainers = repository.getCachedStocks("gainer")
            val cachedLosers  = repository.getCachedStocks("loser")
            val cachedActive  = repository.getCachedStocks("active")

            if (cachedGainers.isNotEmpty()) state = state.copy(gainers    = cachedGainers, isLoadingGainers = false)
            if (cachedLosers.isNotEmpty())  state = state.copy(losers     = cachedLosers,  isLoadingLosers  = false)
            if (cachedActive.isNotEmpty())  state = state.copy(mostActive = cachedActive,  isLoadingActive  = false)

            // Fetch fresh from API
            when (val result = repository.getTopGainersLosers()) {
                is Resource.Success -> {
                    val data = result.data!!

                    val gainers = data.topGainers?.map {
                        StockSummaryItem(
                            symbol        = it.ticker,
                            name          = it.ticker,
                            price         = it.price,
                            change        = it.changeAmount,
                            changePercent = it.changePercentage,
                            volume        = it.volume
                        )
                    } ?: emptyList()

                    val losers = data.topLosers?.map {
                        StockSummaryItem(
                            symbol        = it.ticker,
                            name          = it.ticker,
                            price         = it.price,
                            change        = it.changeAmount,
                            changePercent = it.changePercentage,
                            volume        = it.volume
                        )
                    } ?: emptyList()

                    val active = data.mostActive?.map {
                        StockSummaryItem(
                            symbol        = it.ticker,
                            name          = it.ticker,
                            price         = it.price,
                            change        = it.changeAmount,
                            changePercent = it.changePercentage,
                            volume        = it.volume
                        )
                    } ?: emptyList()

                    state = state.copy(
                        gainers          = gainers,
                        losers           = losers,
                        mostActive       = active,
                        isLoadingGainers = false,
                        isLoadingLosers  = false,
                        isLoadingActive  = false
                    )

                    repository.cacheStocks(gainers, "gainer")
                    repository.cacheStocks(losers,  "loser")
                    repository.cacheStocks(active,  "active")
                }
                is Resource.Error -> {
                    state = state.copy(
                        isLoadingGainers = false,
                        isLoadingLosers  = false,
                        isLoadingActive  = false,
                        error            = result.message
                    )
                }
                is Resource.Loading -> {}
            }
        }
    }
}

class ExploreViewModelFactory(
    private val context: Context,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ExploreViewModel(context, db) as T
    }
}