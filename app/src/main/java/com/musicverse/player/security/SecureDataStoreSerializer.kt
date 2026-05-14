package com.musicverse.player.security

import android.content.Context
import androidx.datastore.core.Serializer
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import android.util.Log

/**
 * SecureDataStoreSerializer — Tink AEAD Encryption for Jetpack DataStore
 *
 * Provides on-the-fly encryption/decryption using Google Tink's AES-128 GCM.
 * Replaces the vulnerable plain-text `preferencesDataStore` for storing
 * sensitive API keys and OAuth tokens.
 */
class SecureDataStoreSerializer(
    private val context: Context,
    private val keysetName: String
) : Serializer<String> {

    override val defaultValue: String = ""

    private val aead: Aead? by lazy {
        try {
            AeadConfig.register()
            AndroidKeysetManager.Builder()
                .withSharedPref(context, keysetName, "musicverse_secure_keys_pref")
                .withKeyTemplate(KeyTemplates.get("AES128_GCM"))
                .withMasterKeyUri("android-keystore://musicverse_master_key")
                .build()
                .keysetHandle
                .getPrimitive(Aead::class.java)
        } catch (e: Exception) {
            Log.e("SecureDataStore", "Failed to initialize Tink Keystore. Data will be reset.", e)
            // 1. Clear corrupted preferences
            context.getSharedPreferences("musicverse_secure_keys_pref", Context.MODE_PRIVATE)
                .edit().clear().apply()
            
            // 2. Clear corrupted datastore file
            val datastoreFile = java.io.File(context.filesDir, "datastore/spotify_auth_secure.pb")
            if (datastoreFile.exists()) {
                datastoreFile.delete()
            }
            
            // 3. Try one more time after clearing
            try {
                AndroidKeysetManager.Builder()
                    .withSharedPref(context, keysetName, "musicverse_secure_keys_pref")
                    .withKeyTemplate(KeyTemplates.get("AES128_GCM"))
                    .withMasterKeyUri("android-keystore://musicverse_master_key")
                    .build()
                    .keysetHandle
                    .getPrimitive(Aead::class.java)
            } catch (e2: Exception) {
                Log.e("SecureDataStore", "Fatal Keystore failure.", e2)
                null // Return null to fallback to unencrypted or default values
            }
        }
    }

    override suspend fun readFrom(input: InputStream): String {
        return try {
            val encryptedBytes = input.readBytes()
            if (encryptedBytes.isEmpty()) return defaultValue
            
            // Decrypt the bytes using Tink
            if (aead == null) return defaultValue
            val decryptedBytes = aead!!.decrypt(encryptedBytes, null)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            defaultValue
        } catch (e: Exception) {
            e.printStackTrace()
            defaultValue
        }
    }

    override suspend fun writeTo(t: String, output: OutputStream) {
        withContext(Dispatchers.IO) {
            try {
                // Encrypt the string using Tink
                if (aead == null) return@withContext
                val encryptedBytes = aead!!.encrypt(t.toByteArray(Charsets.UTF_8), null)
                output.write(encryptedBytes)
            } catch (e: GeneralSecurityException) {
                e.printStackTrace()
            }
        }
    }
}
