# Jury Simulator Project

## Project Overview

**JurySimulator** is an Android application built with Jetpack Compose that simulates a criminal court trial. The user acts as a juror, participating in Voir Dire (jury selection), witnessing the trial, and engaging in deliberation with AI-controlled jurors.

The application leverages a local LLM (Large Language Model) via **Ollama** to generate dynamic case details, dialogue, witness testimony, and juror personalities. This creates realistic and varied trial experiences for educational or training purposes.

## Tech Stack

*   **Language:** Kotlin
*   **UI Framework:** Jetpack Compose (Material3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Dependency Injection:** Manual (handled in `NavGraph.kt`)
*   **Local Data:** Room Database (for Case History), DataStore Preferences
*   **Networking:** Retrofit + Moshi + OkHttp
*   **AI Integration:** Ollama API (connecting to local instance)
*   **Build System:** Gradle with Kotlin DSL

## Architecture & Key Components

### 1. UI Layer (`com.charles.jurysim.ui`)
*   **Navigation:** Single Activity (`MainActivity`) using `JurySimNavGraph` for Compose Navigation.
*   **Screens:**
    *   `Home`: Main entry point with options for new cases, history, settings, and profile
    *   `Setup/ModelSelection`: Configures the connection to the Ollama server and selects the model
    *   `VoirDire`: Chat interface where the "Judge" (AI) interviews the user
    *   `Trial`: Displays opening statements, witness testimony, and evidence
    *   `Deliberation`: Complex screen where the user discusses the case with 11 AI jurors. Features group chat and individual private chats
    *   `Verdict`: Displays the final outcome
*   **ViewModels:** Manage state and business logic. The central controller is `SimulationViewModel`.
*   **Components:**
    *   `NotebookScreen`: A reusable component/overlay for viewing auto-collected facts (Evidence, Witnesses) and user notes

### 2. Data Layer (`com.charles.jurysim.data`)
*   **Repositories:**
    *   `OllamaRepository`: Handles API calls to the local Ollama instance (`/api/generate`, `/api/tags`).
    *   `CaseHistoryRepository`: Saves finished trials to a local Room database.
    *   `PreferencesRepository`: Manages user settings (DataStore).
*   **Models:**
    *   `SimulationState`: The single source of truth for the active simulation (current phase, messages, verdict, facts, etc.).
    *   `AIJuror`: Represents an AI juror with personality, occupation, age, hidden bias, and voting leaning.
    *   `Fact`: Represents a piece of information (Evidence/Person) auto-extracted from the trial.
    *   `TrialPhase`: Enum defining the state machine (SETUP -> VOIR_DIRE -> TRIAL -> DELIBERATION -> VERDICT).

### 3. Simulation Logic (`com.charles.jurysim.util`)
*   **PromptTemplates.kt:** Contains the engineered prompts sent to the LLM. This is the "brain" of the simulation, defining how the AI should act as Judge, Prosecutor, Defense, or individual Jurors.

## Trial Flow

The application follows this sequence:
```
Home → Setup (server URL) → ModelSelection → Intro (case generated)
  → VoirDire (jury selection) → [JurySelected | JuryDismissed]
  → Trial (opening → witnesses → evidence → closing)
  → Deliberation (voting rounds, individual juror chats)
  → Verdict (GUILTY | NOT GUILTY | MISTRIAL)
```

## AI Juror System

Each of 11 AI jurors has:
- `name`, `age`, `occupation`, `personality`
- `hiddenBias` - influences voting subtly without explicit mention
- `initialLeaning` - LEANING_GUILTY, LEANING_NOT_GUILTY, or UNDECIDED
- `avatarId` - for UI display

Jurors are generated via `PromptTemplates.generateAIJurors()` with regex parsing in `SimulationViewModel.parseJurors()`. Fallback to `createDefaultJurors()` if parsing fails.

## Voting System

- Unanimous verdict required (all 12 jurors including user)
- Maximum 5 voting rounds before mistrial declared
- Between rounds, users can chat with individual jurors or group
- `VoteChoice` enum: GUILTY, NOT_GUILTY

## Build & Run Instructions

The project uses Gradle. From the project root directory:

*   **Generate gradle wrapper (if missing):**
    ```bash
    gradle wrapper
    ```

*   **Build debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```

*   **Build and install on connected device:**
    ```bash
    ./gradlew installDebug
    ```

*   **Clean build:**
    ```bash
    ./gradlew clean
    ```

*   **Run tests:**
    ```bash
    ./gradlew test
    ```

*   **Check connected devices:**
    ```bash
    adb devices
    ```

**Prerequisites:**
- Android SDK with minimum API level 26
- Access to a running Ollama instance (locally or accessible via network)
- Android Studio or compatible IDE with Kotlin support

## Network Configuration

The application allows cleartext HTTP traffic for local Ollama servers:
- localhost, 10.0.2.2 (emulator), 192.168.*.* (LAN)
- Configured in `res/xml/network_security_config.xml`

## Development Conventions

- **State Management**: `SimulationViewModel` uses `MutableStateFlow<SimulationState>` as single source of truth for entire trial flow
- **Navigation**: Sealed class `Screen` routes in `NavGraph.kt` with manual ViewModel instantiation (no DI framework)
- **AI Integration**: `PromptTemplates.kt` contains all LLM prompts; `OllamaRepository` handles API calls with 300s timeouts
- **Data Persistence**: Room for case history, DataStore for preferences/settings

## Key Files for Common Tasks

| Task | Files |
|------|-------|
| Add new trial phase | `TrialPhase.kt`, `SimulationViewModel.kt`, `TrialScreen.kt` |
| Modify AI prompts | `PromptTemplates.kt` |
| Add new screen | Create in `ui/screens/`, add route to `NavGraph.kt` |
| Change juror attributes | `AIJuror.kt`, `parseJurors()`, `createDefaultJurors()` |
| Modify voting logic | `SimulationViewModel.kt` (startVoting, collectVotes, checkVerdictResult) |
| Add UI component | `ui/components/`, use in screens |

## Dependencies

- **UI**: Jetpack Compose with Material3, compose-bom:2024.12.01
- **Network**: Retrofit 2.9.0, OkHttp 4.12.0, Moshi 1.15.0 (with KSP codegen)
- **Storage**: Room 2.6.1, DataStore Preferences 1.0.0
- **Async**: Kotlin Coroutines 1.7.3, Lifecycle ViewModel Compose 2.7.0
- **Navigation**: Navigation Compose 2.7.6
- **Code Generation**: KSP (Kotlin Symbol Processing) for Moshi and Room

## Project Structure

```
com.charles.jurysim/
├── data/
│   ├── local/        # Room database (JurySimDatabase, CaseDao, CaseEntity)
│   ├── model/        # Data classes: SimulationState, AIJuror, Evidence, Message, TrialPhase
│   ├── remote/       # Ollama API (OllamaApiService, DTOs)
│   └── repository/   # OllamaRepository, PreferencesRepository, CaseHistoryRepository
├── ui/
│   ├── components/   # ChatBubble, LoadingIndicator, PhaseIndicator
│   ├── navigation/   # NavGraph.kt with Screen sealed class routes
│   ├── screens/      # home/, setup/, simulation/, profile/, settings/, history/
│   └── theme/        # Material3 theming (CourtBlue, CourtGold colors)
├── util/             # Constants.kt, PromptTemplates.kt
├── MainActivity.kt
└── JurySimApp.kt
```

## Note on Build Warning

Build may show a warning about compileSdk=35 with Android Gradle Plugin 8.2.0. This is expected and can be suppressed by adding `android.suppressUnsupportedCompileSdk=35` to gradle.properties.