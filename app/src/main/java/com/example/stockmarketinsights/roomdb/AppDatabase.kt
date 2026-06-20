package com.example.stockmarketinsights.roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.stockmarketinsights.data.local.CompanyListingEntity
import com.example.stockmarketinsights.data.local.WatchlistDao

@Database(
    entities = [
        WatchlistEntity::class,
        StockEntity::class,
        CompanyListingEntity::class   // ← added
    ],
    version = 3,                       // ← bumped from 2
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun watchlistDao(): WatchlistDao
    abstract fun stockDao(): StockDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "stock_market_db"
                )
                    .fallbackToDestructiveMigration() // safe during dev
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
