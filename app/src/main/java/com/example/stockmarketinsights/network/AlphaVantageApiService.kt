package com.example.stockmarketinsights.network

import com.example.stockmarketinsights.data.remote.dto.CompanyInfoDto
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

// ── Top Gainers / Losers ───────────────────────────────────────────────────────
data class TopGainersLosersResponse(
    val top_gainers: List<StockItem>,
    val top_losers: List<StockItem>
)

data class StockItem(
    val ticker: String,
    val price: String,
    val change_amount: String,
    val change_percentage: String,
    val volume: String
)

// ── Symbol Search ─────────────────────────────────────────────────────────────
data class SymbolSearchResponse(val bestMatches: List<SearchMatch>)

data class SearchMatch(
    val symbol: String,
    val name: String,
    val region: String,
    val currency: String
)

// ─────────────────────────────────────────────────────────────────────────────
interface AlphaVantageApiService {

    // Existing: Top Gainers & Losers
    @GET("query")
    suspend fun getTopGainersAndLosers(
        @Query("function") function: String = "TOP_GAINERS_LOSERS",
        @Query("apikey")   apiKey: String
    ): TopGainersLosersResponse

    // Existing: Symbol search
    @GET("query")
    suspend fun searchSymbols(
        @Query("function") function: String = "SYMBOL_SEARCH",
        @Query("keywords") keywords: String,
        @Query("apikey")   apiKey: String
    ): SymbolSearchResponse

    // NEW: Company Overview (returns JSON -> CompanyInfoDto via Gson)
    @GET("query")
    suspend fun getCompanyInfo(
        @Query("function") function: String = "OVERVIEW",
        @Query("symbol")   symbol: String,
        @Query("apikey")   apiKey: String
    ): CompanyInfoDto

    // NEW: Full company listing as CSV (ResponseBody for streaming)
    @GET("query?function=LISTING_STATUS")
    suspend fun getListings(
        @Query("apikey") apiKey: String
    ): ResponseBody

    // NEW: Intraday time series as CSV (ResponseBody for streaming)
    @GET("query?function=TIME_SERIES_INTRADAY&interval=60min&datatype=csv")
    suspend fun getIntradayInfo(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): ResponseBody
}
