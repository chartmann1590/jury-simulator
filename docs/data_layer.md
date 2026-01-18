# Data Layer

## Overview

The data layer of the Jury Simulator application manages all data operations, including local storage, remote API communication, and data transformation. It follows the Repository pattern to abstract data sources and provide a clean API to the rest of the application.

## Architecture

### Layer Structure

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Domain        │    │   Repository     │    │   Data Sources  │
│   Layer         │◄──►│   Layer          │◄──►│   Layer         │
│                 │    │                  │    │                 │
│ (Use Cases)     │    │ (Abstraction)    │    │ (Local/Remote)  │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                             │                           │
                             │                           │
                      ┌──────────────────┐         ┌─────────────────┐
                      │   Data Model     │         │   Local DB      │
                      │   Layer          │         │   (Room)        │
                      │                  │         │                 │
                      │ (Entities/Data   │         │ • CaseEntity    │
                      │  Classes)        │         │ • CaseDao       │
                      └──────────────────┘         │ • Database      │
                             │                     └─────────────────┘
                             │                               │
                             └───────────────────────────────┘
```

## Data Models

### Core Data Classes

Located in `com.jurysim.data.model`, these represent the domain entities:

#### SimulationState
- Single source of truth for active simulation
- Contains current phase, messages, case details, and juror information
- Managed with StateFlow in ViewModels

#### AIJuror
- Represents an AI-controlled juror
- Properties: name, occupation, personality, hidden bias, initial leaning
- Includes voting behavior and avatar information

#### Message
- Represents a message in the trial conversation
- Contains content, sender information, and timestamp
- Differentiates between user and AI messages

#### Fact
- Represents evidence, witnesses, or other trial facts
- Categorized by type (PERSON, EVIDENCE, TESTIMONY, OTHER)
- Includes user notes for personal observations

#### TrialPhase
- Enum defining the current phase of the trial
- Controls navigation and UI behavior
- Ensures proper trial flow sequence

## Repositories

### OllamaRepository
Handles communication with the Ollama API:

```kotlin
class OllamaRepository {
    suspend fun getModels(): Result<List<OllamaModel>>
    suspend fun generate(model: String, prompt: String): Result<String>
    suspend fun testConnection(): Result<Boolean>
    fun updateBaseUrl(baseUrl: String)
}
```

**Responsibilities:**
- API communication with Ollama instance
- Request/response handling
- Connection management
- Error handling and retry logic

### CaseHistoryRepository
Manages storage and retrieval of completed cases:

```kotlin
class CaseHistoryRepository(private val caseDao: CaseDao) {
    fun getAllCases(): Flow<List<CaseEntity>>
    suspend fun saveCase(...)
    suspend fun deleteCase(caseId: Long)
}
```

**Responsibilities:**
- Persist completed trial data
- Retrieve historical cases
- Manage case lifecycle

### PreferencesRepository
Handles user preferences and settings:

```kotlin
class PreferencesRepository(context: Context) {
    val selectedModel: Flow<String?>
    suspend fun setSelectedModel(model: String)
    // Other preference methods
}
```

**Responsibilities:**
- Store user preferences
- Manage settings persistence
- Handle profile information

## Local Database (Room)

### Database Schema

#### CaseEntity
```kotlin
@Entity(tableName = "cases")
data class CaseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val caseTitle: String,
    val defendantName: String,
    val charges: String,
    val caseDescription: String,
    val verdict: String,
    val wasJurySelected: Boolean,
    val completedTimestamp: Long,
    val modelUsed: String
)
```

### DAO (Data Access Object)

#### CaseDao
```kotlin
@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY completedTimestamp DESC")
    fun getAllCases(): Flow<List<CaseEntity>>
    
    @Insert
    suspend fun insertCase(caseEntity: CaseEntity): Long
    
    @Query("DELETE FROM cases WHERE id = :caseId")
    suspend fun deleteCase(caseId: Long)
}
```

### Database Class

#### JurySimDatabase
```kotlin
@Database(
    entities = [CaseEntity::class],
    version = 1,
    exportSchema = false
)
abstract class JurySimDatabase : RoomDatabase() {
    abstract fun caseDao(): CaseDao
}
```

## Remote API Integration

### API Service Definition

#### OllamaApiService
```kotlin
interface OllamaApiService {
    @GET("api/tags")
    suspend fun getTags(): Response<OllamaTagsResponse>

    @POST("api/generate")
    suspend fun generate(@Body request: OllamaGenerateRequest): Response<OllamaGenerateResponse>
}
```

### Data Transfer Objects (DTOs)

#### Request DTOs
- `OllamaGenerateRequest`: Request body for generation API
- Contains model, prompt, and optional parameters

#### Response DTOs
- `OllamaGenerateResponse`: Response from generation API
- `OllamaTagsResponse`: Response from model listing API

## Data Flow Patterns

### Reading Data
1. UI collects data from ViewModel
2. ViewModel observes Repository
3. Repository queries local data source or fetches from remote
4. Data flows back through the chain

### Writing Data
1. UI triggers action
2. ViewModel calls Repository method
3. Repository updates local data source or sends to remote
4. Changes propagate through observables if needed

## Error Handling

### Result Wrapper
Repositories return `Result<T>` for consistent error handling:

```kotlin
suspend fun generate(model: String, prompt: String): Result<String>
```

### Network Error Recovery
- Connection timeout handling
- Retry mechanisms
- Fallback strategies
- User-friendly error messages

## Caching Strategy

### Local Caching
- Completed cases stored in Room database
- User preferences in DataStore
- No caching of API responses (each request is unique)

### Cache Invalidation
- Automatic invalidation when data changes
- Fresh data retrieval for each trial
- Proper cleanup of temporary data

## Data Transformation

### Mapping Layers
- DTOs to domain models in Repository layer
- Domain models to UI models in ViewModel layer
- Proper validation during transformation

### Serialization
- Moshi for JSON serialization
- Automatic adapter generation with KSP
- Proper error handling for malformed data

## Thread Safety

### Coroutines
- Proper dispatcher usage
- Thread-safe operations
- Lifecycle-aware scopes

### Concurrent Access
- Room database handles threading
- Immutable data models where possible
- Proper synchronization for shared resources

## Testing Considerations

### Repository Testing
- Mock remote data sources
- Test error scenarios
- Verify data transformations
- Check caching behavior

### Database Testing
- In-memory database for tests
- Migration testing
- Query validation
- Performance considerations

This data layer architecture ensures clean separation of concerns, proper abstraction of data sources, and maintainable code that can evolve with changing requirements.