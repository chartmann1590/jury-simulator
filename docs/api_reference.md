# API Reference

## Overview

This document provides a comprehensive reference for the public APIs and interfaces available in the Jury Simulator application. It covers the main classes, methods, and data structures that developers may interact with when extending or modifying the application.

## Package: com.jurysim.data.model

### SimulationState
Represents the complete state of an active trial simulation.

```kotlin
data class SimulationState(
    val currentPhase: TrialPhase = TrialPhase.SETUP,
    val messages: List<Message> = emptyList(),
    val caseTitle: String = "",
    val caseDescription: String = "",
    val defendantName: String = "",
    val charges: String = "",
    val isJurySelected: Boolean = false,
    val juryDismissalReason: String = "",
    val juryAcceptanceReason: String = "",
    val currentWitnessIndex: Int = 0,
    val totalWitnesses: Int = 0,
    val verdict: String? = null,
    val notes: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val aiJurors: List<AIJuror> = emptyList(),
    val evidenceItems: List<Evidence> = emptyList(),
    val currentVotingRound: Int = 0,
    val maxVotingRounds: Int = 5,
    val isVotingPhase: Boolean = false,
    val isMistrial: Boolean = false,
    val lastVoteResults: Map<Int, VoteChoice> = emptyMap(),
    val jurorConversations: Map<Int, List<Message>> = emptyMap(),
    val currentJurorChatId: Int = -1,
    val facts: List<Fact> = emptyList(),
    val showNotebook: Boolean = false
)
```

### AIJuror
Represents an AI-controlled juror.

```kotlin
data class AIJuror(
    val id: Int,
    val name: String,
    val occupation: String,
    val personality: String,
    val initialLeaning: JurorLeaning,
    val age: Int = 30,
    val hiddenBias: String = "None",
    val avatarId: Int = 0,
    var currentVote: VoteChoice? = null
)

enum class JurorLeaning {
    LEANING_GUILTY,
    LEANING_NOT_GUILTY,
    UNDECIDED
}

enum class VoteChoice {
    GUILTY,
    NOT_GUILTY
}
```

### Message
Represents a message in the trial conversation.

```kotlin
data class Message(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val speaker: String? = null
)
```

### TrialPhase
Enumeration of all possible trial phases.

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

### Fact
Represents a fact, evidence item, or witness in the trial.

```kotlin
enum class FactType {
    PERSON,
    EVIDENCE,
    TESTIMONY,
    OTHER
}

data class Fact(
    val id: String,
    val type: FactType,
    val title: String,
    val description: String,
    val source: String,
    var userNotes: String = ""
)
```

## Package: com.jurysim.ui.screens.simulation

### SimulationViewModel
Main ViewModel managing the trial simulation state and logic.

```kotlin
class SimulationViewModel(
    private val ollamaRepository: OllamaRepository,
    private val preferencesRepository: PreferencesRepository,
    private val caseHistoryRepository: CaseHistoryRepository
) : ViewModel()

// Public Methods:
fun initialize()
fun startVoirDire()
fun respondToVoirDire(userResponse: String)
fun startOpeningStatements()
fun startWitnessTestimony()
fun startEvidencePresentation()
fun startClosingArguments()
fun startDeliberation()
fun contributeToDeliberation(userInput: String)
fun proceedToVerdict()
fun proceedToNextPhase()
fun updateNotes(notes: String)
fun retryLastOperation()
fun clearError()
fun reset()
fun generateAIJurors()
fun chatWithJuror(jurorId: Int, userInput: String)
fun setCurrentJurorChat(jurorId: Int)
fun startVoting()
fun submitUserVote(vote: VoteChoice)
fun generateAINotes()
fun toggleNotebook()
fun addFact(fact: Fact)
fun updateFactNotes(factId: String, notes: String)
fun submitQuestion(question: String)
fun goToMainMenu()
```

## Package: com.jurysim.data.repository

### OllamaRepository
Handles communication with the Ollama API.

```kotlin
class OllamaRepository

// Public Methods:
fun updateBaseUrl(baseUrl: String)
suspend fun getModels(): Result<List<OllamaModel>>
suspend fun generate(model: String, prompt: String): Result<String>
suspend fun testConnection(): Result<Boolean>
```

### CaseHistoryRepository
Manages storage and retrieval of completed cases.

```kotlin
class CaseHistoryRepository(private val caseDao: CaseDao)

// Public Methods:
fun getAllCases(): Flow<List<CaseEntity>>
suspend fun saveCase(
    caseTitle: String,
    defendantName: String,
    charges: String,
    caseDescription: String,
    verdict: String,
    wasJurySelected: Boolean,
    modelUsed: String
)
suspend fun deleteCase(caseId: Long)
```

### PreferencesRepository
Manages user preferences and settings.

```kotlin
class PreferencesRepository(context: Context)

// Public Properties:
val selectedModel: Flow<String?>
val jurorProfile: Flow<JurorProfile?>

// Public Methods:
suspend fun setSelectedModel(model: String)
suspend fun setJurorProfile(profile: JurorProfile)
suspend fun getJurorProfile(): JurorProfile?
```

