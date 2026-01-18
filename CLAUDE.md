# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Deploy Commands

```bash
# Generate gradle wrapper (if missing)
gradle wrapper

# Build debug APK
./gradlew assembleDebug

# Build and install on connected device
./gradlew installDebug

# Clean build
./gradlew clean

# Check connected devices
adb devices
```

Note: Build may show a warning about compileSdk=35 with Android Gradle Plugin 8.2.0. This is expected and can be suppressed by adding `android.suppressUnsupportedCompileSdk=35` to gradle.properties.

## Architecture Overview

**Jury-Sim** is an Android jury duty simulation app using Kotlin, Jetpack Compose, and Ollama AI for realistic trial simulations.

### MVVM Structure

```
com.jurysim/
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

### Key Architectural Patterns

- **State Management**: `SimulationViewModel` uses `MutableStateFlow<SimulationState>` as single source of truth for entire trial flow
- **Navigation**: Sealed class `Screen` routes in `NavGraph.kt` with manual ViewModel instantiation (no DI framework)
- **AI Integration**: `PromptTemplates.kt` contains all LLM prompts; `OllamaRepository` handles API calls with 300s timeouts
- **Data Persistence**: Room for case history, DataStore for preferences/settings

### Trial Flow

```
Home → Setup (server URL) → ModelSelection → Intro (case generated)
  → VoirDire (jury selection) → [JurySelected | JuryDismissed]
  → Trial (opening → witnesses → evidence → closing)
  → Deliberation (voting rounds, individual juror chats)
  → Verdict (GUILTY | NOT GUILTY | MISTRIAL)
```

### AI Juror System

Each of 11 AI jurors has:
- `name`, `age`, `occupation`, `personality`
- `hiddenBias` - influences voting subtly without explicit mention
- `initialLeaning` - LEANING_GUILTY, LEANING_NOT_GUILTY, or UNDECIDED
- `avatarId` - for UI display

Jurors are generated via `PromptTemplates.generateAIJurors()` with regex parsing in `SimulationViewModel.parseJurors()`. Fallback to `createDefaultJurors()` if parsing fails.

### Voting System

- Unanimous verdict required (all 12 jurors including user)
- Maximum 5 voting rounds before mistrial declared
- Between rounds, users can chat with individual jurors or group
- `VoteChoice` enum: GUILTY, NOT_GUILTY

### Network Configuration

Cleartext HTTP allowed for local Ollama servers:
- localhost, 10.0.2.2 (emulator), 192.168.*.* (LAN)
- Configured in `res/xml/network_security_config.xml`

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
