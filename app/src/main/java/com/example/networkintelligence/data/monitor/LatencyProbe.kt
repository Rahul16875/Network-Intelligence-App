package com.example.networkintelligence.data.monitor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LatencyProbe @Inject constructor(
    private val client: OkHttpClient,
) {

    data class Result(
        val avgLatencyMs: Long?,
        val failureRate: Float,
        val successes: Int,
        val attempts: Int,
    )

    suspend fun probe(
        attempts: Int = DEFAULT_ATTEMPTS,
        url: String = DEFAULT_PROBE_URL,
    ): Result = withContext(Dispatchers.IO) {
        val latencies = mutableListOf<Long>()
        var successes = 0

        repeat(attempts) {
            val latency = runProbe(url)
            if (latency != null) {
                latencies += latency
                successes++
            }
        }

        val avg = if (latencies.isEmpty()) null else latencies.sum() / latencies.size
        val failureRate = (attempts - successes).toFloat() / attempts
        Result(
            avgLatencyMs = avg,
            failureRate = failureRate,
            successes = successes,
            attempts = attempts,
        )
    }

    private fun runProbe(url: String): Long? {
        val request = Request.Builder()
            .url(url)
            .head()
            .header("Cache-Control", "no-cache")
            .build()
        val start = System.nanoTime()
        return try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful || response.code == 204) {
                    (System.nanoTime() - start) / 1_000_000L
                } else {
                    null
                }
            }
        } catch (t: Throwable) {
            null
        }
    }

    companion object {
        private const val DEFAULT_ATTEMPTS = 3
        private const val DEFAULT_PROBE_URL = "https://www.google.com/generate_204"
    }
}
