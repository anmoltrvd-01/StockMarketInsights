package com.example.stockmarketinsights.repository

import android.content.Context
import com.example.stockmarketinsights.BuildConfig
import com.example.stockmarketinsights.data.csv.CompanyListingsParser
import com.example.stockmarketinsights.data.csv.CSVParser
import com.example.stockmarketinsights.data.csv.IntradayInfoParser
import com.example.stockmarketinsights.data.mapper.toCompanyInfo
import com.example.stockmarketinsights.data.mapper.toCompanyListing
import com.example.stockmarketinsights.data.mapper.toCompanyListingEntity
import com.example.stockmarketinsights.data.domain.model.StockSummaryItem
import com.example.stockmarketinsights.domain.model.CompanyInfo
import com.example.stockmarketinsights.domain.model.CompanyListing
import com.example.stockmarketinsights.domain.model.IntradayInfo
import com.example.stockmarketinsights.network.AlphaVantageApiService
import com.example.stockmarketinsights.network.RetrofitInstance
import com.example.stockmarketinsights.roomdb.AppDatabase
import com.example.stockmarketinsights.utils.ApiRateLimiter
import com.example.stockmarketinsights.utils.NetworkUtils
import com.example.stockmarketinsights.utils.Resource
import com.example.stockmarketinsights.utils.toEntity
import com.example.stockmarketinsights.utils.toStockSummaryItem
import com.example.stockmarketinsights.utils.toUi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException

class StockRepository(
    private val context: Context,
    private val api: AlphaVantageApiService = RetrofitInstance.api,
    private val db: AppDatabase,
    private val companyListingsParser: CSVParser<CompanyListing> = CompanyListingsParser(),
    private val intradayInfoParser: CSVParser<IntradayInfo>      = IntradayInfoParser()
) {

    private val stockDao = db.stockDao()
    private val apiKey   = BuildConfig.ALPHA_VANTAGE_API_KEY

    // ─────────────────────────────────────────────────────────────────────────
    // EXISTING: Top gainers / losers (unchanged - used by ExploreScreen)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getTopStocks(type: String): List<StockSummaryItem> {
        if (!NetworkUtils.isConnected(context)) {
            return stockDao.getAllStocks().map { it.toUi() }
        }
        if (!ApiRateLimiter.canCallApi()) {
            return stockDao.getAllStocks().map { it.toUi() }
        }
        ApiRateLimiter.recordCall()

        val response  = api.getTopGainersAndLosers(apiKey = apiKey)
        val apiStocks = if (type.lowercase() == "gainers") response.top_gainers else response.top_losers
        val uiStocks  = apiStocks.map { it.toStockSummaryItem() }

        stockDao.insertStocks(uiStocks.map { it.toEntity() })
        return uiStocks
    }

    suspend fun getTopGainers() = getTopStocks("gainers")
    suspend fun getTopLosers()  = getTopStocks("losers")

    // ─────────────────────────────────────────────────────────────────────────
    // EXISTING: Symbol search (unchanged - used by SearchAllStocksScreen)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun searchSymbol(query: String): List<StockSummaryItem> {
        if (!NetworkUtils.isConnected(context)) {
            return stockDao.searchStocks("%$query%").map { it.toUi() }
        }
        if (!ApiRateLimiter.canCallApi()) return emptyList()
        ApiRateLimiter.recordCall()
        val response = api.searchSymbols(keywords = query, apiKey = apiKey)
        return response.bestMatches.map { it.toStockSummaryItem() }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NEW: Company listings via CSV (used by ExploreScreen company listing feature)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getCompanyListings(
        fetchFromRemote: Boolean,
        query: String
    ): Flow<Resource<List<CompanyListing>>> = flow {

        emit(Resource.Loading(true))

        val localListings = stockDao.searchCompanyListing(query)
        emit(Resource.Success(data = localListings.map { it.toCompanyListing() }))

        val isDbEmpty             = localListings.isEmpty() && query.isBlank()
        val shouldLoadFromCache   = !isDbEmpty && !fetchFromRemote
        if (shouldLoadFromCache) {
            emit(Resource.Loading(false))
            return@flow
        }

        if (!NetworkUtils.isConnected(context)) {
            emit(Resource.Error("No internet connection"))
            emit(Resource.Loading(false))
            return@flow
        }

        val remoteListings = try {
            val response = api.getListings(apiKey = apiKey)
            companyListingsParser.parse(response.byteStream())
        } catch (e: IOException) {
            emit(Resource.Error("Couldn't load listings: ${e.localizedMessage}"))
            null
        } catch (e: HttpException) {
            emit(Resource.Error("Server error: ${e.localizedMessage}"))
            null
        }

        remoteListings?.let { listings ->
            stockDao.clearCompanyListings()
            stockDao.insertCompanyListings(listings.map { it.toCompanyListingEntity() })
            emit(Resource.Success(
                data = stockDao.searchCompanyListing("").map { it.toCompanyListing() }
            ))
        }

        emit(Resource.Loading(false))
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NEW: Intraday chart data via CSV (used by DetailsScreen chart)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getIntradayInfo(symbol: String): Resource<List<IntradayInfo>> {
        return try {
            val response = api.getIntradayInfo(symbol = symbol, apiKey = apiKey)
            val results  = intradayInfoParser.parse(response.byteStream())
            Resource.Success(results)
        } catch (e: IOException) {
            Resource.Error("Couldn't load chart data: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.localizedMessage}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // NEW: Real company overview (used by DetailsScreen description / info)
    // ─────────────────────────────────────────────────────────────────────────

    suspend fun getCompanyInfo(symbol: String): Resource<CompanyInfo> {
        return try {
            val dto    = api.getCompanyInfo(symbol = symbol, apiKey = apiKey)
            val result = dto.toCompanyInfo()
            Resource.Success(result)
        } catch (e: IOException) {
            Resource.Error("Couldn't load company info: ${e.localizedMessage}")
        } catch (e: HttpException) {
            Resource.Error("Server error: ${e.localizedMessage}")
        }
    }
}
