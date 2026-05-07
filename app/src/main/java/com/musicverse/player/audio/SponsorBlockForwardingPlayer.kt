package com.musicverse.player.audio

import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ForwardingPlayer
import com.musicverse.player.data.api.SponsorSegment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * A wrapper around ExoPlayer that automatically skips SponsorBlock segments.
 * This ensures skipping happens at the engine level, independent of the UI.
 */
class SponsorBlockForwardingPlayer(
    player: ExoPlayer,
    private val scope: CoroutineScope
) : ForwardingPlayer(player) {

    private var segments: List<SponsorSegment> = emptyList()
    private var monitorJob: Job? = null

    init {
        addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    startMonitoring()
                } else {
                    stopMonitoring()
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                // If user seeks manually into a segment, we still skip it
                checkAndSkip()
            }
        })
    }

    fun setSkipSegments(newSegments: List<SponsorSegment>) {
        segments = newSegments.sortedBy { it.segment.first() }
        checkAndSkip()
    }

    private fun startMonitoring() {
        stopMonitoring()
        monitorJob = scope.launch(Dispatchers.Main) {
            while (isActive) {
                checkAndSkip()
                delay(500) // Check every 500ms
            }
        }
    }

    private fun stopMonitoring() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private fun checkAndSkip() {
        if (segments.isEmpty()) return
        
        val currentPosition = currentPosition / 1000f // Convert to seconds
        
        for (segment in segments) {
            val start = segment.segment[0]
            val end = segment.segment[1]
            
            if (currentPosition in start..end) {
                // We are inside a skip segment! Jump to the end + 100ms buffer
                val targetPositionMs = (end * 1000).toLong() + 100
                seekTo(targetPositionMs)
                break
            }
        }
    }
}
