package com.musicverse.player.di

import com.musicverse.player.data.api.SpotifyApiService
import com.musicverse.player.data.api.SpotifyAuthApi
import com.musicverse.player.data.api.YouTubeApiService
import com.musicverse.player.data.api.SponsorBlockApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import com.musicverse.player.BuildConfig

/**
 * Hilt module providing network dependencies.
 *
 * Three Retrofit instances:
 *   1. Spotify API (api.spotify.com/v1/)
 *   2. Spotify Auth (accounts.spotify.com/)
 *   3. YouTube Data API (www.googleapis.com/youtube/v3/)
 *   4. SponsorBlock API (sponsor.ajay.app/)
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
        coerceInputValues = true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("SpotifyApi")
    fun provideSpotifyRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.spotify.com/v1/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    @Named("SpotifyAuth")
    fun provideSpotifyAuthRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://accounts.spotify.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    @Named("YouTube")
    fun provideYouTubeRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://www.googleapis.com/youtube/v3/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideSpotifyApiService(@Named("SpotifyApi") retrofit: Retrofit): SpotifyApiService {
        return retrofit.create(SpotifyApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideSpotifyAuthApi(@Named("SpotifyAuth") retrofit: Retrofit): SpotifyAuthApi {
        return retrofit.create(SpotifyAuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideYouTubeApiService(@Named("YouTube") retrofit: Retrofit): YouTubeApiService {
        return retrofit.create(YouTubeApiService::class.java)
    }

    @Provides
    @Singleton
    @Named("SponsorBlock")
    fun provideSponsorBlockRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://sponsor.ajay.app/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }

    @Provides
    @Singleton
    fun provideSponsorBlockApiService(@Named("SponsorBlock") retrofit: Retrofit): SponsorBlockApiService {
        return retrofit.create(SponsorBlockApiService::class.java)
    }
}

