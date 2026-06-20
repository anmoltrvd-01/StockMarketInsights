package com.example.stockmarketinsights.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.stockmarketinsights.domain.model.CompanyInfo
import com.example.stockmarketinsights.domain.model.IntradayInfo
import com.example.stockmarketinsights.repository.StockRepository
import com.example.stockmarketinsights.utils.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

data class StockDetailsState(
    val companyInfo: CompanyInfo?       = null,
    val intradayInfos: List<IntradayInfo> = emptyList(),
    val isLoading: Boolean              = false,
    val error: String?                  = null
)

class StockDetailsViewModel(
    private val repository: StockRepository,
    private val symbol: String
) : ViewModel() {

    var state by mutableStateOf(StockDetailsState())
        private set

    init {
        loadDetails()
    }

    private fun loadDetails() {
        viewModelScope.launch {
            state = state.copy(isLoading = true, error = null)

            // Fetch company info and intraday data in parallel
            val companyDeferred  = async { repository.getCompanyInfo(symbol) }
            val intradayDeferred = async { repository.getIntradayInfo(symbol) }

            when (val result = companyDeferred.await()) {
                is Resource.Success -> state = state.copy(companyInfo = result.data)
                is Resource.Error   -> state = state.copy(error = result.message)
                else                -> Unit
            }

            when (val result = intradayDeferred.await()) {
                is Resource.Success -> state = state.copy(
                    intradayInfos = result.data ?: emptyList()
                )
                is Resource.Error   -> state = state.copy(error = result.message)
                else                -> Unit
            }

            state = state.copy(isLoading = false)
        }
    }
}

class StockDetailsViewModelFactory(
    private val repository: StockRepository,
    private val symbol: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StockDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StockDetailsViewModel(repository, symbol) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
