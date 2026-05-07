package com.musicverse.player.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.musicverse.player.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONObject

data class VibeCheckState(
    val score: Int = 0,
    val reason: String = "Analyzing...",
    val isComplete: Boolean = false,
    val isVerified: Boolean = false,
    val error: String? = null
)

/**
 * AiVibeCheckRepository
 * 
 * Uses Gemini 1.5 Flash to analyze YouTube comments for a specific audio track
 * to determine its actual audio quality and "vibe". Filters out fake live versions
 * or bad crowd recordings.
 */
class AiVibeCheckRepository {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        systemInstruction = content { 
            text("You are an expert audio engineer and music critic. Analyze the provided YouTube comments for an audio track. Rate the actual audio quality and performance 'vibe' from 1 to 100. If the comments mention bad audio, fake live tracks, or excessive crowd noise, lower the score significantly. Return ONLY a valid JSON object in this format: { \"score\": 85, \"reason\": \"One short sentence explaining the score.\" }") 
        }
    )

    /**
     * Streams the Gemini response to avoid UI blocking.
     * Parses the JSON chunks as they arrive.
     */
    fun performVibeCheckStream(videoId: String, comments: List<String>): Flow<VibeCheckState> {
        val prompt = "Analyze these comments for video $videoId:\n" + comments.joinToString("\n") { "- $it" }

        return generativeModel.generateContentStream(prompt)
            .map { response ->
                val text = response.text ?: ""
                try {
                    // Gemini might wrap JSON in markdown blocks
                    val cleanJson = text.replace("```json", "").replace("```", "").trim()
                    if (cleanJson.startsWith("{") && cleanJson.endsWith("}")) {
                        val json = JSONObject(cleanJson)
                        val score = json.optInt("score", 0)
                        VibeCheckState(
                            score = score,
                            reason = json.optString("reason", ""),
                            isComplete = true,
                            isVerified = score >= 80
                        )
                    } else {
                        // Intermediate streaming state
                        VibeCheckState(reason = "Thinking...", isComplete = false)
                    }
                } catch (e: Exception) {
                    VibeCheckState(reason = "Parsing...", isComplete = false)
                }
            }
            .catch { e ->
                emit(VibeCheckState(error = "AI Vibe Check failed: ${e.message}", isComplete = true))
            }
    }

    /**
     * Simulates fetching top 50 comments from YouTube API.
     */
    suspend fun fetchComments(videoId: String): List<String> {
        // In a real scenario, this hits the YouTube Data API commentThreads endpoint.
        return listOf(
            "The audio quality on this live version is insane!",
            "You can really hear the bass perfectly.",
            "Wow, the crowd is a bit loud at the start, but the mix is great.",
            "Best acoustic version of this song by far.",
            "I love how clear the vocals are."
        )
    }
}
