package com.example.networkintelligence.data.repository

import android.util.Log
import com.example.networkintelligence.data.local.dao.TowerCacheDao
import com.example.networkintelligence.util.APP_TAG
import com.example.networkintelligence.data.local.entity.TowerCacheEntity
import com.example.networkintelligence.data.remote.CarrierMncResolver
import com.example.networkintelligence.data.remote.OpenCellIdApi
import com.example.networkintelligence.data.remote.dto.TowerDto
import com.example.networkintelligence.domain.model.CarrierCoverage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TowerCoverageRepository @Inject constructor(
    private val dao: TowerCacheDao,
    private val api: OpenCellIdApi,
    private val carrierMncResolver: CarrierMncResolver,
) {

    suspend fun fetchCoverage(bucketKey: String): List<CarrierCoverage> =
        withContext(Dispatchers.IO) {
            Log.d(TAG, "fetchCoverage: start for bucket=$bucketKey")

            val cached = dao.getByBucket(bucketKey)
            if (cached != null && !isCacheExpired(cached.fetchedAt)) {
                val coverage = deserialize(cached.payload)
                if (coverage.isNotEmpty()) {
                    val ageMinutes = (System.currentTimeMillis() - cached.fetchedAt) / 60_000
                    Log.i(TAG, "fetchCoverage: cache HIT for bucket=$bucketKey (age=${ageMinutes}min, ${coverage.size} carriers)")
                    Log.d(TAG, "fetchCoverage: carriers=${coverage.map { "${it.carrierName}(${it.towerCount})" }}")
                    return@withContext coverage
                }
                Log.w(TAG, "fetchCoverage: cache has 0 carriers for bucket=$bucketKey — treating as miss and re-fetching")
            } else if (cached != null) {
                Log.d(TAG, "fetchCoverage: cache EXPIRED for bucket=$bucketKey — fetching fresh data")
            } else {
                Log.d(TAG, "fetchCoverage: no cache for bucket=$bucketKey — fetching from API")
            }

            val result = api.fetchTowersInBucket(bucketKey)

            if (result.isFailure) {
                Log.e(TAG, "fetchCoverage: API call FAILED for bucket=$bucketKey", result.exceptionOrNull())
                val stale = cached?.let { deserialize(it.payload) } ?: emptyList()
                if (stale.isNotEmpty()) Log.w(TAG, "fetchCoverage: returning stale cache as fallback (${stale.size} carriers)")
                else Log.w(TAG, "fetchCoverage: no fallback available — returning empty")
                return@withContext stale
            }

            val towers = result.getOrNull()!!
            Log.i(TAG, "fetchCoverage: API returned ${towers.size} towers for bucket=$bucketKey")

            if (towers.isEmpty()) {
                Log.w(TAG, "fetchCoverage: 0 towers — OpenCelliD has no data for this area")
                return@withContext emptyList()
            }

            val coverage = aggregateByCarrier(towers)
            Log.i(TAG, "fetchCoverage: aggregated into ${coverage.size} carriers:")
            coverage.forEach { c ->
                Log.i(TAG, "  ${c.carrierName}: ${c.towerCount} towers, best=${c.bestRadioLabel}, NR=${c.hasNr}, LTE=${c.hasLte}, UMTS=${c.hasUmts}, GSM=${c.hasGsm}")
            }

            if (coverage.isNotEmpty()) {
                dao.upsert(
                    TowerCacheEntity(
                        bucketKey = bucketKey,
                        fetchedAt = System.currentTimeMillis(),
                        payload = serialize(coverage),
                    ),
                )
                dao.deleteExpired(System.currentTimeMillis() - CACHE_TTL_MS)
                Log.d(TAG, "fetchCoverage: saved ${coverage.size} carriers to cache for bucket=$bucketKey")
            } else {
                Log.w(TAG, "fetchCoverage: API returned 0 carriers — NOT caching empty result, will retry next time")
            }

            coverage
        }

    private fun aggregateByCarrier(towers: List<TowerDto>): List<CarrierCoverage> {
        return towers
            .groupBy { it.mcc to it.mnc }
            .map { (mccMnc, carrierTowers) ->
                val (mcc, mnc) = mccMnc
                val radios = carrierTowers.map { it.radio.uppercase() }.toSet()
                CarrierCoverage(
                    carrierName = carrierMncResolver.resolve(mcc, mnc),
                    mcc = mcc,
                    mnc = mnc,
                    towerCount = carrierTowers.size,
                    hasNr = radios.contains("NR"),
                    hasLte = radios.contains("LTE"),
                    hasUmts = radios.any { it == "UMTS" || it == "WCDMA" || it == "3G" },
                    hasGsm = radios.any { it == "GSM" || it == "2G" },
                )
            }
            .sortedByDescending { it.towerCount }
    }

    private fun serialize(coverage: List<CarrierCoverage>): String {
        val arr = JSONArray()
        coverage.forEach { c ->
            arr.put(
                JSONObject().apply {
                    put("carrierName", c.carrierName)
                    put("mcc", c.mcc)
                    put("mnc", c.mnc)
                    put("towerCount", c.towerCount)
                    put("hasNr", c.hasNr)
                    put("hasLte", c.hasLte)
                    put("hasUmts", c.hasUmts)
                    put("hasGsm", c.hasGsm)
                },
            )
        }
        return arr.toString()
    }

    private fun deserialize(json: String): List<CarrierCoverage> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                CarrierCoverage(
                    carrierName = o.getString("carrierName"),
                    mcc = o.getInt("mcc"),
                    mnc = o.getInt("mnc"),
                    towerCount = o.getInt("towerCount"),
                    hasNr = o.getBoolean("hasNr"),
                    hasLte = o.getBoolean("hasLte"),
                    hasUmts = o.getBoolean("hasUmts"),
                    hasGsm = o.getBoolean("hasGsm"),
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "deserialize: failed to parse cached JSON", e)
            emptyList()
        }
    }

    private fun isCacheExpired(fetchedAt: Long): Boolean =
        System.currentTimeMillis() - fetchedAt > CACHE_TTL_MS

    companion object {
        private const val TAG = APP_TAG
        private val CACHE_TTL_MS = TimeUnit.DAYS.toMillis(7)
    }
}
