# Architecture

## Overview

The Jury Simulator application follows the MVVM (Model-View-ViewModel) architectural pattern combined with modern Android development practices. The architecture emphasizes separation of concerns, testability, and maintainability.

## High-Level Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   UI Layer      │◄──►│  ViewModel Layer │◄──►│  Data Layer     │
│ (Compose)       │    │ (State holders)  │    │ (Repositories)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
         ▲                       ▲                       ▲
         │                       │                       │
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Navigation     │    │  Business Logic  │    │  Local Storage  │
│ (NavGraph)      │    │ (StateFlow)      │    │ (Room)          │
└─────────────────┘    └──────────────────┘    │  Remote APIs    │
                                              │ (Ollama)        │
                                              └─────────────────┘
```

## Layer Breakdown

### UI Layer

Located in `com.charles.jurysim.ui`, this layer contains:

- **Navigation**: `NavGraph.kt` manages app-wide navigation
- **Screens**: Individual composables for each screen
- **Components**: Reusable UI elements
- **Theme**: Material Design 3 theming

#### Key Principles
- Composables should be stateless or have minimal local state
- UI state is managed by ViewModels
- Follow Material Design 3 guidelines
- Accessibility-compliant

### ViewModel Layer

Located in `com.charles.jurysim.ui.screens.*`, this layer contains:

- **State Management**: Using `StateFlow` and `MutableStateFlow`
- **Business Logic**: Trial flow management, AI interaction logic
- **UI Events**: Handling user interactions and updating state

#### Key Classes
- `SimulationViewModel`: Central orchestrator for trial simulation
- Screen-specific ViewModels for navigation screens

### Data Layer

Located in `com.charles.jurysim.data`, this layer contains:

- **Models**: Data classes representing domain entities
- **Remote**: API interfaces and DTOs for Ollama integration
- **Local**: Room database components
- **Repository**: Abstraction layer for data operations

## Detailed Component Architecture

### Simulation State Management

The application uses a single source of truth approach with `SimulationState`:

```kotlin
data class SimulationState(
    val currentPhase: TrialPhase = TrialPhase.SETUP,
    val messages: List<Message> = emptyList(),
    val caseTitle: String = "",
    val caseDescription: String = "",
    val defendantName: String = "",
    val charges: String = "",
    // ... other properties
)
```

State updates are handled reactively using StateFlow:

```kotlin
private val _state = MutableStateFlow(SimulationState())
val state: StateFlow<SimulationState> = _state.asStateFlow()
```

### Trial Flow Management

The trial progression is managed through the `TrialPhase` enum:

```kotlin
enum class TrialPhase {
    SETUP,
    MODEL_SELECTION,
    INTRO,
    VOIR_DIRE,
    OPENING_STATEMENTS,
    WITNESS_TESTIMONY,
    EVIDENCE_PRESENTATION,
    CLOSING_ARGUMENTS,
    DELIBERATION,
    VERDICT
}
```

### Dependency Injection

Currently, dependencies are manually instantiated in the `NavGraph.kt`:

```kotlin
val ollamaRepository = remember { OllamaRepository() }
val simulationViewModel = remember { SimulationViewModel(ollamaRepository, preferencesRepository, caseHistoryRepository) }
```

Future versions may incorporate Hilt for more sophisticated dependency injection.

## Data Models

### Core Entities

- `SimulationState`: Single source of truth for active simulation
- `AIJuror`: Represents an AI-controlled juror with personality and biases
- `Message`: Represents a message in the trial conversation
- `Fact`: Represents evidence, witnesses, or other trial facts
- `CaseEntity`: Represents a completed case stored in the database

### Relationships

```
SimulationState 1 ←→ * Message
SimulationState 1 ←→ * AIJuror  
SimulationState 1 ←→ * Fact
CaseEntity ←→ CaseHistoryRepository
```

## Networking Layer

### Ollama Integration

The application communicates with Ollama through REST APIs:

- Base URL configurable by the user
- Uses Retrofit for HTTP communication
- Moshi for JSON serialization
- OkHttp for HTTP client functionality

### API Endpoints Used

- `/api/tags` - Retrieve available models
- `/api/generate` - Generate AI responses

## Database Layer

### Room Database

The local database stores case history:

- `JurySimDatabase`: Main database class
- `CaseDao`: Data access object for case operations
- `CaseEntity`: Entity representing a completed case

## Error Handling

The application implements comprehensive error handling:

- Network failure recovery
- AI service unavailability fallbacks
- Graceful degradation when services are unavailable
- User-friendly error messages

## Threading and Concurrency

- Uses Kotlin Coroutines for asynchronous operations
- `viewModelScope` for lifecycle-aware operations
- Proper thread handling for UI updates
- Background processing for AI requests

## Security Considerations

- Network security configuration allows cleartext for local development
- No sensitive data stored locally
- User data protection through DataStore
- Secure API communication with Ollama

This architecture provides a solid foundation for the Jury Simulator application, balancing simplicity with extensibility for future enhancements.