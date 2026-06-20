package com.example.stockmarketinsights.network

import com.example.stockmarketinsights.data.remote.dto.CompanyInfoDto
import com.example.stockmarketinsights.dataModel.SymbolSearchResponse
import com.example.stockmarketinsights.dataModel.TopGainersLosersResponse
import okhttp3.ResponseBody
import retrofit2.http.GET
import retrofit2.http.Query

interface AlphaVantageApiService {

    companion object {
        const val BASE_URL = "https://www.alphavantage.co/"
    }

    /** Top gainers, losers, and most active */
    @GET("query?function=TOP_GAINERS_LOSERS")
    suspend fun getTopGainersLosers(
        @Query("apikey") apiKey: String
    ): TopGainersLosersResponse

    /** Symbol keyword search */
    @GET("query?function=SYMBOL_SEARCH")
    suspend fun searchSymbol(
        @Query("keywords") keywords: String,
        @Query("apikey") apiKey: String
    ): SymbolSearchResponse

    /** Company overview / info */
    @GET("query?function=OVERVIEW")
    suspend fun getCompanyInfo(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): CompanyInfoDto

    /** Full company listing CSV (LISTING_STATUS) */
    @GET("query?function=LISTING_STATUS")
    suspend fun getListings(
        @Query("apikey") apiKey: String
    ): ResponseBody

    /** Intraday time-series CSV */
    @GET("query?function=TIME_SERIES_INTRADAY&interval=60min&datatype=csv")
    suspend fun getIntradayInfo(
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): ResponseBody
}
