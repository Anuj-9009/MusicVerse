package com.musicverse.player.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.musicverse.player.data.local.TrackDao
import com.musicverse.player.data.repository.VersionDiscoveryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/**
 * DiscoverySyncWorker — Offline-First Background Sync
 *
 * Queued when the user triggers a discovery scan while offline.
 * WorkManager guarantees execution once the device reconnects to the internet,
 * even if the app has been fully closed in the meantime.
 *
 * Result is persisted to Room; Kotlin Flow in the UI auto-updates
 * without any user action.
 */
@HiltWorker
class DiscoverySyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val versionDiscoveryRepository: VersionDiscoveryRepository,
    private val trackDao: TrackDao
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val KEY_TRACK_ID = "track_id"
        private const val WORK_NAME_PREFIX = "discovery_sync_"

        /**
         * Enqueue a discovery sync for a single track.
         * Requires internet — will wait automatically until reconnected.
         * Deduplicated by trackId so double-taps don't spawn duplicate jobs.
         */
        fun enqueue(context: Context, trackId: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<DiscoverySyncWorker>()
                .setConstraints(constraints)
                .setInputData(workDataOf(KEY_TRACK_ID to trackId))
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "$WORK_NAME_PREFIX$trackId",
                ExistingWorkPolicy.KEEP, // Don't replace if already queued
                workRequest
            )
        }

        /**
         * Enqueue a full-library discovery sync (runs per-track, sequentially).
         */
        fun enqueueFullLibrary(context: Context, trackIds: List<String>) {
            trackIds.forEach { enqueue(context, it) }
        }
    }

    override suspend fun doWork(): Result {
        val trackId = inputData.getString(KEY_TRACK_ID)
            ?: return Result.failure()

        return try {
            val track = trackDao.getTrackById(trackId)
                ?: return Result.failure()

            versionDiscoveryRepository.discoverVersionsForTrack(track)
            Result.success()
        } catch (e: Exception) {
            // Retry up to 3 times with exponential backoff (WorkManager default)
            Result.retry()
        }
    }
}
