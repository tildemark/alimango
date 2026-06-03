# Alimango 🦀

Alimango is a modern, high-performance, offline-first Android client for [WaniKani API v2](https://docs.api.wanikani.com/20170710/). It is designed to mirror all necessary data locally using Android's Room database and background synchronizations via WorkManager, providing a seamless user experience that works offline.

The app uses a dark-first, Material 3 Jetpack Compose layout styled with WaniKani's signature brand colors.

---

## 🚀 Key Features

*   **Offline-First Architecture**: All UI data flows reactively from Room Database via Kotlin Flows. The user interface never talks directly to the network.
*   **Active Reviews**: A dedicated review session interface featuring:
    *   **Wanakana Integration**: Automatic Romaji-to-Kana conversion for vocabulary/kanji reading questions.
    *   **Audio Playback**: Real-time pronunciation audio streaming for vocabulary items.
*   **Delta Sync**: Background synchronizations using the `/v2/subjects` and `/v2/assignments` delta sync headers (`updated_after`) to optimize network usage.
*   **Secure Storage**: Personal Access Tokens (PAT) are stored securely on-device using AndroidX `EncryptedSharedPreferences`.
*   **WaniKani Dashboard**: Direct summaries of your current Level, pending Reviews, pending Lessons, and active SRS stage counts.

---

## 🛠️ Tech Stack

| Layer | Library | Purpose |
|---|---|---|
| **UI** | Jetpack Compose / Material 3 | Fully declarative, native UI components |
| **DI** | Dagger Hilt | Compile-time dependency injection |
| **Database** | Room 2.6+ | Local SQLite database mapper |
| **Networking** | Ktor Client 3.x (Android Engine) | Safe, asynchronous HTTP client |
| **Serialization** | Kotlinx.serialization | Compile-time polymorphic JSON parser |
| **Background Sync**| WorkManager | Schedule periodic network sync |
| **IME Input** | Wanakana Android | Direct Japanese Romaji-to-Kana conversion |

---

## 📂 Project Structure

```
alimango/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   └── kotlin/com/tildemark/alimango/
│   │       ├── AlimangoApp.kt            # Application configuration & Hilt worker factory
│   │       ├── MainActivity.kt           # Main launcher activity hosting Compose NavHost
│   │       ├── data/                     # Local Room DB, Ktor network API, repositories & sync worker
│   │       ├── domain/                   # Pure Kotlin use cases & models
│   │       └── ui/                       # Jetpack Compose layouts (Theme, NavGraph, Screens)
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── libs.versions.toml                # Version catalog for unified dependencies
├── build.gradle.kts
├── gradle.properties
└── settings.gradle.kts
```

---

## ⚙️ Setup and Installation

### 1. Prerequisite Configuration
To securely inject your WaniKani token during compilation, open your `local.properties` file at the root of the project and add your PAT (Personal Access Token):

```properties
wanikani.pat=your_wanikani_token_here
```

*Note: `local.properties` is configured in `.gitignore` and is never committed to source control.*

### 2. Build the Debug APK
Build the debug version of the application using the Gradle wrapper command:

```bash
# On Windows
.\gradlew.bat assembleDebug

# On macOS / Linux
./gradlew assembleDebug
```

The compiled APK will be located under `app/build/outputs/apk/debug/app-debug.apk`.
