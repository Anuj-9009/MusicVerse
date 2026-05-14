package com.musicverse.player.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.musicverse.player.BuildConfig
import com.musicverse.player.data.api.SpotifyAuthApi
import com.musicverse.player.data.api.SpotifyTokenResponse
import com.musicverse.player.security.SecureDataStoreSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages Spotify OAuth 2.0 Authorization Code flow.
 *
 * Flow:
 *   1. [buildAuthUrl] → opens Spotify login in browser
 *   2. User authorizes → redirect to musicverse://callback?code=XXX
 *   3. [handleAuthCallback] → exchanges code for tokens
 *   4. [getValidAccessToken] → auto-refreshes expired tokens
 */

@Serializable
data class SpotifyTokens(
    val accessToken: String? = null,
    val refreshToken: String? = null,
    val expiresAt: Long = 0L,
    val userDisplayName: String? = null
)

@Singleton
class SpotifyAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val spotifyAuthApi: SpotifyAuthApi
) {
    companion object {
        private const val REDIRECT_URI = "musicverse://callback"
        private const val AUTH_URL = "https://accounts.spotify.com/authorize"

        // Scopes needed: read liked songs + user profile
        private val SCOPES = listOf(
            "user-library-read",
            "user-read-private",
            "user-read-email",
            "playlist-read-private",
            "playlist-read-collaborative"
        ).joinToString(" ")
    }

    private val dataStore: DataStore<String> = DataStoreFactory.create(
        serializer = SecureDataStoreSerializer(context, "spotify_auth_keyset"),
        produceFile = { File(context.filesDir, "datastore/spotify_auth_secure.pb") }
    )

    private val json = Json { ignoreUnknownKeys = true }

    private fun parseTokens(jsonString: String): SpotifyTokens {
        return if (jsonString.isEmpty()) SpotifyTokens() else runCatching {
            json.decodeFromString<SpotifyTokens>(jsonString)
        }.getOrDefault(SpotifyTokens())
    }

    private val clientId: String get() = BuildConfig.SPOTIFY_CLIENT_ID
    private val clientSecret: String get() = BuildConfig.SPOTIFY_CLIENT_SECRET

    /**
     * Observe whether the user is authenticated.
     */
    val isAuthenticated: Flow<Boolean> = dataStore.data
        .catch { emit("") }
        .map {
            !parseTokens(it).accessToken.isNullOrBlank()
        }

    /**
     * Observe the user's display name.
     */
    val userDisplayName: Flow<String?> = dataStore.data
        .catch { emit("") }
        .map {
            parseTokens(it).userDisplayName
        }

    /**
     * Build the Spotify authorization URL and launch it in the browser.
     */
    fun buildAuthIntent(): Intent {
        val url = Uri.parse(AUTH_URL).buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", REDIRECT_URI)
            .appendQueryParameter("scope", SCOPES)
            .appendQueryParameter("show_dialog", "true")
            .build()

        return Intent(Intent.ACTION_VIEW, url).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Handle the deep-link callback from Spotify.
     * Exchange the authorization code for access + refresh tokens.
     *
     * @return true if authentication succeeded
     */
    suspend fun handleAuthCallback(uri: Uri): Boolean {
        val code = uri.getQueryParameter("code") ?: return false
        val error = uri.getQueryParameter("error")
        if (error != null) return false

        return try {
            val response = spotifyAuthApi.exchangeCodeForToken(
                code = code,
                redirectUri = REDIRECT_URI,
                clientId = clientId,
                clientSecret = clientSecret
            )
            saveTokens(response)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get a valid access token, auto-refreshing if expired.
     *
     * @return Bearer-prefixed token string, or null if not authenticated
     */
    suspend fun getValidAccessToken(): String? {
        val tokens = parseTokens(dataStore.data.first())
        val accessToken = tokens.accessToken ?: return null
        val refreshToken = tokens.refreshToken ?: return null
        val expiresAt = tokens.expiresAt

        // If token is still valid (with 60s buffer), return it
        if (System.currentTimeMillis() < expiresAt - 60_000) {
            return "Bearer $accessToken"
        }

        // Token expired — refresh it
        return try {
            val response = spotifyAuthApi.refreshToken(
                refreshToken = refreshToken,
                clientId = clientId,
                clientSecret = clientSecret
            )
            saveTokens(response)
            "Bearer ${response.accessToken}"
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Clear all stored tokens (logout).
     */
    suspend fun logout() {
        dataStore.updateData { "" }
    }

    /**
     * Save the display name after fetching user profile.
     */
    suspend fun saveUserDisplayName(name: String) {
        dataStore.updateData { currentJson ->
            val tokens = parseTokens(currentJson).copy(userDisplayName = name)
            json.encodeToString(tokens)
        }
    }

    private suspend fun saveTokens(response: SpotifyTokenResponse) {
        dataStore.updateData { currentJson ->
            val tokens = parseTokens(currentJson).copy(
                accessToken = response.accessToken,
                refreshToken = response.refreshToken ?: parseTokens(currentJson).refreshToken,
                expiresAt = System.currentTimeMillis() + (response.expiresIn * 1000L)
            )
            json.encodeToString(tokens)
        }
    }
}
