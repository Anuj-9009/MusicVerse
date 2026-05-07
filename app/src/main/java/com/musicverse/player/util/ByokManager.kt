package com.musicverse.player.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.DataStoreFactory
import com.musicverse.player.security.SecureDataStoreSerializer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BYOK (Bring Your Own Key) Manager
 *
 * Allows power users to supply their own API keys via the Settings screen.
 * When a custom key is set, it overrides the app's embedded key,
 * routing heavy API usage through the user's own quota.
 *
 * Keys are stored encrypted in DataStore. The app checks these first
 * before falling back to the NDK/BuildConfig values.
 */
@Serializable
data class ByokKeys(
    val geminiKey: String? = null,
    val youtubeKey: String? = null
)

@Singleton
class ByokManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore: DataStore<String> = DataStoreFactory.create(
        serializer = SecureDataStoreSerializer(context, "byok_keyset"),
        produceFile = { File(context.filesDir, "datastore/byok_secure.pb") }
    )

    private val json = Json { ignoreUnknownKeys = true }

    private fun parseKeys(jsonString: String): ByokKeys {
        return if (jsonString.isEmpty()) ByokKeys() else runCatching {
            json.decodeFromString<ByokKeys>(jsonString)
        }.getOrDefault(ByokKeys())
    }

    // ── Gemini Key ────────────────────────────────────────────────────────
    val geminiKey: Flow<String?> = dataStore.data
        .map { parseKeys(it).geminiKey?.takeIf { k -> k.isNotBlank() } }

    suspend fun setGeminiKey(key: String) {
        dataStore.updateData { currentJson ->
            val keys = parseKeys(currentJson).copy(geminiKey = key.trim().takeIf { it.isNotBlank() })
            json.encodeToString(keys)
        }
    }

    // ── YouTube Key ───────────────────────────────────────────────────────
    val youTubeKey: Flow<String?> = dataStore.data
        .map { parseKeys(it).youtubeKey?.takeIf { k -> k.isNotBlank() } }

    suspend fun setYouTubeKey(key: String) {
        dataStore.updateData { currentJson ->
            val keys = parseKeys(currentJson).copy(youtubeKey = key.trim().takeIf { it.isNotBlank() })
            json.encodeToString(keys)
        }
    }

    suspend fun clearAllKeys() {
        dataStore.updateData { "" }
    }
}
