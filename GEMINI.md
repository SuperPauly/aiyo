# Aiyo

## Project Overview

**Aiyo** is an Android AI chat application built with Kotlin and Jetpack Compose. It allows users to chat with various AI models by integrating with OpenRouter, bringing their own API keys. The project follows a Clean Architecture pattern with MVVM.

### Key Technologies
*   **Language:** Kotlin
*   **UI Toolkit:** Jetpack Compose (Material3)
*   **Architecture:** MVVM + Clean Architecture (Multi-module)
*   **Dependency Injection:** Dagger Hilt
*   **Network:** OpenAI Kotlin Client (Ktor based)
*   **Local Storage:** Room Database, MMKV (for key-value storage)
*   **Build System:** Gradle (Kotlin DSL) with Version Catalogs (`libs.versions.toml`)

## Architecture & Modules

The project is structured into four main modules:

1.  **`:app`**: The main entry point. Connects all layers, handles dependency injection setup (`HiltAndroidApp`), and contains the main Activity (`MainActivity`).
    *   **Namespace:** `com.beradeep.aiyo`
    *   **Dependencies:** `:ui`, `:domain`, `:data`
2.  **`:domain`**: The core business logic layer. Contains models, repository interfaces, and use cases. It should have no dependencies on data or UI details.
    *   **Namespace:** `com.beradeep.aiyo.domain`
3.  **`:data`**: The implementation layer. Handles data sources (API, Database, Preferences). Implements repositories defined in `:domain`.
    *   **Namespace:** `com.beradeep.aiyo.data`
    *   **Dependencies:** `:domain`, Room, OpenAI Client, MMKV
4.  **`:ui`**: The presentation layer. Contains Compose screens, components, and ViewModels.
    *   **Namespace:** `com.beradeep.aiyo.ui` (Inferred)

## Building and Running

### Prerequisites
*   Android Studio
*   JDK 11+
*   Android SDK API 24+

### Commands

*   **Build Debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```
*   **Run Unit Tests:**
    ```bash
    ./gradlew test
    ```
*   **Run Lint Checks:**
    ```bash
    ./gradlew lint
    ```
*   **Format Code (Ktlint):**
    ```bash
    ./gradlew ktlintFormat
    ```
*   **Check Code Formatting:**
    ```bash
    ./gradlew ktlintCheck
    ```

## Development Conventions

*   **Code Style:** The project uses **Ktlint** for code formatting. This is strictly enforced via a pre-commit hook.
    *   **Action:** Always run `./gradlew ktlintFormat` before committing changes to ensure compliance.
*   **Pre-commit Hooks:** A pre-commit hook is installed that runs `ktlintCheck` and `lint`.
    *   If you encounter a "permission denied" error for the hook, ensure it is executable: `chmod +x .git/hooks/pre-commit` (or reinstall it via the Gradle task `installPreCommitHook`).
*   **Dependency Management:** All dependencies and versions are managed in `gradle/libs.versions.toml`. When adding new libraries, add them there first.
*   **Feature Branches:** Use feature branches for development (e.g., `feature/amazing-feature`) and submit Pull Requests.

## Project Structure Highlights

*   `app/src/main/java/com/beradeep/aiyo/AiyoApp.kt`: Hilt Application class.
*   `app/src/main/java/com/beradeep/aiyo/MainActivity.kt`: Main entry point, sets up the NavHost and Theme.
*   `scripts/git-hooks/pre-commit`: The script defining pre-commit checks.
*   `gradle/libs.versions.toml`: Central location for dependency versions.
