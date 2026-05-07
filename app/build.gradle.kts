import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization")
    kotlin("kapt")
}

// Load local.properties for API keys
val localProperties = Properties()
rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use {
    localProperties.load(it)
}

android {
    namespace = "com.musicverse.player"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.musicverse.player"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // API keys (loaded from local.properties)
        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"${localProperties.getProperty("SPOTIFY_CLIENT_ID") ?: ""}\"")
        buildConfigField("String", "SPOTIFY_CLIENT_SECRET", "\"${localProperties.getProperty("SPOTIFY_CLIENT_SECRET") ?: ""}\"")
        buildConfigField("String", "YOUTUBE_API_KEY", "\"${localProperties.getProperty("YOUTUBE_API_KEY") ?: ""}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY") ?: ""}\"")
        buildConfigField("String", "SUPABASE_URL", "\"${localProperties.getProperty("SUPABASE_URL") ?: ""}\"")
        buildConfigField("String", "SUPABASE_ANON_KEY", "\"${localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""}\"")
        buildConfigField("String", "REDIS_URL", "\"${localProperties.getProperty("REDIS_URL") ?: ""}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isDebuggable = true
            applicationIdSuffix = ".debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    // NDK/CMake disabled — requires NDK to be installed
    // externalNativeBuild {
    //     cmake {
    //         path = file("CMakeLists.txt")
    //         version = "3.22.1"
    //     }
    // }
}

dependencies {
    // ── Core Android ──
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // ── Jetpack Compose BOM ──
    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.foundation:foundation")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // ── Navigation ──
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // ── Media3 (ExoPlayer + MediaSession) ──
    implementation("androidx.media3:media3-exoplayer:1.5.1")
    implementation("androidx.media3:media3-session:1.5.1")
    implementation("androidx.media3:media3-ui:1.5.1")
    implementation("androidx.media3:media3-common:1.5.1")
    implementation("androidx.media3:media3-exoplayer-hls:1.5.1")
    implementation("androidx.media3:media3-exoplayer-dash:1.5.1")

    // ── Hilt (Dependency Injection) ──
    implementation("com.google.dagger:hilt-android:2.53.1")
    kapt("com.google.dagger:hilt-android-compiler:2.53.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // ── Networking ──
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-kotlinx-serialization:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")

    // ── Image Loading ──
    implementation("io.coil-kt:coil-compose:2.7.0")

    // ── Room (Local Database) ──
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    // ── DataStore (Preferences) ──
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // ── Coroutines ──
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // ── Google Fonts for Compose ──
    implementation("androidx.compose.ui:ui-text-google-fonts:1.7.6")

    // ── Accompanist (System UI, Permissions) ──
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.36.0")
    implementation("com.google.accompanist:accompanist-permissions:0.36.0")

    // ── Palette (extract colors from album art) ──
    implementation("androidx.palette:palette-ktx:1.0.0")

    // ── Google Generative AI (Gemini) ──
    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    // ── WorkManager (Offline-First Background Sync) ──
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.hilt:hilt-work:1.2.0")
    kapt("androidx.hilt:hilt-compiler:1.2.0")

    // ── Security & Tink AEAD ──
    implementation("com.google.crypto.tink:tink-android:1.8.0")

    // ── Testing ──
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}

kapt {
    correctErrorTypes = true
}
