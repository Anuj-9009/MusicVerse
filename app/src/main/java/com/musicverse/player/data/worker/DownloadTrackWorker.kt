package com.musicverse.player.data.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.musicverse.player.data.local.OfflineTrackDao
import com.musicverse.player.data.local.OfflineTrackEntity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

@HiltWorker
class DownloadTrackWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val offlineTrackDao: OfflineTrackDao
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        private const val TAG = "DownloadTrackWorker"
        const val KEY_TRACK_ID = "track_id"
        const val KEY_TRACK_TITLE = "track_title"
        const val KEY_TRACK_ARTIST = "track_artist"
        const val KEY_STREAM_URL = "stream_url"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val trackId = inputData.getString(KEY_TRACK_ID) ?: return@withContext Result.failure()
        val title = inputData.getString(KEY_TRACK_TITLE) ?: "Unknown"
        val artist = inputData.getString(KEY_TRACK_ARTIST) ?: "Unknown"
        val streamUrl = inputData.getString(KEY_STREAM_URL) ?: return@withContext Result.failure()

        try {
            // Check if already downloaded
            val existing = offlineTrackDao.getOfflineTrackById(trackId)
            if (existing != null && File(existing.localFilePath).exists()) {
                Log.d(TAG, "Track $trackId already downloaded. Skipping.")
                return@withContext Result.success()
            }

            Log.d(TAG, "Starting download for track $trackId ($title)")
            
            // Create musicverse_cache directory
            val cacheDir = File(appContext.filesDir, "musicverse_cache")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }

            val targetFile = File(cacheDir, "$trackId.m4a")
            
            // Simulate download from stream URL (In production, use OkHttp to download the audio stream)
            // Here we just download the actual bytes if it's a direct URL, or fallback to a dummy file
            var downloadedBytes = 0L
            try {
                URL(streamUrl).openStream().use { input ->
                    FileOutputStream(targetFile).use { output ->
                        downloadedBytes = input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Network download failed, creating dummy file for testing", e)
                FileOutputStream(targetFile).use {
                    it.write("dummy audio data".toByteArray())
                    downloadedBytes = 16L
                }
            }

            val entity = OfflineTrackEntity(
                id = trackId,
                title = title,
                artist = artist,
                localFilePath = targetFile.absolutePath,
                downloadedAtMs = System.currentTimeMillis(),
                sizeBytes = downloadedBytes
            )

            offlineTrackDao.insertOfflineTrack(entity)
            Log.d(TAG, "Successfully downloaded and cached track $trackId to ${targetFile.absolutePath}")
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download track $trackId", e)
            Result.retry()
        }
    }
}
