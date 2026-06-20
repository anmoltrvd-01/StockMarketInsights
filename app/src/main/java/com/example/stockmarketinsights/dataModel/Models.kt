package com.example.stockmarketinsights.dataModel

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

// ----- Explore / Search -----

@Parcelize
data class StockSummaryItem(
    val symbol: String,
    val name: String,
    val price: String,
    val change: String,
    val changePercent: String,
    val volume: String = "N/A"
) : Parcelable

data class UiState<T>(
    val isLoading: Boolean = false,
    val data: T? = null,
    val error: String? = null
)

// ----- Top Gainers/Losers API response -----

data class TopGainersLosersResponse(
    @SerializedName("top_gainers") val topGainers: List<StockItem>?,
    @SerializedName("top_losers") val topLosers: List<StockItem>?,
    @SerializedName("most_actively_traded") val mostActive: List<StockItem>?
)

data class StockItem(
    @SerializedName("ticker") val ticker: String,
    @SerializedName("price") val price: String,
    @SerializedName("change_amount") val changeAmount: String,
    @SerializedName("change_percentage") val changePercentage: String,
    @SerializedName("volume") val volume: String
)

// ----- Symbol Search API response -----

data class SymbolSearchResponse(
    @SerializedName("bestMatches") val bestMatches: List<SymbolMatch>?
)

data class SymbolMatch(
    @SerializedName("1. symbol") val symbol: String,
    @SerializedName("2. name") val name: String,
    @SerializedName("4. region") val region: String,
    @SerializedName("9. matchScore") val matchScore: String
)

// ----- Watchlist -----

data class WatchlistItem(
    val watchlistName: String,
    val stockName: String,
    val symbol: String
)
