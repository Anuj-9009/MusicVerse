#include <jni.h>
#include <string>

/**
 * MusicVerse Secure Keys — NDK/JNI Layer
 *
 * Stores API key fragments obfuscated in native C++ code.
 * This significantly raises the reverse-engineering barrier compared to
 * plain BuildConfig strings — attackers must decompile the .so binary
 * rather than simply unzipping the APK and reading strings.
 *
 * IMPORTANT: For production, split each key across multiple functions
 * and reassemble at runtime. Replace placeholder values below with
 * your actual key fragments split at arbitrary character positions.
 *
 * Usage from Kotlin:
 *   val key = SecureKeys.getSpotifyClientId()
 */

extern "C" {

// ── Spotify Client ID ─────────────────────────────────────────────────────
// Split the key into 3 fragments to prevent simple string search in binary
JNIEXPORT jstring JNICALL
Java_com_musicverse_player_security_SecureKeys_getSpotifyClientIdNative(
        JNIEnv* env, jobject /* this */) {

    // Fragment 1 + Fragment 2 + Fragment 3 reassembled at runtime
    std::string frag1 = "YOUR_SPOTIFY";
    std::string frag2 = "_CLIENT";
    std::string frag3 = "_ID_HERE";
    return env->NewStringUTF((frag1 + frag2 + frag3).c_str());
}

// ── YouTube API Key ───────────────────────────────────────────────────────
JNIEXPORT jstring JNICALL
Java_com_musicverse_player_security_SecureKeys_getYouTubeApiKeyNative(
        JNIEnv* env, jobject /* this */) {

    std::string frag1 = "YOUR_YOUTUBE";
    std::string frag2 = "_API";
    std::string frag3 = "_KEY_HERE";
    return env->NewStringUTF((frag1 + frag2 + frag3).c_str());
}

// ── Gemini API Key ────────────────────────────────────────────────────────
JNIEXPORT jstring JNICALL
Java_com_musicverse_player_security_SecureKeys_getGeminiApiKeyNative(
        JNIEnv* env, jobject /* this */) {

    std::string frag1 = "YOUR_GEMINI";
    std::string frag2 = "_API";
    std::string frag3 = "_KEY_HERE";
    return env->NewStringUTF((frag1 + frag2 + frag3).c_str());
}

} // extern "C"