## Package: com.jurysim.data.local

### JurySimDatabase
Main database class for Room database.

```kotlin
abstract class JurySimDatabase : RoomDatabase() {
    abstract fun caseDao(): CaseDao

    companion object {
        fun getDatabase(context: Context): JurySimDatabase
    }
}
```

### CaseDao
Data Access Object for case operations.

```kotlin
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY completedTimestamp DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Insert
    suspend fun insertCase(caseEntity: CaseEntity): Long

    @Query("DELETE FROM cases WHERE id = :caseId")
    suspend fun deleteCase(caseId: Long)
}
```

### CaseEntity
Entity representing a completed case in the database.

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

## Package: com.jurysim.util

### Constants
Application-wide constants.

```kotlin
object Constants {
    const val DEFAULT_OLLAMA_URL = "http://localhost:11434"
    const val TOTAL_PROSECUTION_WITNESSES = 2
    const val TOTAL_DEFENSE_WITNESSES = 2
}
```

### PromptTemplates
Object containing all AI prompt templates.

```kotlin
object PromptTemplates {
    fun generateCase(): String
    fun voirDireIntro(
        caseTitle: String,
        charges: String,
        jurorName: String,
        jurorOccupation: String,
        jurorAge: Int,
        hasLegalExperience: Boolean
    ): String
    fun voirDireQuestion(previousConversation: String): String
    fun openingStatement(side: String, caseDescription: String, charges: String, defendantName: String): String
    fun generateWitness(
        witnessNumber: Int,
        totalWitnesses: Int,
        side: String,
        caseDescription: String,
        defendantName: String
    ): String
    fun crossExamination(witnessName: String, testimony: String, side: String): String
    fun presentEvidence(caseDescription: String, defendantName: String): String
    fun closingArgument(side: String, caseDescription: String, defendantName: String, charges: String): String
    fun deliberationStart(
        caseDescription: String,
        defendantName: String,
        charges: String,
        jurors: List<AIJuror>
    ): String
    fun deliberationResponse(
        conversation: String,
        userInput: String,
        jurors: List<AIJuror>
    ): String
    fun finalVerdict(conversation: String): String
    fun generateAIJurors(): String
    fun individualJurorChat(
        jurorName: String,
        jurorOccupation: String,
        jurorPersonality: String,
        jurorHiddenBias: String,
        jurorLeaning: String,
        caseDescription: String,
        defendantName: String,
        charges: String,
        previousConversation: String,
        userInput: String
    ): String
    fun jurorVote(
        jurorName: String,
        jurorPersonality: String,
        jurorHiddenBias: String,
        jurorLeaning: String,
        caseDescription: String,
        defendantName: String,
        charges: String,
        deliberationSummary: String,
        votingRound: Int
    ): String
    fun mistrialAnnouncement(caseTitle: String, votingRounds: Int): String
    fun generateAINotes(
        caseDescription: String,
        defendantName: String,
        charges: String,
        witnessTestimony: String,
        evidencePresented: String
    ): String
    fun judgeRuling(
        caseDescription: String,
        defendantName: String,
        charges: String,
        currentWitness: String,
        question: String
    ): String
    fun witnessCrossExam(
        witnessName: String,
        caseDescription: String,
        defendantName: String,
        question: String
    ): String
}
```

## Package: com.jurysim.data.remote

### OllamaApiService
Interface for Ollama API communication.

```kotlin
interface OllamaApiService {
    @GET("api/tags")
    suspend fun getTags(): Response<OllamaTagsResponse>

    @POST("api/generate")
    suspend fun generate(@Body request: OllamaGenerateRequest): Response<OllamaGenerateResponse>
}
```

## Package: com.jurysim.data.remote.dto

### OllamaGenerateRequest
Request body for the generate API endpoint.

```kotlin
data class OllamaGenerateRequest(
    @Json(name = "model") val model: String,
    @Json(name = "prompt") val prompt: String,
    @Json(name = "stream") val stream: Boolean = false,
    @Json(name = "options") val options: Map<String, Any>? = null
)
```

### OllamaGenerateResponse
Response from the generate API endpoint.

```kotlin
data class OllamaGenerateResponse(
    @Json(name = "model") val model: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "response") val response: String,
    @Json(name = "done") val done: Boolean,
    @Json(name = "context") val context: List<Int>? = null,
    @Json(name = "total_duration") val totalDuration: Long? = null,
    @Json(name = "load_duration") val loadDuration: Long? = null,
    @Json(name = "prompt_eval_count") val promptEvalCount: Int? = null,
    @Json(name = "eval_count") val evalCount: Int? = null
)
```

### OllamaTagsResponse
Response from the tags API endpoint.

```kotlin
data class OllamaTagsResponse(
    @Json(name = "models") val models: List<ModelInfo>
)

data class ModelInfo(
    @Json(name = "name") val name: String,
    @Json(name = "modified_at") val modifiedAt: String? = null,
    @Json(name = "size") val size: Long? = null,
    @Json(name = "digest") val digest: String? = null
)
```

This API reference provides comprehensive documentation of the public interfaces available in the Jury Simulator application, enabling developers to understand and extend the application's functionality.