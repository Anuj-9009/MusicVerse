package com.musicverse.player.data.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Alias for the Retrofit response model used in SponsorBlockApiService
@Serializable
data class SponsorSegment(
    val segment: List<Float> = emptyList(), // [startTimeSec, endTimeSec]
    val category: String = "",
    @SerialName("UUID") val uuid: String = "",
    val videoDuration: Float = 0f,
    val votes: Int = 0
) {
    val startSec: Float get() = segment.getOrElse(0) { 0f }
    val endSec: Float get() = segment.getOrElse(1) { 0f }
}
