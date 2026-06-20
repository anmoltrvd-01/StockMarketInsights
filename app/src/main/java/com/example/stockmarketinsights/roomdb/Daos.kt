package com.example.stockmarketinsights.roomdb

import androidx.room.*
import com.example.stockmarketinsights.data.local.CompanyListingEntity

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlists")
    suspend fun getAllWatchlists(): List<WatchlistEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWatchlist(watchlist: WatchlistEntity)

    @Delete
    suspend fun deleteWatchlist(watchlist: WatchlistEntity)

    @Query("DELETE FROM watchlists WHERE watchlistName = :name")
    suspend fun deleteWatchlistByName(name: String)

    @Query("SELECT * FROM watchlist_stocks WHERE watchlistName = :watchlistName")
    suspend fun getStocksInWatchlist(watchlistName: String): List<WatchlistStockEntity>

    /** Count of stocks in a single watchlist — used for the "N stocks" subtitle */
    @Query("SELECT COUNT(*) FROM watchlist_stocks WHERE watchlistName = :watchlistName")
    suspend fun getStockCountInWatchlist(watchlistName: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addStockToWatchlist(stock: WatchlistStockEntity)

    @Query("DELETE FROM watchlist_stocks WHERE watchlistName = :watchlistName AND stockSymbol = :symbol")
    suspend fun removeStockFromWatchlist(watchlistName: String, symbol: String)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist_stocks WHERE watchlistName = :watchlistName AND stockSymbol = :symbol)")
    suspend fun isStockInWatchlist(watchlistName: String, symbol: String): Boolean

    @Query("DELETE FROM watchlist_stocks WHERE watchlistName = :name")
    suspend fun deleteAllStocksInWatchlist(name: String)
}

@Dao
interface StockDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStocks(stocks: List<StockEntity>)

    @Query("SELECT * FROM cached_stocks WHERE type = :type")
    suspend fun getStocksByType(type: String): List<StockEntity>

    @Query("DELETE FROM cached_stocks WHERE type = :type")
    suspend fun clearStocksByType(type: String)

    @Query("SELECT * FROM cached_stocks WHERE symbol LIKE '%' || :query || '%' OR name LIKE '%' || :query || '%'")
    suspend fun searchCachedStocks(query: String): List<StockEntity>

    // ---- Company Listings (CSV-based, from Alpha Vantage LISTING_STATUS) ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompanyListings(listings: List<CompanyListingEntity>)

    @Query("DELETE FROM company_listings")
    suspend fun clearCompanyListings()

    @Query("""
        SELECT * FROM company_listings
        WHERE LOWER(name) LIKE '%' || LOWER(:query) || '%'
           OR UPPER(:query) == symbol
        LIMIT 100
    """)
    suspend fun searchCompanyListings(query: String): List<CompanyListingEntity>

    @Query("SELECT COUNT(*) FROM company_listings")
    suspend fun countCompanyListings(): Int
}