package com.example.networkintelligence.di

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /** Short-timeout client used for latency probes. */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(5, TimeUnit.SECONDS)
            .callTimeout(7, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()

    /** Long-timeout client for speed tests (up to 60s to download 10 MB). */
    @Provides
    @Singleton
    @Named("speedtest")
    fun provideSpeedTestClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .callTimeout(90, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()

    /** Long-timeout client for Gemini API calls. */
    @Provides
    @Singleton
    @Named("gemini")
    fun provideGeminiClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .callTimeout(40, TimeUnit.SECONDS)
            .retryOnConnectionFailure(false)
            .build()

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(
        @ApplicationContext context: Context,
    ): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
}
