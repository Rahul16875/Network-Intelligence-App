package com.example.networkintelligence.data.remote

import android.util.Log
import com.example.networkintelligence.BuildConfig
import com.example.networkintelligence.util.APP_TAG
import com.example.networkintelligence.data.remote.dto.TowerDto
import okhttp3.OkHttpClient
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Named

class OpenCellIdApi @Inject constructor(
    @Named("opencellid") private val client: OkHttpClient,
    private val carrierMncResolver: CarrierMncResolver,
) {

    fun fetchTowersInBucket(bucketKey: String): Result<List<TowerDto>> {
        val bbox = bucketToBbox(bucketKey)
        if (bbox == null) {
            Log.w(TAG, "fetchTowers: could not parse bucket key '$bucketKey'")
            return Result.success(emptyList())
        }

        val apiKey = BuildConfig.OPENCELLID_API_KEY
        if (apiKey.isBlank()) {
            Log.e(TAG, "fetchTowers: OPENCELLID_API_KEY is blank — check local.properties")
            return Result.success(emptyList())
        }

        val url = "https://opencellid.org/cell/getInArea" +
            "?key=$apiKey" +
            "&BBOX=${bbox.south},${bbox.west},${bbox.north},${bbox.east}" +
            "&format=json"

        Log.d(TAG, "fetchTowers: bucket=$bucketKey bbox=${bbox.south},${bbox.west},${bbox.north},${bbox.east}")

        return try {
            val request = okhttp3.Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            Log.d(TAG, "fetchTowers: HTTP ${response.code}")

            if (!response.isSuccessful) {
                Log.e(TAG, "fetchTowers: API error HTTP ${response.code} — body: ${response.body?.string()?.take(300)}")
                return Result.failure(Exception("OpenCelliD HTTP ${response.code}"))
            }

            val body = response.body?.string()
            if (body.isNullOrBlank()) {
                Log.w(TAG, "fetchTowers: empty response body")
                return Result.success(emptyList())
            }

            Log.d(TAG, "fetchTowers: raw response (first 500 chars): ${body.take(500)}")
            val towers = parseTowers(body)
            Log.i(TAG, "fetchTowers: parsed ${towers.size} towers for bucket=$bucketKey")
            towers.groupBy { "${it.mcc}-${it.mnc}" }.forEach { (mccMnc, t) ->
                Log.d(TAG, "  carrier $mccMnc (${carrierMncResolver.resolve(
                    t.first().mcc, t.first().mnc
                )}): ${t.size} towers, radios=${t.map { it.radio }.toSet()}")
            }

            Result.success(towers)
        } catch (e: Exception) {
            Log.e(TAG, "fetchTowers: exception for bucket=$bucketKey", e)
            Result.failure(e)
        }
    }

    private fun parseTowers(json: String): List<TowerDto> {
        return try {
            val root = JSONObject(json)
            val cells = root.optJSONArray("cells")
            if (cells == null) {
                Log.w(TAG, "parseTowers: no 'cells' array in response. Keys: ${root.keys().asSequence().toList()}")
                return emptyList()
            }
            Log.d(TAG, "parseTowers: 'cells' array has ${cells.length()} entries")
            (0 until cells.length()).mapNotNull { i ->
                val cell = cells.getJSONObject(i)
                TowerDto(
                    lat = cell.getDouble("lat"),
                    lon = cell.getDouble("lon"),
                    mcc = cell.getInt("mcc"),
                    mnc = cell.getInt("mnc"),
                    radio = cell.optString("radio", "GSM"),
                    range = cell.optInt("range", 0),
                    samples = cell.optInt("samples", 1),
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "parseTowers: JSON parse error", e)
            emptyList()
        }
    }

    private data class BBox(val south: Double, val west: Double, val north: Double, val east: Double)

    private fun bucketToBbox(bucketKey: String): BBox? {
        val parts = bucketKey.split("_")
        if (parts.size != 2) return null
        val latBucket = parts[0].toIntOrNull() ?: return null
        val lngBucket = parts[1].toIntOrNull() ?: return null
        // Exact bucket only (~1.1km × 1.1km ≈ 1.2 km²) — OpenCelliD free tier limit is 4 km²
        return BBox(
            south = latBucket / 100.0,
            north = (latBucket + 1) / 100.0,
            west = lngBucket / 100.0,
            east = (lngBucket + 1) / 100.0,
        )
    }

    companion object {
        private const val TAG = APP_TAG
    }
}
