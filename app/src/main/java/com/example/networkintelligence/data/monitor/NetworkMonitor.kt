package com.example.networkintelligence.data.monitor

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.TelephonyManager
import androidx.core.content.ContextCompat
import com.example.networkintelligence.domain.model.NetworkType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    data class Snapshot(
        val networkType: NetworkType,
        val providerName: String?,
        val signalStrengthDbm: Int?,
    )

    fun captureSnapshot(): Snapshot {
        val connectivity = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivity.activeNetwork
        val capabilities = activeNetwork?.let(connectivity::getNetworkCapabilities)

        return when {
            capabilities == null -> Snapshot(NetworkType.NONE, providerName = null, signalStrengthDbm = null)
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> wifiSnapshot()
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> cellularSnapshot()
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ->
                Snapshot(NetworkType.ETHERNET, providerName = null, signalStrengthDbm = null)
            else -> Snapshot(NetworkType.NONE, providerName = null, signalStrengthDbm = null)
        }
    }

    private fun wifiSnapshot(): Snapshot {
        val wifi = context.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        val rssi = try {
            wifi?.connectionInfo?.rssi
        } catch (t: Throwable) {
            null
        }
        val ssid = try {
            wifi?.connectionInfo?.ssid?.trim('"')?.takeIf { it.isNotEmpty() && it != "<unknown ssid>" }
        } catch (t: Throwable) {
            null
        }
        return Snapshot(
            networkType = NetworkType.WIFI,
            providerName = ssid,
            signalStrengthDbm = rssi?.takeIf { it in -127..0 },
        )
    }

    private fun cellularSnapshot(): Snapshot {
        val telephony = context.getSystemService(Context.TELEPHONY_SERVICE) as? TelephonyManager
        val carrierName = try {
            telephony?.networkOperatorName?.takeIf { it.isNotBlank() }
        } catch (t: Throwable) {
            null
        }
        val generation = detectCellularGeneration(telephony)
        val signal = readCellularSignalDbm(telephony)
        return Snapshot(
            networkType = generation,
            providerName = carrierName,
            signalStrengthDbm = signal,
        )
    }

    private fun detectCellularGeneration(telephony: TelephonyManager?): NetworkType {
        val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE,
        ) == PackageManager.PERMISSION_GRANTED

        if (telephony == null || !hasPhoneStatePermission) return NetworkType.CELLULAR_UNKNOWN

        val dataNetworkType = try {
            @Suppress("DEPRECATION")
            telephony.dataNetworkType
        } catch (t: SecurityException) {
            return NetworkType.CELLULAR_UNKNOWN
        } catch (t: Throwable) {
            return NetworkType.CELLULAR_UNKNOWN
        }

        return when (dataNetworkType) {
            TelephonyManager.NETWORK_TYPE_NR -> NetworkType.CELLULAR_5G
            TelephonyManager.NETWORK_TYPE_LTE -> NetworkType.CELLULAR_4G
            TelephonyManager.NETWORK_TYPE_HSPAP,
            TelephonyManager.NETWORK_TYPE_HSPA,
            TelephonyManager.NETWORK_TYPE_HSDPA,
            TelephonyManager.NETWORK_TYPE_HSUPA,
            TelephonyManager.NETWORK_TYPE_UMTS,
            TelephonyManager.NETWORK_TYPE_EVDO_B -> NetworkType.CELLULAR_3G
            TelephonyManager.NETWORK_TYPE_EDGE,
            TelephonyManager.NETWORK_TYPE_GPRS,
            TelephonyManager.NETWORK_TYPE_CDMA,
            TelephonyManager.NETWORK_TYPE_1xRTT -> NetworkType.CELLULAR_2G
            else -> NetworkType.CELLULAR_UNKNOWN
        }
    }

    private fun readCellularSignalDbm(telephony: TelephonyManager?): Int? {
        if (telephony == null) return null
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val hasPhoneStatePermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_PHONE_STATE,
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPhoneStatePermission) return null
        return try {
            telephony.signalStrength?.cellSignalStrengths
                ?.mapNotNull { it.dbm.takeIf { v -> v in -140..-30 } }
                ?.firstOrNull()
        } catch (t: SecurityException) {
            null
        } catch (t: Throwable) {
            null
        }
    }
}
