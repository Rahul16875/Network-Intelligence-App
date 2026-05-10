package com.example.networkintelligence.data.speedtest

import android.util.Log
import com.example.networkintelligence.util.APP_TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class SpeedTestClient @Inject constructor(
    @Named("speedtest") private val client: OkHttpClient,
) {

    data class Result(
        val downloadMbps: Float?,
        val uploadMbps: Float?,
    )

    suspend fun measure(): Result = withContext(Dispatchers.IO) {
        // Two samples each; average the non-null results for stability.
        val dlSamples = (1..SAMPLE_COUNT).mapNotNull { measureDownload() }
        val ulSamples = (1..SAMPLE_COUNT).mapNotNull { measureUpload() }

        val download = if (dlSamples.isEmpty()) null else dlSamples.average().toFloat()
        val upload   = if (ulSamples.isEmpty()) null else ulSamples.average().toFloat()

        Log.i(APP_TAG, "SpeedTest: dl_samples=$dlSamples → ${download}Mbps | ul_samples=$ulSamples → ${upload}Mbps")
        Result(downloadMbps = download, uploadMbps = upload)
    }

    /**
     * Measures download throughput by timing ONLY the body transfer — not
     * DNS/TCP/TLS overhead. Timer starts after response headers arrive.
     */
    private fun measureDownload(): Float? {
        val request = Request.Builder()
            .url("$CLOUDFLARE_BASE/__down?bytes=$DOWNLOAD_BYTES")
            .header("Cache-Control", "no-cache")
            .get()
            .build()
        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val body = response.body ?: return null

                // Start timing HERE — connection overhead already paid.
                val start = System.nanoTime()
                var totalBytes = 0L
                val buf = ByteArray(BUFFER_SIZE)
                val stream = body.byteStream()
                var read: Int
                while (stream.read(buf).also { read = it } != -1) {
                    totalBytes += read
                }

                val elapsedSec = (System.nanoTime() - start) / 1_000_000_000.0
                if (elapsedSec <= 0.0 || totalBytes == 0L) return null
                val mbps = ((totalBytes * 8L) / elapsedSec / 1_000_000.0).toFloat()
                Log.d(APP_TAG, "SpeedTest dl: ${totalBytes}B in ${elapsedSec}s = ${mbps}Mbps")
                mbps
            }
        } catch (t: Throwable) {
            Log.w(APP_TAG, "SpeedTest download failed: ${t.message}")
            null
        }
    }

    /**
     * Measures upload by timing the full request (including send + response).
     * OkHttp reuses the keep-alive connection from download so connection
     * overhead is minimal for the second+ requests.
     */
    private fun measureUpload(): Float? {
        val uploadBytes = UPLOAD_BYTES
        val body = object : RequestBody() {
            override fun contentType() = "application/octet-stream".toMediaType()
            override fun contentLength() = uploadBytes.toLong()
            override fun writeTo(sink: BufferedSink) {
                val chunk = ByteArray(BUFFER_SIZE)
                var remaining = uploadBytes
                while (remaining > 0) {
                    val toWrite = minOf(chunk.size, remaining)
                    sink.write(chunk, 0, toWrite)
                    remaining -= toWrite
                }
            }
        }
        val request = Request.Builder()
            .url("$CLOUDFLARE_BASE/__up")
            .post(body)
            .build()
        return try {
            val start = System.nanoTime()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return null
                val elapsedSec = (System.nanoTime() - start) / 1_000_000_000.0
                if (elapsedSec <= 0.0) return null
                val mbps = ((uploadBytes * 8L) / elapsedSec / 1_000_000.0).toFloat()
                Log.d(APP_TAG, "SpeedTest ul: ${uploadBytes}B in ${elapsedSec}s = ${mbps}Mbps")
                mbps
            }
        } catch (t: Throwable) {
            Log.w(APP_TAG, "SpeedTest upload failed: ${t.message}")
            null
        }
    }

    companion object {
        private const val CLOUDFLARE_BASE  = "https://speed.cloudflare.com"
        private const val DOWNLOAD_BYTES   = 10_000_000  // 10 MB
        private const val UPLOAD_BYTES     = 4_000_000   // 4 MB (increased for accuracy)
        private const val SAMPLE_COUNT     = 2           // average over 2 runs
        private const val BUFFER_SIZE      = 16_384      // 16 KB read buffer
    }
}
