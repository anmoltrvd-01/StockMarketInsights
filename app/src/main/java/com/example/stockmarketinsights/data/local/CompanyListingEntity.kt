package com.example.stockmarketinsights.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company_listings")
data class CompanyListingEntity(
    val name: String,
    val symbol: String,
    val exchange: String,
    @PrimaryKey val id: Int? = null
)
