package com.example.stockmarketinsights.domain.model

import java.time.LocalDateTime

data class CompanyListing(
    val name: String,
    val symbol: String,
    val exchange: String
)

data class CompanyInfo(
    val symbol: String,
    val description: String,
    val name: String,
    val country: String,
    val industry: String
)

data class IntradayInfo(
    val date: LocalDateTime,
    val close: Double
)
