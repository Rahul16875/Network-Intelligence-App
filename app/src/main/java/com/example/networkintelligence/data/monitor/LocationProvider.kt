package com.example.networkintelligence.data.monitor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import android.util.Log
import com.example.networkintelligence.util.APP_TAG
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.math.floor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationProvider @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedClient: FusedLocationProviderClient,
) {

    suspend fun getCoarseBucket(): String? {
        if (!hasAnyLocationPermission()) {
            Log.w(TAG, "getCoarseBucket: no location permission granted")
            return null
        }
        val location = lastKnownLocation()
        if (location == null) {
            Log.w(TAG, "getCoarseBucket: location is null (GPS off or cold start with no cached location)")
            return null
        }
        val bucket = bucketHash(location.latitude, location.longitude)
        Log.i(TAG, "getCoarseBucket: lat=${location.latitude} lng=${location.longitude} → bucket=$bucket")
        return bucket
    }

    private suspend fun lastKnownLocation(): Location? {
        if (!hasAnyLocationPermission()) return null
        return try {
            awaitLastLocation() ?: awaitCurrentLocation()
        } catch (t: SecurityException) {
            null
        } catch (t: Throwable) {
            null
        }
    }

    private suspend fun awaitLastLocation(): Location? = suspendCancellableCoroutine { cont ->
        try {
            fusedClient.lastLocation
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
                .addOnCanceledListener { cont.resume(null) }
        } catch (t: SecurityException) {
            cont.resume(null)
        } catch (t: Throwable) {
            cont.resume(null)
        }
    }

    private suspend fun awaitCurrentLocation(): Location? = suspendCancellableCoroutine { cont ->
        try {
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { cont.resume(it) }
                .addOnFailureListener { cont.resume(null) }
                .addOnCanceledListener { cont.resume(null) }
        } catch (t: SecurityException) {
            cont.resume(null)
        } catch (t: Throwable) {
            cont.resume(null)
        }
    }

    private fun hasAnyLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    private fun bucketHash(lat: Double, lng: Double): String {
        val latBucket = floor(lat * BUCKETS_PER_DEGREE).toInt()
        val lngBucket = floor(lng * BUCKETS_PER_DEGREE).toInt()
        return "${latBucket}_$lngBucket"
    }

    private companion object {
        private const val TAG = APP_TAG
        // 100 buckets per degree latitude ≈ ~1.11 km per bucket.
        const val BUCKETS_PER_DEGREE: Double = 100.0
    }
}
