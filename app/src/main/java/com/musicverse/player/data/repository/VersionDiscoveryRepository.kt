package com.musicverse.player.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.musicverse.player.BuildConfig
import com.musicverse.player.data.api.YouTubeApiService
import com.musicverse.player.data.local.TrackEntity
import com.musicverse.player.data.local.VersionDao
import com.musicverse.player.data.local.VersionEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Version Discovery Repository — The AI brain of the app.
 *
 * Pipeline for each track:
 *   1. Build targeted YouTube search queries for each version type
 *   2. Fetch YouTube results via Data API v3
 *   3. Get video details (duration, view count)
 *   4. Send batch to Gemini AI for vibe scoring + classification
 *   5. Cache scored versions in Room
 *
 * The Gemini prompt is carefully engineered to:
 *   - Classify version type (live, acoustic, cover, remix)
 *   - Score audio quality on a 1-100 scale
 *   - Assign an audio badge (Pristine, Crowd Singalong, etc.)
 *   - Provide a one-line reason for the score
 */
@Singleton
class VersionDiscoveryRepository @Inject constructor(
    private val youTubeApi: YouTubeApiService,
    private val versionDao: VersionDao
) {

    private val youtubeApiKey = BuildConfig.YOUTUBE_API_KEY
    private val geminiApiKey = BuildConfig.GEMINI_API_KEY

    /**
     * Gemini generative model — Gemini 1.5 Flash for speed.
     */
    private val geminiModel by lazy {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = geminiApiKey,
            generationConfig = generationConfig {
                temperature = 0.3f       // Low temp for consistent scoring
                topP = 0.8f
                maxOutputTokens = 2048
            }
        )
    }

    /**
     * Version types to search for.
     */
    private val searchVariants = listOf(
        VersionSearchType("live", "live concert performance"),
        VersionSearchType("acoustic", "acoustic session unplugged"),
        VersionSearchType("cover", "cover version"),
        VersionSearchType("remix", "remix")
    )

    data class VersionSearchType(val type: String, val queryModifier: String)

    /**
     * Discover alternate versions for a single track.
     *
     * @param track The track to find versions for
     * @param onProgress Called with (discovered, total) counts
     * @return Number of versions discovered
     */
    suspend fun discoverVersionsForTrack(
        track: TrackEntity,
        onProgress: (Int) -> Unit = {}
    ): Int = withContext(Dispatchers.IO) {
        val allCandidates = mutableListOf<YouTubeCandidate>()
        var discovered = 0

        // Phase 1: Search YouTube for each version type
        for (variant in searchVariants) {
            try {
                val query = "${track.artist} ${track.title} ${variant.queryModifier}"
                val searchResult = youTubeApi.searchVideos(
                    query = query,
                    apiKey = youtubeApiKey,
                    maxResults = 5
                )

                val videoIds = searchResult.items
                    .mapNotNull { it.id?.videoId }
                    .joinToString(",")

                if (videoIds.isNotEmpty()) {
                    // Get video details (duration, views)
                    val details = youTubeApi.getVideoDetails(
                        ids = videoIds,
                        apiKey = youtubeApiKey
                    )

                    details.items.forEach { video ->
                        allCandidates.add(
                            YouTubeCandidate(
                                videoId = video.id,
                                title = video.snippet?.title ?: "",
                                channelName = video.snippet?.channelTitle ?: "",
                                thumbnailUrl = video.snippet?.thumbnails?.high?.url
                                    ?: video.snippet?.thumbnails?.medium?.url,
                                durationIso = video.contentDetails?.duration ?: "",
                                viewCount = video.statistics?.viewCount?.toLongOrNull() ?: 0,
                                suggestedType = variant.type
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Continue with other variants even if one fails
                e.printStackTrace()
            }
        }

        if (allCandidates.isEmpty()) return@withContext 0

        // Phase 2: Send candidates to Gemini for AI scoring
        try {
            val scoredVersions = scoreWithGemini(track, allCandidates)

            // Phase 3: Cache in Room
            versionDao.deleteVersionsForTrack(track.id)
            versionDao.insertVersions(scoredVersions)
            discovered = scoredVersions.size
            onProgress(discovered)
        } catch (e: Exception) {
            // Fallback: Save candidates with default scores if Gemini fails
            val fallbackVersions = allCandidates.map { candidate ->
                VersionEntity(
                    id = candidate.videoId,
                    trackId = track.id,
                    type = candidate.suggestedType,
                    title = candidate.title,
                    channelName = candidate.channelName,
                    youtubeVideoId = candidate.videoId,
                    thumbnailUrl = candidate.thumbnailUrl,
                    durationMs = parseIsoDuration(candidate.durationIso),
                    viewCount = candidate.viewCount,
                    aiVibeScore = 50,
                    aiVibeReason = "AI scoring unavailable — ranked by views",
                    audioBadge = candidate.suggestedType
                )
            }
            versionDao.deleteVersionsForTrack(track.id)
            versionDao.insertVersions(fallbackVersions)
            discovered = fallbackVersions.size
            onProgress(discovered)
        }

        return@withContext discovered
    }

    /**
     * Discover versions for all tracks without existing versions.
     *
     * @param tracks List of tracks to process
     * @param onProgress Called with (processedTracks, totalTracks, totalVersionsFound)
     */
    suspend fun discoverVersionsForAll(
        tracks: List<TrackEntity>,
        onProgress: (processedTracks: Int, totalTracks: Int, totalVersions: Int) -> Unit = { _, _, _ -> }
    ) = withContext(Dispatchers.IO) {
        var totalVersions = 0
        tracks.forEachIndexed { index, track ->
            // Skip tracks that already have versions
            if (!versionDao.hasVersionsForTrack(track.id)) {
                val count = discoverVersionsForTrack(track)
                totalVersions += count
            }
            onProgress(index + 1, tracks.size, totalVersions)
        }
    }

    /**
     * Send YouTube candidates to Gemini for intelligent scoring.
     *
     * The prompt is carefully engineered to produce structured JSON output
     * with version type, vibe score, reason, and audio badge.
     */
    private suspend fun scoreWithGemini(
        track: TrackEntity,
        candidates: List<YouTubeCandidate>
    ): List<VersionEntity> {
        val candidateList = candidates.mapIndexed { i, c ->
            "[$i] \"${c.title}\" by ${c.channelName} (${c.viewCount} views, duration: ${c.durationIso})"
        }.joinToString("\n")

        val prompt = """
            You are an expert music curator and audiophile. Analyze these YouTube video candidates 
            as alternate versions of the song "${track.title}" by "${track.artist}" from album "${track.album}".

            CANDIDATES:
            $candidateList

            For EACH candidate, determine:
            1. **type**: The version type — one of: "live", "acoustic", "cover", "remix", or "studio" (if it's just a reupload)
            2. **vibeScore**: Audio quality score from 1-100 based on:
               - Channel credibility (official channels score higher)
               - View count (social proof of quality)
               - Title clarity (professional naming convention)
               - Expected audio fidelity based on the title/channel
            3. **vibeReason**: One concise sentence explaining WHY this score
            4. **audioBadge**: One of: "pristine", "lossless", "live", "crowd", "acoustic", "remaster"
            5. **keep**: true if this is a genuine alternate version, false if it's a reaction, tutorial, lyrics video, or unrelated

            IMPORTANT RULES:
            - Lyrics videos, reaction videos, and tutorials should get vibeScore < 20 and keep = false
            - Official live performances from verified channels should score 80+
            - Fan recordings with crowd noise should get badge "crowd" and score 40-60
            - Professional acoustic sessions (Tiny Desk, COLORS, etc.) should score 85+
            - Remove exact duplicates of the studio version

            Respond ONLY with a JSON array (no markdown, no code fences):
            [{"index": 0, "type": "live", "vibeScore": 85, "vibeReason": "Official live at Glastonbury with pristine board mix", "audioBadge": "pristine", "keep": true}, ...]
        """.trimIndent()

        val response = geminiModel.generateContent(prompt)
        val responseText = response.text ?: return emptyList()

        // Parse the JSON response
        return parseGeminiResponse(responseText, track.id, candidates)
    }

    /**
     * Parse Gemini's JSON response into VersionEntity objects.
     */
    private fun parseGeminiResponse(
        jsonText: String,
        trackId: String,
        candidates: List<YouTubeCandidate>
    ): List<VersionEntity> {
        // Clean up potential markdown formatting
        val cleanJson = jsonText
            .replace("```json", "")
            .replace("```", "")
            .trim()

        return try {
            val results = kotlinx.serialization.json.Json {
                ignoreUnknownKeys = true
                isLenient = true
            }.decodeFromString<List<GeminiVersionResult>>(cleanJson)

            results.filter { it.keep }.mapNotNull { result ->
                val candidate = candidates.getOrNull(result.index) ?: return@mapNotNull null
                VersionEntity(
                    id = candidate.videoId,
                    trackId = trackId,
                    type = result.type,
                    title = candidate.title,
                    channelName = candidate.channelName,
                    youtubeVideoId = candidate.videoId,
                    thumbnailUrl = candidate.thumbnailUrl,
                    durationMs = parseIsoDuration(candidate.durationIso),
                    viewCount = candidate.viewCount,
                    aiVibeScore = result.vibeScore.coerceIn(1, 100),
                    aiVibeReason = result.vibeReason,
                    audioBadge = result.audioBadge
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // ── Observables ──

    fun getVersionsForTrack(trackId: String): Flow<List<VersionEntity>> =
        versionDao.getVersionsForTrack(trackId)

    fun getTotalVersionCount(): Flow<Int> =
        versionDao.getTotalVersionCount()

    fun getTopVersions(limit: Int = 20): Flow<List<VersionEntity>> =
        versionDao.getTopVersions(limit)

    // ── Utility ──

    /**
     * Parse ISO 8601 duration (e.g., "PT4M33S") to milliseconds.
     */
    private fun parseIsoDuration(iso: String): Long {
        val regex = Regex("PT(?:(\\d+)H)?(?:(\\d+)M)?(?:(\\d+)S)?")
        val match = regex.find(iso) ?: return 0
        val hours = match.groupValues[1].toLongOrNull() ?: 0
        val minutes = match.groupValues[2].toLongOrNull() ?: 0
        val seconds = match.groupValues[3].toLongOrNull() ?: 0
        return (hours * 3600 + minutes * 60 + seconds) * 1000
    }

    /**
     * Internal candidate model before AI scoring.
     */
    data class YouTubeCandidate(
        val videoId: String,
        val title: String,
        val channelName: String,
        val thumbnailUrl: String?,
        val durationIso: String,
        val viewCount: Long,
        val suggestedType: String
    )
}

/**
 * Gemini AI response model for version scoring.
 */
@kotlinx.serialization.Serializable
data class GeminiVersionResult(
    val index: Int = 0,
    val type: String = "live",
    val vibeScore: Int = 50,
    val vibeReason: String = "",
    val audioBadge: String = "live",
    val keep: Boolean = true
)
