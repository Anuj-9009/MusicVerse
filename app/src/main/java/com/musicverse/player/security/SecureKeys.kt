package com.musicverse.player.security

/**
 * SecureKeys — Kotlin JNI bridge to the native C++ key vault.
 *
 * Falls back to BuildConfig values for local debug builds
 * where the native library may not be compiled yet.
 */
object SecureKeys {

    init {
        try {
            System.loadLibrary("musicverse_keys")
        } catch (e: UnsatisfiedLinkError) {
            // NDK not compiled in debug builds — fall back to BuildConfig
        }
    }

    private external fun getSpotifyClientIdNative(): String
    private external fun getYouTubeApiKeyNative(): String
    private external fun getGeminiApiKeyNative(): String

    fun getSpotifyClientId(): String = runCatching {
        getSpotifyClientIdNative()
    }.getOrElse { com.musicverse.player.BuildConfig.SPOTIFY_CLIENT_ID }

    fun getYouTubeApiKey(): String = runCatching {
        getYouTubeApiKeyNative()
    }.getOrElse { com.musicverse.player.BuildConfig.YOUTUBE_API_KEY }

    fun getGeminiApiKey(): String = runCatching {
        getGeminiApiKeyNative()
    }.getOrElse { com.musicverse.player.BuildConfig.GEMINI_API_KEY }
}
