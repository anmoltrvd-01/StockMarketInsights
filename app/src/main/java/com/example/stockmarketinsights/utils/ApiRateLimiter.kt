package com.example.stockmarketinsights.utils

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Simple token-bucket rate limiter for Alpha Vantage free tier (5 calls/min, 500/day).
 */
class ApiRateLimiter(
    private val maxCallsPerMinute: Int = 5
) {
    private val mutex = Mutex()
    private val callTimestamps = ArrayDeque<Long>()

    suspend fun <T> throttled(block: suspend () -> T): T {
        mutex.withLock {
            val now = System.currentTimeMillis()
            // Remove timestamps older than 1 minute
            while (callTimestamps.isNotEmpty() && now - callTimestamps.first() > 60_000) {
                callTimestamps.removeFirst()
            }
            if (callTimestamps.size >= maxCallsPerMinute) {
                val waitMs = 60_000 - (now - callTimestamps.first()) + 100
                if (waitMs > 0) kotlinx.coroutines.delay(waitMs)
            }
            callTimestamps.addLast(System.currentTimeMillis())
        }
        return block()
    }
}
