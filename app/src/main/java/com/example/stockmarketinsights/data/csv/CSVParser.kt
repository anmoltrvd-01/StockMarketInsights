package com.example.stockmarketinsights.data.csv

import com.example.stockmarketinsights.data.mapper.toIntradayInfo
import com.example.stockmarketinsights.data.remote.dto.IntradayInfoDto
import com.example.stockmarketinsights.domain.model.CompanyListing
import com.example.stockmarketinsights.domain.model.IntradayInfo
import com.opencsv.CSVReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.InputStreamReader

interface CSVParser<T> {
    suspend fun parse(stream: InputStream): List<T>
}

class CompanyListingsParser : CSVParser<CompanyListing> {
    override suspend fun parse(stream: InputStream): List<CompanyListing> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            val rows = csvReader.readAll()
            val result = rows
                .drop(1) // skip header row
                .mapNotNull { line ->
                    val symbol = line.getOrNull(0)
                    val name = line.getOrNull(1)
                    val exchange = line.getOrNull(2)
                    CompanyListing(
                        name = name ?: return@mapNotNull null,
                        symbol = symbol ?: return@mapNotNull null,
                        exchange = exchange ?: return@mapNotNull null
                    )
                }
            csvReader.close()
            result
        }
    }
}

class IntradayInfoParser : CSVParser<IntradayInfo> {
    override suspend fun parse(stream: InputStream): List<IntradayInfo> {
        val csvReader = CSVReader(InputStreamReader(stream))
        return withContext(Dispatchers.IO) {
            val rows = csvReader.readAll()
            csvReader.close()

            // Alpha Vantage's free tier sometimes returns a JSON error
            // ("Note"/"Information" about rate limits) instead of real CSV.
            // In that case the header row won't look like a CSV header at all.
            if (rows.isEmpty() || rows.size < 2) {
                return@withContext emptyList()
            }
            val header = rows.first().joinToString(",")
            if (!header.contains("timestamp", ignoreCase = true)) {
                // Not a real CSV (likely an API limit / error message)
                return@withContext emptyList()
            }

            rows
                .drop(1) // skip header
                .mapNotNull { line ->
                    val timestamp = line.getOrNull(0) ?: return@mapNotNull null
                    val closeStr = line.getOrNull(4) ?: return@mapNotNull null
                    val close = closeStr.toDoubleOrNull() ?: return@mapNotNull null
                    IntradayInfoDto(timestamp, close).toIntradayInfo()
                }
                // Group by date and take the most recent day's data
                .groupBy { it.date.toLocalDate() }
                .maxByOrNull { it.key }
                ?.value
                ?.sortedBy { it.date.hour }
                ?: emptyList()
        }
    }
}