package com.musicverse.player.data.local.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import android.util.Base64

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "musicverse_secure_prefs")

class SecurePreferences(private val context: Context) {

    private var aead: Aead? = null

    init {
        AeadConfig.register()
    }

    private suspend fun getAead(): Aead = withContext(Dispatchers.IO) {
        if (aead == null) {
            aead = AndroidKeysetManager.Builder()
                .withSharedPref(context, "musicverse_keyset", "musicverse_prefs")
                .withKeyTemplate(KeyTemplates.get("AES128_GCM"))
                .withMasterKeyUri("android-keystore://musicverse_master_key")
                .build()
                .keysetHandle
                .getPrimitive(Aead::class.java)
        }
        aead!!
    }

    suspend fun saveSecureString(key: String, value: String) {
        val aeadInstance = getAead()
        val encrypted = aeadInstance.encrypt(value.toByteArray(Charsets.UTF_8), key.toByteArray(Charsets.UTF_8))
        val encoded = Base64.encodeToString(encrypted, Base64.DEFAULT)
        val prefKey = stringPreferencesKey(key)
        context.dataStore.edit { prefs ->
            prefs[prefKey] = encoded
        }
    }

    suspend fun getSecureString(key: String): Flow<String?> {
        val aeadInstance = getAead()
        val prefKey = stringPreferencesKey(key)
        return context.dataStore.data.map { prefs ->
            val encoded = prefs[prefKey] ?: return@map null
            try {
                val decoded = Base64.decode(encoded, Base64.DEFAULT)
                val decrypted = aeadInstance.decrypt(decoded, key.toByteArray(Charsets.UTF_8))
                String(decrypted, Charsets.UTF_8)
            } catch (e: Exception) {
                null
            }
        }
    }
}
