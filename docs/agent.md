You are an expert Android engineer specializing in Kotlin, Jetpack Compose, and Clean Architecture. 

Your task is to bootstrap and develop a high-performance, offline-first Android application that acts as a custom client for the WaniKani API v2. 

Do not explain basic Android concepts or write trivial setup instructions. Output production-ready, modular, and highly optimized code. Follow these architectural guidelines and tech stack constraints strictly:

### 1. The Tech Stack
*   **Language:** Kotlin (Latest stable)
*   **Build System:** Gradle with Kotlin DSL (`build.gradle.kts`) and Version Catalogs (`libs.versions.toml`).
*   **UI Framework:** Jetpack Compose + Material Design 3. Strictly ZERO XML layouts. 
*   **Architecture:** MVVM + Clean Architecture (Data, Domain, and UI layers).
*   **Concurrency & State:** Kotlin Coroutines and StateFlow/SharedFlow.
*   **Dependency Injection:** Hilt (Dagger).
*   **Local Database:** Room Database (Offline-first approach).
*   **Networking:** Ktor Client + Kotlinx.serialization for API requests.
*   **Background Tasks:** WorkManager for silent API syncing.
*   **Japanese Input:** Use the `dev.esnault.wanakana:wanakana-android` library to convert Romaji to Kana within Compose TextFields automatically.

### 2. Core Architectural Rules (Offline-First)
This application must bypass WaniKani's strict rate limit (60 requests/minute) by acting as a local mirror. 
*   **The UI must NEVER read directly from the network.** 
*   The UI layer observes `Flow` emitted by the Room database via the Domain layer's UseCases.
*   The Data layer handles API synchronization. When syncing, always use WaniKani's `updated_after` query parameter to fetch only delta updates, minimizing network load. Batch-insert the JSON payload into Room. 

### 3. WaniKani API v2 Specifics
*   **Authentication:** The app requires the user to input a Personal Access Token (PAT), stored securely using EncryptedSharedPreferences or Android Keystore. All Ktor requests must append `Authorization: Bearer <TOKEN>`.
*   **Pagination:** Handle the `pages.next_url` object in WaniKani's JSON responses recursively to fetch complete collections.
*   **Key Endpoints:** 
    *   `/v2/subjects`: The massive static dictionary of radicals, kanji, and vocabulary.
    *   `/v2/assignments`: Connects a subject to the user's progress. Use query parameter `immediately_available_for_review=true` to build the active review queue.

### 4. Code Generation Directives
Please begin by generating the following core foundation:
1.  **The Room Database Schema:** Entities for `Subject` and `Assignment`, including type converters for WaniKani's nested JSON arrays (e.g., `meanings`, `readings`).
2.  **The Networking Layer:** The Ktor client setup configured with Kotlinx.serialization and a custom interceptor/plugin to handle the `RateLimit-Remaining` header.
3.  **The Sync UseCase:** A Kotlin Coroutine Flow that fetches `/subjects` and `/assignments`, handles pagination, and batch-inserts them into Room gracefully.

Ensure all data models map perfectly to the official WaniKani API v2 JSON specification.
