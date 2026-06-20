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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(context: Context, db: AppDatabase) : ViewModel() {

    private val repository = StockRepository(context = context, db = db)

    var query by mutableStateOf("")
        private set
    var results by mutableStateOf<List<StockSummaryItem>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchJob?.cancel()
        if (newQuery.isBlank()) {
            results = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(400L)
            isLoading = true
            error = null
            when (val result = repository.searchSymbol(newQuery)) {
                is Resource.Success -> {
                    results = result.data?.bestMatches?.map {
                        StockSummaryItem(
                            symbol = it.symbol,
                            name = it.name,
                            price = "N/A",
                            change = "0",
                            changePercent = "0%"
                        )
                    } ?: emptyList()
                }
                is Resource.Error -> {
                    error = result.message
                    results = emptyList()
                }
                is Resource.Loading -> {}
            }
            isLoading = false
        }
    }
}

class SearchViewModelFactory(
    private val context: Context,
    private val db: AppDatabase
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return SearchViewModel(context, db) as T
    }
}
