<!-- Header Block -->
<div align="center">
  <br />
  <img src="assets/header-v2.svg" width="100%" alt="banner">
  <p>
    <img src="https://img.shields.io/badge/Status-In%20Development-orange" alt="Status In%20Development orange" /> <img src="https://img.shields.io/badge/Platform-Android-green" alt="Platform Android green" /> <img src="https://img.shields.io/badge/Min%20API-24-blue" alt="Min%20API 24 blue" /> <img src="https://img.shields.io/badge/Kotlin-2.0-purple" alt="Kotlin 2.0 purple" /> <img src="https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.02-teal" alt="Jetpack%20Compose BOM%202024.02 teal" />
  </p>
  <p>
    MusicVerse is a native Android application that reimagines music discovery by finding <b>alternate versions<b> (live performances, acoustic sessions, covers, remixes) of your favorite songs across Spotify and YouTube, powered by <b>Gemini AI<b> scoring.
  </p>
</div>

<hr style="border: 0; height: 1px; background-image: linear-gradient(to right, rgba(212, 168, 68, 0), rgba(212, 168, 68, 0.4), rgba(212, 168, 68, 0));" />

> ⚠️ **DEVELOPMENTAL BUILD** — This project is in active development and may not work properly. See [Known Issues](#-known-issues--workarounds) below for details.



---

## 📱 Screenshots & Design

The app follows a **"Warm Muted"** dark theme aesthetic designed in [Google Stitch](https://stitch.withgoogle.com/):

- **Login Screen** — Elegant serif branding, gold Spotify CTA, warm gradient
- **Home Screen** — Dynamic greeting, quick-action hero grid, filter pills
- **Now Playing** — Album art palette extraction, waveform progress bar, alternate versions
- **Discovery** — AI-powered version finding with real-time progress
- **Library (Vault)** — Searchable track collection with sort/filter
- **Profile** — Listening stats, audio preferences, integration management

---

## 🏗️ Architecture

```
Single-Activity (MainActivity.kt)
├── Jetpack Navigation (Login → 4-tab bottom nav)
│   ├── Home
│   ├── Discover
│   ├── Vault (Library)
│   └── Profile
├── Hilt Dependency Injection
├── Room Database (tracks, versions, offline cache)
├── Dual ExoPlayer Audio Engine (Primary + Ghost Buffer)
└── Retrofit + Kotlin Serialization (Spotify API, YouTube API)
```

### Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Dual ExoPlayer** | Primary player for current track, Ghost player pre-buffers the best alternate version for instant crossfade switching |
| **Gemini AI Scoring** | YouTube search results are scored by Gemini 1.5 Flash for audio quality, channel credibility, and version type classification |
| **ISRC Matching** | International Standard Recording Codes from Spotify are used for precise version identification |
| **Dark Mode Only** | Strict dark theme for audiophile aesthetic — no light mode variant by design |
| **Offline-First** | Room database caches all imported tracks and discovered versions for offline browsing |

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| **UI** | Jetpack Compose + Material3 + Custom Design System |
| **Navigation** | Jetpack Navigation Compose with shared element transitions |
| **DI** | Hilt (Dagger) |
| **Database** | Room with Flow-based reactive queries |
| **Audio** | Media3 ExoPlayer (dual-instance) + Android Equalizer API |
| **Networking** | Retrofit2 + OkHttp + Kotlin Serialization |
| **AI** | Google Generative AI SDK (Gemini 1.5 Flash) |
| **Image Loading** | Coil + Palette API for dynamic colors |
| **Auth** | Spotify OAuth 2.0 (PKCE flow) |
| **Ads** | SponsorBlock API integration for YouTube |
| **Typography** | Archivo Black, Bebas Neue, Playfair Display, Space Mono, Inter |

---

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17+
- Android SDK 34
- An Android device or emulator (API 24+)

### Setup

1. **Clone the repository:**
   ```bash
   git clone https://github.com/Anuj-9009/MusicVerse.git
   cd MusicVerse
   ```

2. **Create `local.properties`** in the project root:
   ```properties
   sdk.dir=/path/to/your/android/sdk

   # Required for Spotify import
   SPOTIFY_CLIENT_ID=your_spotify_client_id
   SPOTIFY_CLIENT_SECRET=your_spotify_client_secret

   # Optional — enhances AI discovery features
   YOUTUBE_API_KEY=your_youtube_api_key
   GEMINI_API_KEY=your_gemini_api_key

   # Optional — not yet implemented
   SUPABASE_URL=
   SUPABASE_ANON_KEY=
   REDIS_URL=
   ```

3. **Spotify Developer Setup:**
   - Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
   - Create an app
   - Set Redirect URI to: `musicverse://callback`
   - Copy Client ID and Client Secret to `local.properties`

4. **Build & Run:**
   ```bash
   ./gradlew assembleDebug
   # Or open in Android Studio and press Run
   ```

---

## 📂 Project Structure

```
app/src/main/java/com/musicverse/player/
├── audio/
│   ├── MusicVersePlayer.kt          # Dual ExoPlayer engine with crossfade
│   └── SponsorBlockForwardingPlayer.kt  # SponsorBlock segment skipping
├── data/
│   ├── api/
│   │   ├── SpotifyApiService.kt     # Spotify Web API (liked songs, search, playlists)
│   │   ├── YouTubeApiService.kt     # YouTube Data API v3
│   │   └── SponsorBlockApiService.kt
│   ├── local/
│   │   ├── TrackDao.kt              # Room DAO for imported tracks
│   │   ├── VersionDao.kt            # Room DAO for discovered versions
│   │   ├── OfflineTrackDao.kt       # Offline cache management
│   │   └── AppDatabase.kt
│   ├── models/
│   │   └── SpotifyModels.kt         # Spotify API response models
│   └── repository/
│       ├── SpotifyImportRepository.kt    # Paginated import + search
│       ├── VersionDiscoveryRepository.kt  # YouTube + Gemini AI pipeline
│       ├── AiVibeCheckRepository.kt       # Gemini scoring engine
│       └── YouTubeExtractor.kt
├── ui/
│   ├── theme/
│   │   ├── Color.kt                 # Warm Muted color palette
│   │   ├── Type.kt                  # Editorial typography system
│   │   ├── Theme.kt                 # Material3 dark theme
│   │   ├── Shape.kt                 # Rounded corners + spacing tokens
│   │   └── Motion.kt                # Spring physics animation presets
│   ├── components/
│   │   ├── BentoComponents.kt       # Reusable cards, pills, badges
│   │   ├── EqualizerBottomSheet.kt  # 5-band EQ controls
│   │   ├── FluidVisualizer.kt       # Audio visualizer
│   │   ├── ScrubBar.kt              # Seek bar with spring physics
│   │   └── VersionSwitcher.kt       # Source toggle (Spotify ↔ YouTube)
│   └── screens/
│       ├── login/LoginScreen.kt     # Connect with Spotify / YouTube
│       ├── home/HomeScreen.kt       # Dynamic greeting + hero grid
│       ├── import_/ImportScreen.kt  # Spotify import + search
│       ├── library/LibraryScreen.kt # Searchable track collection
│       ├── discovery/DiscoveryScreen.kt  # AI discovery phases
│       ├── player/PlayerScreen.kt   # Now Playing with waveform
│       ├── profile/ProfileScreen.kt # Stats + integrations
│       └── settings/SettingsScreen.kt    # BYOK API key panel
├── util/
│   └── SpotifyAuthManager.kt        # OAuth 2.0 token management
├── di/
│   ├── AppModule.kt                 # Hilt module (Room, Retrofit)
│   └── AudioModule.kt               # Hilt module (ExoPlayer instances)
└── MainActivity.kt                  # Single-Activity entry + navigation
```

---

## ⚠️ Known Issues & Workarounds

### 🔴 Critical Issues

| Issue | Details | Workaround |
|-------|---------|------------|
| **No Real Spotify Playback** | ExoPlayer cannot play `spotify:track:` URIs directly. Spotify's ToS requires using their SDK for audio streaming, which needs a Premium account. | We intercept Spotify URIs and substitute public domain MP3 files from SoundHelix. Each track gets a different demo MP3 based on title hash (16 variations). |
| **YouTube Streams Don't Work** | YouTube doesn't provide direct audio stream URLs through their API. The `youtubeVideoId` stored in versions is not a playable URL. | Ghost buffer pre-loads but actual YouTube playback requires a stream extraction service (like NewPipe Extractor) which is not yet integrated. |
| **API Keys Required** | Spotify search, YouTube discovery, and Gemini AI scoring all require valid API keys. Without them, those features silently fail. | Demo versions are auto-generated in `PlayerViewModel` when no real versions exist, so the UI is never empty. |

### 🟡 Moderate Issues

| Issue | Details | Status |
|-------|---------|--------|
| **OAuth Deep Link** | Spotify callback (`musicverse://callback`) may not trigger correctly on all Android versions. Requires app to be properly registered with the OS. | Implemented but untested on physical devices |
| **SponsorBlock** | The SponsorBlock integration polls every 500ms which could cause battery drain on long sessions. | Works for YouTube content only; no-ops when on Spotify source |
| **Search Debounce** | The Import screen search triggers API calls on every keystroke after 2 characters. No proper debounce delay. | Works but may hit Spotify rate limits on fast typing |
| **Crossfade Sync** | When switching sources, position sync between Primary and Ghost players may have ~100ms drift. | Acceptable for most listeners; could be improved with better clock sync |

### 🟢 Minor/Cosmetic Issues

| Issue | Details |
|-------|---------|
| **Profile Data** | Profile screen shows hardcoded demo data ("Julian Vance", "1,248 hours") — not connected to real user data |
| **Equalizer** | 5-band EQ is wired to Android's native Equalizer API but preset management isn't persisted across sessions |
| **Album Art Palette** | Dynamic color extraction occasionally produces muddy colors for monochrome album art |
| **Waveform Progress** | The waveform bars use pseudo-random heights, not actual audio waveform data |
| **Font Loading** | Custom fonts (Playfair Display, Archivo Black, etc.) are bundled as TTF, adding ~550KB to APK size |

---

## 🔧 Development Journey

### Phase 1: Project Scaffolding
- Set up single-activity Compose architecture with Hilt DI
- Configured Room database with Track + Version entities
- Built the initial "Industrial Minimalist" dark theme (Electric Blue + Charcoal)
- Implemented the Bento Grid layout system

### Phase 2: Core Audio Engine
- Built dual ExoPlayer architecture (Primary + Ghost)
- Implemented volume crossfade between Spotify and YouTube sources
- Added SponsorBlock integration for ad-free YouTube playback
- Created 5-band equalizer with session-aware binding

### Phase 3: Spotify Integration
- Implemented OAuth 2.0 PKCE flow with deep-link callback
- Built paginated import for Liked Songs (50 per page)
- Added full playlist iteration (all user playlists)
- Implemented Spotify catalog search with result import

### Phase 4: AI Discovery Pipeline
- Integrated YouTube Data API v3 for version searching
- Built Gemini 1.5 Flash scoring prompt (type classification + quality scoring)
- Added fallback scoring when Gemini API is unavailable
- Created auto-generation of demo versions for UI testing

### Phase 5: UI Redesign ("Warm Muted" Theme)
- **Major pivot**: Replaced cold Electric Blue cyber aesthetic with warm amber/charcoal palette
- Designed in Google Stitch tool, then translated to Compose
- Created Login screen with elegant serif branding
- Rebuilt Now Playing with waveform progress bar
- Added Profile screen with stats and integration management
- Restructured navigation: Login → 4-tab (Home, Discover, Vault, Profile)

### Phase 6: Polish & Integration
- Wired skip prev/next controls to queue management
- Added real-time progress tracking (250ms polling)
- Improved audio variety (16 different demo tracks)
- Backward-compatible color aliases so existing screens auto-adopt new palette

---

## 🎨 Design System

### Color Palette

| Token | Hex | Usage |
|-------|-----|-------|
| Amber (Primary) | `#D4A844` | CTAs, selected states, highlights |
| Sunset Orange | `#E86833` | Play button, active tags, nav indicator |
| Teal | `#4DB6AC` | YouTube Music accent, secondary actions |
| Deep Charcoal | `#1A1A1A` | Primary background |
| Off-White | `#E8E4E0` | Body text |
| Warm Surface | `#1E1C1A` → `#363230` | 4-level elevation system |

### Typography

| Role | Font | Usage |
|------|------|-------|
| Display / Headlines | Archivo Black | Section titles, hero text |
| Condensed Headers | Bebas Neue | Stat values, counter text |
| Serif Branding | Playfair Display | "MusicVerse" logo, greeting |
| Monospace | Space Mono | Timestamps, labels, badges |
| Body / UI | Inter | All body text, buttons, inputs |

### Animation System

All interactive elements use **physics-based spring animations** (no linear tweens):

| Preset | Damping | Stiffness | Use Case |
|--------|---------|-----------|----------|
| Press | MediumBouncy | High | Button taps, card presses |
| Entrance | LowBouncy | VeryLow | Staggered item appearance |
| Transition | NoBouncy | Low | Page enter/exit |
| Scrub | LowBouncy | Medium | Seek bar thumb |

---

## 📋 API Dependencies

| Service | Required? | Free Tier? | Purpose |
|---------|-----------|-----------|---------|
| **Spotify Web API** | Yes (for import) | Yes (with developer account) | Import liked songs, search catalog, user profile |
| **YouTube Data API v3** | Optional | Yes (10,000 units/day) | Search for alternate versions |
| **Gemini AI** | Optional | Yes (free tier available) | AI scoring of version quality |
| **SponsorBlock** | Automatic | Yes (community API) | Skip sponsor segments in YouTube |

---

## 🤝 Contributing

This is a student project in active development. If you'd like to contribute:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is for educational purposes. All third-party APIs (Spotify, YouTube, Gemini) are subject to their respective Terms of Service.

---

## 🙏 Acknowledgments

- **Google Stitch** — UI design prototyping
- **SoundHelix** — Public domain demo audio tracks
- **SponsorBlock** — Community-driven ad-segment database
- **Jetpack Compose** — Modern Android UI toolkit
- **Gemini AI** — Intelligent version scoring

---

<div align="center" style="margin-top: 40px;">
  <img src="assets/footer-v2.svg" width="100%" alt="footer">
</div>
<p style="font-family: 'Sora', sans-serif; font-size: 13px; font-weight: 600; color: #D4A844; margin: 0; text-align: center;">
  built by ANUJ with ❤️ to the acoustic warmth of lauryn hill's 'Ex-Factor'
</p>
