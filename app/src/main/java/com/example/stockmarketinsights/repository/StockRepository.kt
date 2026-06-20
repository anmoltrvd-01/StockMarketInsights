package com.example.stockmarketinsights.repository

import android.content.Context
import com.example.stockmarketinsights.BuildConfig
import com.example.stockmarketinsights.data.csv.CompanyListingsParser
import com.example.stockmarketinsights.data.csv.CSVParser
import com.example.stockmarketinsights.data.csv.IntradayInfoParser
import com.example.stockmarketinsights.data.mapper.toCompanyInfo
import com.example.stockmarketinsights.data.mapper.toCompanyListing
import com.example.stockmarketinsights.data.mapper.toCompanyListingEntity
import com.example.stockmarketinsights.dataModel.StockSummaryItem
import com.example.stockmarketinsights.dataModel.SymbolSearchResponse
import com.example.stockmarketinsights.dataModel.TopGainersLosersResponse
import com.example.stockmarketinsights.domain.model.CompanyInfo
import com.example.stockmarketinsights.domain.model.CompanyListing
import com.example.stockmarketinsights.domain.model.IntradayInfo
import com.example.stockmarketinsights.network.RetrofitInstance
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.roomdb.StockEntity
import com.example.stockmarketinsights.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StockRepository(
    private val context: Context,
    private val db: AppDatabase,
    private val companyListingsParser: CSVParser<CompanyListing> = CompanyListingsParser(),
    private val intradayInfoParser: CSVParser<IntradayInfo> = IntradayInfoParser()
) {
    private val api      = RetrofitInstance.api
    private val apiKey   = BuildConfig.ALPHA_VANTAGE_API_KEY
    private val stockDao = db.stockDao()


    suspend fun getTopGainersLosers(): Resource<TopGainersLosersResponse> {
        return try {
            Resource.Success(api.getTopGainersLosers(apiKey))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Unknown error fetching top gainers/losers")
        }
    }

    suspend fun getCachedStocks(type: String): List<StockSummaryItem> {
        return stockDao.getStocksByType(type).map {
            StockSummaryItem(
                symbol        = it.symbol,
                name          = it.name,
                price         = it.price,
                change        = it.change,
                changePercent = it.changePercent,
                volume        = it.volume
            )
        }
    }

    suspend fun cacheStocks(stocks: List<StockSummaryItem>, type: String) {
        val entities = stocks.map {
            StockEntity(
                symbol        = it.symbol,
                name          = it.name,
                price         = it.price,
                change        = it.change,
                changePercent = it.changePercent,
                volume        = it.volume,
                type          = type
            )
        }
        stockDao.clearStocksByType(type)
        stockDao.insertStocks(entities)
    }


    suspend fun searchSymbol(keywords: String): Resource<SymbolSearchResponse> {
        return try {
            Resource.Success(api.searchSymbol(keywords, apiKey))
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Search failed")
        }
    }

    suspend fun getCompanyInfo(symbol: String): Resource<CompanyInfo> {
        return try {
            val dto = api.getCompanyInfo(symbol, apiKey)
            if (dto.symbol.isNullOrBlank() && dto.name.isNullOrBlank()) {
                return Resource.Error("No company info available for $symbol")
            }
            Resource.Success(dto.toCompanyInfo())
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load company info")
        }
    }


    suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol, apiKey)
            val infos = intradayInfoParser.parse(response.byteStream())
            if (infos.isEmpty()) {
                Resource.Error("Chart data unavailable (API limit reached or no data for $symbol)")
            } else {
                Resource.Success(infos)
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to load chart data")
        }
    }

    fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> = flow {
        emit(Resource.Loading(true))

        val localListings = stockDao.searchCompanyListings(query)
        val isDbEmpty = localListings.isEmpty() && query.isBlank()

        if (!isDbEmpty) {
            emit(Resource.Success(localListings.map { it.toCompanyListing() }))
        }

        val shouldFetchRemote = isDbEmpty || fetchFromRemote
        if (!shouldFetchRemote) {
            emit(Resource.Loading(false))
            return@flow
        }

        val remoteListings = try {
            val response = api.getListings(apiKey)
            companyListingsParser.parse(response.byteStream())
        } catch (e: Exception) {
            emit(Resource.Error("Couldn't load listings: ${e.message}"))
            null
        }

        remoteListings?.let { listings ->
            stockDao.clearCompanyListings()
            stockDao.insertCompanyListings(listings.map { it.toCompanyListingEntity() })
            val refreshed = stockDao.searchCompanyListings(query)
            emit(Resource.Success(refreshed.map { it.toCompanyListing() }))
        }

        emit(Resource.Loading(false))
    }
}