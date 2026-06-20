package com.example.stockmarketinsights.data.mapper

import android.R.attr.country
import com.example.stockmarketinsights.data.local.CompanyListingEntity
import com.example.stockmarketinsights.data.remote.dto.CompanyInfoDto
import com.example.stockmarketinsights.data.remote.dto.IntradayInfoDto
import com.example.stockmarketinsights.domain.model.CompanyInfo
import com.example.stockmarketinsights.domain.model.CompanyListing
import com.example.stockmarketinsights.domain.model.IntradayInfo
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun CompanyInfoDto.toCompanyInfo(): CompanyInfo = CompanyInfo(
    symbol = symbol ?: "",
    description = description ?: "",
    name = name ?: "",
    country = country ?: "",
    industry = industry ?: ""
)

fun IntradayInfoDto.toIntradayInfo(): IntradayInfo? {
    return try {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        IntradayInfo(
            date = LocalDateTime.parse(timestamp, formatter),
            close = close
        )
    } catch (e: Exception) {
        null
    }
}

fun CompanyListingEntity.toCompanyListing(): CompanyListing = CompanyListing(
    name = name,
    symbol = symbol,
    exchange = exchange
)

fun CompanyListing.toCompanyListingEntity(): CompanyListingEntity = CompanyListingEntity(
    name = name,
    symbol = symbol,
    exchange = exchange
)
