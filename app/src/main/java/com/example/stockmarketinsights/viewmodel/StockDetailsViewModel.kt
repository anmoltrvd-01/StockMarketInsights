package com.example.stockmarketinsights.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stockmarketinsights.domain.model.CompanyInfo
import com.example.stockmarketinsights.domain.model.IntradayInfo
import com.example.stockmarketinsights.repository.StockRepository
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class StockDetailsState(
    val companyInfo: CompanyInfo? = null,
    val intradayInfos: List<IntradayInfo> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val chartError: String? = null
)

class StockDetailsViewModel(
    context: Context,
    db: AppDatabase,
    private val symbol: String
) : ViewModel() {

    private val repository = StockRepository(context = context, db = db)

    var state by mutableStateOf(StockDetailsState())
        private set

    init {
        loadDetails()
    }

    fun loadDetails() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null, chartError = null)

            val companyInfoDeferred = async { repository.getCompanyInfo(symbol) }
            val intradayDeferred = async { repository.getIntradayInfo(symbol) }

            when (val result = companyInfoDeferred.await()) {
                is Resource.Success -> state = state.copy(companyInfo = result.data)
                is Resource.Error -> state = state.copy(error = result.message)
                is Resource.Loading -> {}
            }

            when (val result = intradayDeferred.await()) {
                is Resource.Success -> state = state.copy(intradayInfos = result.data ?: emptyList())
                is Resource.Error -> state = state.copy(chartError = result.message)
                is Resource.Loading -> {}
            }

            state = state.copy(isLoading = false)
        }
    }
}

class StockDetailsViewModelFactory(
    private val context: Context,
    private val db: AppDatabase,
    private val symbol: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return StockDetailsViewModel(context, db, symbol) as T
    }
}