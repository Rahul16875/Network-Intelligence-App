package com.example.networkintelligence.data.local.db

import androidx.room.TypeConverter
import com.example.networkintelligence.domain.model.NetworkType

class Converters {

    @TypeConverter
    fun networkTypeToString(value: NetworkType): String = value.name

    @TypeConverter
    fun stringToNetworkType(value: String): NetworkType =
        runCatching { NetworkType.valueOf(value) }.getOrDefault(NetworkType.NONE)
}
