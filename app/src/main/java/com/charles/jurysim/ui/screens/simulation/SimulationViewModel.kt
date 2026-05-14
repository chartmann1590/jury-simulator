package com.charles.jurysim.ui.screens.simulation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.charles.jurysim.data.model.AIJuror
import com.charles.jurysim.data.model.Evidence
import com.charles.jurysim.data.model.JurorLeaning
import com.charles.jurysim.data.model.Message
import com.charles.jurysim.data.model.SimulationState
import com.charles.jurysim.data.model.TrialPhase
import com.charles.jurysim.data.model.VoteChoice
import com.charles.jurysim.data.llm.LlmEngine
import com.charles.jurysim.data.repository.CaseHistoryRepository
import com.charles.jurysim.data.repository.PreferencesRepository
import com.charles.jurysim.util.Constants
import com.charles.jurysim.util.PromptTemplates
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
import kotlin.text.MatchResult

class SimulationViewModel(
    private val llmEngine: LlmEngine,
    private val preferencesRepository: PreferencesRepository,
    private val caseHistoryRepository: CaseHistoryRepository
) : ViewModel() {
    private data class JudgeProfile(
        val name: String,
        val gender: String,
        val voiceSeed: Int
    )

    private val _state = MutableStateFlow(SimulationState())
    val state: StateFlow<SimulationState> = _state.asStateFlow()

    private var voirDireQuestionCount = 0
    private var lastFailedOperation: (suspend () -> Unit)? = null
    private var jurorProfile: com.charles.jurysim.data.model.JurorProfile? = null
    private var trialMessages: List<Message> = emptyList()
    private var judgeProfile: JudgeProfile? = null

    init {
        viewModelScope.launch {
            preferencesRepository.ttsEnabled.collectLatest { enabled ->
                _state.value = _state.value.copy(ttsEnabled = enabled)
            }
        }
    }

    fun initialize() {
        viewModelScope.launch {
            jurorProfile = preferencesRepository.getJurorProfile()
            generateCase()
        }
    }

    private suspend fun generateCase() {
        judgeProfile = createJudgeProfile()
        _state.value = _state.value.copy(
            isLoading = true,
            currentPhase = TrialPhase.INTRO,
            judgeName = judgeProfile?.name.orEmpty(),
            judgeGender = judgeProfile?.gender ?: "UNKNOWN",
            judgeVoiceSeed = judgeProfile?.voiceSeed ?: 0
        )

        val prompt = PromptTemplates.generateCase()
        val result = llmEngine.generate(prompt)

        result.onSuccess { response ->
            parseCase(response)
        }.onFailure { error ->
            lastFailedOperation = { generateCase() }
            _state.value = _state.value.copy(
                isLoading = false,
                error = error.message
            )
        }
    }

    private fun parseCase(response: String) {
        val caseTitle = response.substringAfter("CASE_TITLE:").substringBefore("DEFENDANT:").trim()
        val defendant = response.substringAfter("DEFENDANT:").substringBefore("CHARGES:").trim()
        val charges = response.substringAfter("CHARGES:").substringBefore("DESCRIPTION:").trim()
        val description = response.substringAfter("DESCRIPTION:").substringBefore("EVIDENCE:").trim()

        _state.value = _state.value.copy(
            caseTitle = caseTitle,
            defendantName = defendant,
            charges = charges,
            caseDescription = description,
            isLoading = false
        )
    }

    fun startVoirDire() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentPhase = TrialPhase.VOIR_DIRE,
                messages = emptyList(),
                isLoading = true
            )

            val profile = jurorProfile ?: com.charles.jurysim.data.model.JurorProfile()

            val prompt = PromptTemplates.voirDireIntro(
                caseTitle = _state.value.caseTitle,
                charges = _state.value.charges,
                judgeName = getJudgeName(),
                jurorName = profile.name.ifBlank { "Potential Juror" },
                jurorOccupation = profile.occupation.ifBlank { "Not specified" },
                jurorAge = profile.age,
                hasLegalExperience = profile.hasLegalExperience
            )

            val result = llmEngine.generate(prompt)

            result.onSuccess { response ->
                val message = Message(
                    content = response,
                    isUser = false,
                    speaker = getJudgeDisplaySpeaker()
                )
                _state.value = _state.value.copy(
                    messages = _state.value.messages + message,
                    isLoading = false
                )
                voirDireQuestionCount = 1
            }.onFailure { error ->
                lastFailedOperation = {
                    startVoirDire()
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun respondToVoirDire(userResponse: String) {
        viewModelScope.launch {
            if (_state.value.isJurySelected || _state.value.isJuryDismissed || _state.value.isCaseClosed) {
                return@launch
            }

            val userMessage = Message(content = userResponse, isUser = true)
            _state.value = _state.value.copy(
                messages = _state.value.messages + userMessage,
                isLoading = true
            )

            val conversation = _state.value.messages.joinToString("\n") {
                "${if (it.isUser) "Potential Juror" else it.speaker}: ${it.content}"
            }

            val prompt = PromptTemplates.voirDireQuestion(
                previousConversation = conversation,
                judgeName = getJudgeName()
            )
            val result = llmEngine.generate(prompt)

            result.onSuccess { response ->
                val message = Message(
                    content = response,
                    isUser = false,
                    speaker = getJudgeDisplaySpeaker()
                )

                val updatedMessages = _state.value.messages + message

                // Check if selected or dismissed
                val isSelected = response.contains("SELECTED:", ignoreCase = true)
                val isDismissed = response.contains("DISMISSED:", ignoreCase = true)

                // Extract reasons if provided
                val acceptanceReason = if (isSelected) {
                    response.substringAfter("SELECTED:", "").trim().ifBlank {
                        "The court believes you will be a fair and impartial juror for this case."
                    }
                } else ""

                val dismissalReason = if (isDismissed) {
                    response.substringAfter("DISMISSED:", "").trim().ifBlank {
                        "Based on your responses during voir dire, the attorneys decided to exercise a peremptory challenge."
                    }
                } else ""

                _state.value = _state.value.copy(
                    messages = updatedMessages,
                    isLoading = false,
                    isJurySelected = isSelected,
                    isJuryDismissed = isDismissed,
                    isCaseClosed = isDismissed,
                    juryAcceptanceReason = acceptanceReason,
                    juryDismissalReason = dismissalReason
                )

                if (isSelected) {
                    voirDireQuestionCount = 0
                } else if (isDismissed) {
                    // Don't auto-generate new case, let JuryDismissedScreen handle it
                }

                voirDireQuestionCount++
            }.onFailure { error ->
                lastFailedOperation = {
                    respondToVoirDire(userResponse)
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun startOpeningStatements() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentPhase = TrialPhase.OPENING_STATEMENTS,
                messages = emptyList(),
                isLoading = true
            )

            // Prosecution opening
            val prosecutionPrompt = PromptTemplates.openingStatement(
                side = "Prosecution",
                caseDescription = _state.value.caseDescription,
                charges = _state.value.charges,
                defendantName = _state.value.defendantName
            )

            // Defense opening
            val defensePrompt = PromptTemplates.openingStatement(
                side = "Defense",
                caseDescription = _state.value.caseDescription,
                charges = _state.value.charges,
                defendantName = _state.value.defendantName
            )

            try {
                // Run generations in parallel to save time
                val prosecutionDeferred = async { llmEngine.generate(prosecutionPrompt) }
                val defenseDeferred = async { llmEngine.generate(defensePrompt) }

                val prosecutionResult = prosecutionDeferred.await()
                val defenseResult = defenseDeferred.await()

                val newMessages = mutableListOf<Message>()

                prosecutionResult.onSuccess { response ->
                    newMessages.add(Message(content = response, isUser = false, speaker = "Prosecutor"))
                }.onFailure {
                    // Log error but continue if possible?
                }

                defenseResult.onSuccess { response ->
                    newMessages.add(Message(content = response, isUser = false, speaker = "Defense Attorney"))
                }

                if (newMessages.isEmpty()) {
                    val error = prosecutionResult.exceptionOrNull()?.message ?: defenseResult.exceptionOrNull()?.message ?: "Failed to generate statements"
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error
                    )
                } else {
                    _state.value = _state.value.copy(
                        messages = newMessages,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun startWitnessTestimony() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentPhase = TrialPhase.WITNESS_TESTIMONY,
                currentWitnessIndex = 0,
                totalWitnesses = Constants.TOTAL_PROSECUTION_WITNESSES + Constants.TOTAL_DEFENSE_WITNESSES,
                isLoading = true
            )

            presentNextWitness()
        }
    }

    private suspend fun presentNextWitness() {
        val currentIndex = _state.value.currentWitnessIndex
        val totalWitnesses = _state.value.totalWitnesses

        if (currentIndex >= totalWitnesses) {
            _state.value = _state.value.copy(isLoading = false)
            return
        }

        val side = if (currentIndex < Constants.TOTAL_PROSECUTION_WITNESSES) "Prosecution" else "Defense"
        val witnessNumber = if (side == "Prosecution") currentIndex + 1 else currentIndex - Constants.TOTAL_PROSECUTION_WITNESSES + 1
        val totalForSide = if (side == "Prosecution") Constants.TOTAL_PROSECUTION_WITNESSES else Constants.TOTAL_DEFENSE_WITNESSES

        val prompt = PromptTemplates.generateWitness(
            witnessNumber = witnessNumber,
            totalWitnesses = totalForSide,
            side = side,
            caseDescription = _state.value.caseDescription,
            defendantName = _state.value.defendantName
        )

        val result = llmEngine.generate(prompt)

        result.onSuccess { response ->
            val witnessName = response.substringAfter("WITNESS:").substringBefore("TESTIMONY:").trim()
            val testimony = response.substringAfter("TESTIMONY:").trim()

            val message = Message(
                content = "$witnessName takes the stand.\n\n$testimony",
                isUser = false,
                speaker = "$side Witness"
            )

            // Auto-tag Witness
            addFact(com.charles.jurysim.data.model.Fact(
                id = "WIT_${currentIndex + 1}",
                type = com.charles.jurysim.data.model.FactType.PERSON,
                title = witnessName,
                description = "Witness for the $side",
                source = "Witness Testimony"
            ))

            _state.value = _state.value.copy(
                messages = _state.value.messages + message,
                currentWitnessIndex = currentIndex + 1,
                isLoading = false
            )
        }.onFailure { error ->
            _state.value = _state.value.copy(
                isLoading = false,
                error = error.message
            )
        }
    }

    fun startEvidencePresentation() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentPhase = TrialPhase.EVIDENCE_PRESENTATION,
                isLoading = true
            )

            val prompt = PromptTemplates.presentEvidence(
                caseDescription = _state.value.caseDescription,
                defendantName = _state.value.defendantName
            )

            val result = llmEngine.generate(prompt)

            result.onSuccess { response ->
                val message = Message(
                    content = response,
                    isUser = false,
                    speaker = "Prosecutor"
                )
                
                parseAndStoreEvidence(response)
                
                _state.value = _state.value.copy(
                    messages = _state.value.messages + message,
                    isLoading = false
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun startClosingArguments() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentPhase = TrialPhase.CLOSING_ARGUMENTS,
                isLoading = true
            )

            // Prosecution closing
            val prosecutionPrompt = PromptTemplates.closingArgument(
                side = "Prosecution",
                caseDescription = _state.value.caseDescription,
                defendantName = _state.value.defendantName,
                charges = _state.value.charges
            )

            // Defense closing
            val defensePrompt = PromptTemplates.closingArgument(
                side = "Defense",
                caseDescription = _state.value.caseDescription,
                defendantName = _state.value.defendantName,
                charges = _state.value.charges
            )

            try {
                // Run generations in parallel
                val prosecutionDeferred = async { llmEngine.generate(prosecutionPrompt) }
                val defenseDeferred = async { llmEngine.generate(defensePrompt) }

                val prosecutionResult = prosecutionDeferred.await()
                val defenseResult = defenseDeferred.await()

                val newMessages = mutableListOf<Message>()

                prosecutionResult.onSuccess { response ->
                    newMessages.add(Message(content = response, isUser = false, speaker = "Prosecutor"))
                }

                defenseResult.onSuccess { response ->
                    newMessages.add(Message(content = response, isUser = false, speaker = "Defense Attorney"))
                }

                if (newMessages.isEmpty()) {
                    val error = prosecutionResult.exceptionOrNull()?.message ?: defenseResult.exceptionOrNull()?.message ?: "Failed to generate closing arguments"
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = error
                    )
                } else {
                    _state.value = _state.value.copy(
                        messages = _state.value.messages + newMessages,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun startDeliberation() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentPhase = TrialPhase.DELIBERATION,
                messages = emptyList(),
                isLoading = true
            )

            val prompt = PromptTemplates.deliberationStart(
                caseDescription = _state.value.caseDescription,
                defendantName = _state.value.defendantName,
                charges = _state.value.charges,
                jurors = _state.value.aiJurors
            )

            val result = llmEngine.generate(prompt)

            result.onSuccess { response ->
                val message = Message(
                    content = response,
                    isUser = false,
                    speaker = "Jury Room"
                )
                _state.value = _state.value.copy(
                    messages = _state.value.messages + message,
                    isLoading = false
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun contributeToDeliberation(userInput: String) {
        viewModelScope.launch {
            val userMessage = Message(content = userInput, isUser = true, speaker = "You")
            _state.value = _state.value.copy(
                messages = _state.value.messages + userMessage,
                isLoading = true
            )

            val conversation = _state.value.messages.joinToString("\n") {
                "${it.speaker ?: if (it.isUser) "User" else "Other"}: ${it.content}"
            }

            val prompt = PromptTemplates.deliberationResponse(
                conversation = conversation,
                userInput = userInput,
                jurors = _state.value.aiJurors
            )
            val result = llmEngine.generate(prompt)

            result.onSuccess { response ->
                val message = Message(
                    content = response,
                    isUser = false,
                    speaker = "Jury Room"
                )
                _state.value = _state.value.copy(
                    messages = _state.value.messages + message,
                    isLoading = false
                )
            }.onFailure { error ->
                lastFailedOperation = {
                    // Remove the user message we just added since we're going to retry
                    val messagesWithoutLast = _state.value.messages.dropLast(1)
                    _state.value = _state.value.copy(messages = messagesWithoutLast)
                    contributeToDeliberation(userInput)
                }
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun proceedToVerdict() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentPhase = TrialPhase.VERDICT,
                isLoading = true
            )

            val conversation = _state.value.messages.joinToString("\n") {
                "${it.speaker ?: if (it.isUser) "User" else "Other"}: ${it.content}"
            }

            val prompt = PromptTemplates.finalVerdict(conversation)
            val result = llmEngine.generate(prompt)

            result.onSuccess { response ->
                val verdict = if (response.contains("VERDICT: GUILTY", ignoreCase = true)) {
                    "GUILTY"
                } else {
                    "NOT GUILTY"
                }

                val reasoning = response.substringAfter("REASONING:", "").trim()
                val message = Message(
                    content = reasoning,
                    isUser = false,
                    speaker = "Jury"
                )

                _state.value = _state.value.copy(
                    verdict = verdict,
                    messages = _state.value.messages + message,
                    isLoading = false
                )

                // Save case to history
                viewModelScope.launch {
                    caseHistoryRepository.saveCase(
                        caseTitle = _state.value.caseTitle,
                        defendantName = _state.value.defendantName,
                        charges = _state.value.charges,
                        caseDescription = _state.value.caseDescription,
                        verdict = verdict,
                        wasJurySelected = _state.value.isJurySelected,
                        modelUsed = Constants.LITERTLM_MODEL_DISPLAY_NAME
                    )
                }
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun proceedToNextPhase() {
        when (_state.value.currentPhase) {
            TrialPhase.OPENING_STATEMENTS -> {
                if (_state.value.currentWitnessIndex < _state.value.totalWitnesses) {
                    viewModelScope.launch {
                        _state.value = _state.value.copy(isLoading = true)
                        presentNextWitness()
                    }
                } else {
                    startWitnessTestimony()
                }
            }
            TrialPhase.WITNESS_TESTIMONY -> {
                if (_state.value.currentWitnessIndex < _state.value.totalWitnesses) {
                    viewModelScope.launch {
                        _state.value = _state.value.copy(isLoading = true)
                        presentNextWitness()
                    }
                } else {
                    startEvidencePresentation()
                }
            }
            TrialPhase.EVIDENCE_PRESENTATION -> startClosingArguments()
            TrialPhase.CLOSING_ARGUMENTS -> startDeliberation()
            else -> {}
        }
    }

    fun updateNotes(notes: String) {
        _state.value = _state.value.copy(notes = notes)
    }

    fun retryLastOperation() {
        viewModelScope.launch {
            _state.value = _state.value.copy(error = null)
            lastFailedOperation?.invoke()
            lastFailedOperation = null
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun reset() {
        _state.value = SimulationState()
        voirDireQuestionCount = 0
        lastFailedOperation = null
        trialMessages = emptyList()
        initialize()
    }

    // Generate AI jurors when jury is selected
    fun generateAIJurors() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val prompt = PromptTemplates.generateAIJurors()
            val result = llmEngine.generate(prompt)

            result.onSuccess { response ->
                val jurors = parseJurors(response)
                _state.value = _state.value.copy(
                    aiJurors = jurors,
                    isLoading = false
                )
            }.onFailure { error ->
                // Create default jurors if generation fails
                val defaultJurors = createDefaultJurors()
                _state.value = _state.value.copy(
                    aiJurors = defaultJurors,
                    isLoading = false,
                    error = "Using default jurors: ${error.message}"
                )
            }
        }
    }

    private fun parseJurors(response: String): List<AIJuror> = JurorParsing.parseJurors(response)

    private fun createDefaultJurors(): List<AIJuror> = JurorParsing.defaultJurors()

    // Chat with individual juror
    fun chatWithJuror(jurorId: Int, userInput: String) {
        viewModelScope.launch {
            val juror = _state.value.aiJurors.find { it.id == jurorId } ?: return@launch

            val userMessage = Message(content = userInput, isUser = true, speaker = "You")
            val currentConversation = _state.value.jurorConversations[jurorId] ?: emptyList()
            val updatedConversation = currentConversation + userMessage

            _state.value = _state.value.copy(
                jurorConversations = _state.value.jurorConversations + (jurorId to updatedConversation),
                currentJurorChatId = jurorId,
                isLoading = true
            )

            val conversationText = updatedConversation.joinToString("\n") {
                "${if (it.isUser) "You" else juror.name}: ${it.content}"
            }

            val prompt = PromptTemplates.individualJurorChat(
                jurorName = juror.name,
                jurorOccupation = juror.occupation,
                jurorPersonality = juror.personality,
                jurorHiddenBias = juror.hiddenBias,
                jurorLeaning = juror.initialLeaning.name,
                caseDescription = _state.value.caseDescription,
                defendantName = _state.value.defendantName,
                charges = _state.value.charges,
                previousConversation = conversationText,
                userInput = userInput
            )

            val result = llmEngine.generate(prompt)

            result.onSuccess { response ->
                val jurorMessage = Message(
                    content = response.trim(),
                    isUser = false,
                    speaker = juror.name
                )
                val finalConversation = updatedConversation + jurorMessage
                _state.value = _state.value.copy(
                    jurorConversations = _state.value.jurorConversations + (jurorId to finalConversation),
                    isLoading = false
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = error.message
                )
            }
        }
    }

    fun setCurrentJurorChat(jurorId: Int) {
        _state.value = _state.value.copy(currentJurorChatId = jurorId)
    }

    // Start voting process
    fun startVoting() {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                isVotingPhase = true,
                currentVotingRound = _state.value.currentVotingRound + 1,
                isLoading = true
            )

            collectVotes()
        }
    }

    private suspend fun collectVotes() {
        val votes = mutableMapOf<Int, VoteChoice>()
        val deliberationSummary = _state.value.messages.takeLast(10).joinToString("\n") {
            "${it.speaker ?: "Unknown"}: ${it.content}"
        }

        for (juror in _state.value.aiJurors) {
            val prompt = PromptTemplates.jurorVote(
                jurorName = juror.name,
                jurorPersonality = juror.personality,
                jurorHiddenBias = juror.hiddenBias,
                jurorLeaning = juror.initialLeaning.name,
                caseDescription = _state.value.caseDescription,
                defendantName = _state.value.defendantName,
                charges = _state.value.charges,
                deliberationSummary = deliberationSummary,
                votingRound = _state.value.currentVotingRound
            )

            val result = llmEngine.generate(prompt)
            result.onSuccess { response ->
                val vote = if (response.contains("VOTE: GUILTY", ignoreCase = true) &&
                              !response.contains("NOT_GUILTY", ignoreCase = true) &&
                              !response.contains("NOT GUILTY", ignoreCase = true)) {
                    VoteChoice.GUILTY
                } else {
                    VoteChoice.NOT_GUILTY
                }
                votes[juror.id] = vote
            }.onFailure {
                // Default to juror's leaning if vote fails
                votes[juror.id] = when (juror.initialLeaning) {
                    JurorLeaning.LEANING_GUILTY -> VoteChoice.GUILTY
                    JurorLeaning.LEANING_NOT_GUILTY -> VoteChoice.NOT_GUILTY
                    JurorLeaning.UNDECIDED -> if (Math.random() > 0.5) VoteChoice.GUILTY else VoteChoice.NOT_GUILTY
                }
            }
        }

        // Add user's vote (we'll ask them separately)
        _state.value = _state.value.copy(
            lastVoteResults = votes,
            isLoading = false
        )

        checkVerdictResult(votes)
    }

    fun submitUserVote(vote: VoteChoice) {
        val updatedVotes = _state.value.lastVoteResults.toMutableMap()
        updatedVotes[0] = vote // User is juror 0

        _state.value = _state.value.copy(lastVoteResults = updatedVotes)
        checkVerdictResult(updatedVotes)
    }

    private fun checkVerdictResult(votes: Map<Int, VoteChoice>) {
        val decision = VerdictResolver.resolve(
            votes = votes,
            currentRound = _state.value.currentVotingRound,
            maxRounds = _state.value.maxVotingRounds
        )

        when (decision) {
            is VerdictDecision.Unanimous -> finalizeVerdict(decision.verdict)
            VerdictDecision.Mistrial -> declareMistrial()
            is VerdictDecision.Hung -> {
                val voteMessage = Message(
                    content = "Vote results - Round ${_state.value.currentVotingRound}: Guilty: ${decision.guilty}, Not Guilty: ${decision.notGuilty}. The jury has not reached a unanimous verdict. Continue deliberation.",
                    isUser = false,
                    speaker = "Foreperson"
                )
                _state.value = _state.value.copy(
                    isVotingPhase = false,
                    messages = _state.value.messages + voteMessage
                )
            }
        }
    }

    private fun finalizeVerdict(verdict: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(
                currentPhase = TrialPhase.VERDICT,
                verdict = verdict,
                isVotingPhase = false,
                isLoading = false
            )

            val reasonMessage = Message(
                content = "The jury has reached a unanimous verdict of $verdict after ${_state.value.currentVotingRound} round(s) of voting.",
                isUser = false,
                speaker = "Foreperson"
            )
            _state.value = _state.value.copy(
                messages = _state.value.messages + reasonMessage
            )

            // Save case to history
            caseHistoryRepository.saveCase(
                caseTitle = _state.value.caseTitle,
                defendantName = _state.value.defendantName,
                charges = _state.value.charges,
                caseDescription = _state.value.caseDescription,
                verdict = verdict,
                wasJurySelected = _state.value.isJurySelected,
                modelUsed = Constants.LITERTLM_MODEL_DISPLAY_NAME
            )
        }
    }

    private fun declareMistrial() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val prompt = PromptTemplates.mistrialAnnouncement(
                caseTitle = _state.value.caseTitle,
                votingRounds = _state.value.maxVotingRounds,
                judgeName = getJudgeName()
            )

            val result = llmEngine.generate(prompt)

            result.onSuccess { response ->
                val mistrialMessage = Message(
                    content = response,
                    isUser = false,
                    speaker = getJudgeDisplaySpeaker()
                )
                _state.value = _state.value.copy(
                    currentPhase = TrialPhase.VERDICT,
                    verdict = "MISTRIAL",
                    isMistrial = true,
                    isVotingPhase = false,
                    messages = _state.value.messages + mistrialMessage,
                    isLoading = false
                )
            }.onFailure {
                _state.value = _state.value.copy(
                    currentPhase = TrialPhase.VERDICT,
                    verdict = "MISTRIAL",
                    isMistrial = true,
                    isVotingPhase = false,
                    isLoading = false
                )
            }

            // Save case to history
            caseHistoryRepository.saveCase(
                caseTitle = _state.value.caseTitle,
                defendantName = _state.value.defendantName,
                charges = _state.value.charges,
                caseDescription = _state.value.caseDescription,
                verdict = "MISTRIAL",
                wasJurySelected = _state.value.isJurySelected,
                modelUsed = Constants.LITERTLM_MODEL_DISPLAY_NAME
            )
        }
    }

    // Generate AI notes from trial
    fun generateAINotes() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val witnessTestimony = trialMessages
                .filter { it.speaker?.contains("Witness") == true }
                .joinToString("\n\n") { "${it.speaker}: ${it.content}" }

            val evidenceText = _state.value.evidenceItems.joinToString("\n") {
                "- ${it.name}: ${it.description} (${it.significance})"
            }.ifEmpty {
                trialMessages
                    .filter { it.content.contains("EVIDENCE", ignoreCase = true) }
                    .joinToString("\n\n") { it.content }
            }

            val prompt = PromptTemplates.generateAINotes(
                caseDescription = _state.value.caseDescription,
                defendantName = _state.value.defendantName,
                charges = _state.value.charges,
                witnessTestimony = witnessTestimony,
                evidencePresented = evidenceText
            )

            val result = llmEngine.generate(prompt)

            result.onSuccess { response ->
                _state.value = _state.value.copy(
                    notes = response.trim(),
                    isLoading = false
                )
            }.onFailure { error ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Failed to generate notes: ${error.message}"
                )
            }
        }
    }

    // Store evidence items when they're presented
    fun storeTrialMessages() {
        trialMessages = _state.value.messages.toList()
    }

    fun toggleNotebook() {
        _state.value = _state.value.copy(showNotebook = !_state.value.showNotebook)
    }

    fun addFact(fact: com.charles.jurysim.data.model.Fact) {
        _state.value = _state.value.copy(
            facts = _state.value.facts + fact
        )
    }

    fun updateFactNotes(factId: String, notes: String) {
        val updatedFacts = _state.value.facts.map {
            if (it.id == factId) it.copy(userNotes = notes) else it
        }
        _state.value = _state.value.copy(facts = updatedFacts)
    }

    fun submitQuestion(question: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            // 1. Ask Judge
            val judgePrompt = PromptTemplates.judgeRuling(
                caseDescription = _state.value.caseDescription,
                defendantName = _state.value.defendantName,
                charges = _state.value.charges,
                currentWitness = "Witness ${_state.value.currentWitnessIndex}",
                question = question,
                judgeName = getJudgeName()
            )

            val judgeResult = llmEngine.generate(judgePrompt)
            
            val judgeResponse = judgeResult.getOrElse {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Judge failed to rule: ${it.message}"
                )
                return@launch
            }

            val isAllowed = judgeResponse.contains("ALLOWED", ignoreCase = true)
            val judgeMessage = Message(
                content = judgeResponse,
                isUser = false,
                speaker = getJudgeDisplaySpeaker()
            )
            
            _state.value = _state.value.copy(
                messages = _state.value.messages + Message(content = question, isUser = true, speaker = "You") + judgeMessage
            )

            if (isAllowed) {
                // 2. Ask Witness
                val witnessPrompt = PromptTemplates.witnessCrossExam(
                    witnessName = "Witness ${_state.value.currentWitnessIndex}",
                    caseDescription = _state.value.caseDescription,
                    defendantName = _state.value.defendantName,
                    question = question
                )

                val witnessResult = llmEngine.generate(witnessPrompt)
                
                witnessResult.onSuccess { witnessResponse ->
                     val witnessMessage = Message(
                        content = witnessResponse,
                        isUser = false,
                        speaker = "Witness"
                    )
                    _state.value = _state.value.copy(
                        messages = _state.value.messages + witnessMessage,
                        isLoading = false
                    )
                }.onFailure {
                    _state.value = _state.value.copy(isLoading = false)
                }
            } else {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun parseAndStoreEvidence(evidenceResponse: String) {
        val evidenceList = mutableListOf<Evidence>()
        val evidencePattern = Regex("""EVIDENCE\s*\[?(\d+)\]?[:\s]*(.+?)(?:DESCRIPTION:|Description:)\s*(.+?)(?:SIGNIFICANCE:|Significance:)\s*(.+?)(?=EVIDENCE|\z)""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))

        evidencePattern.findAll(evidenceResponse).forEach { match: MatchResult ->
            val id = match.groupValues[1].toIntOrNull() ?: evidenceList.size + 1
            val name = match.groupValues[2].trim()
            val description = match.groupValues[3].trim()
            val significance = match.groupValues[4].trim()

            val evidence = Evidence(id, name, description, significance, "Prosecution")
            evidenceList.add(evidence)
            
            // Auto-tag as Fact
            addFact(com.charles.jurysim.data.model.Fact(
                id = "EV_$id",
                type = com.charles.jurysim.data.model.FactType.EVIDENCE,
                title = name,
                description = description,
                source = "Evidence Presentation"
            ))
        }

        if (evidenceList.isNotEmpty()) {
            _state.value = _state.value.copy(evidenceItems = evidenceList)
        }
    }

    fun getTrialMessages(): List<Message> = trialMessages

    fun goToMainMenu() {
        _state.value = SimulationState()
        voirDireQuestionCount = 0
        lastFailedOperation = null
        trialMessages = emptyList()
    }

    private fun getJudgeName(): String {
        return judgeProfile?.name?.takeIf { it.isNotBlank() }
            ?: _state.value.judgeName.takeIf { it.isNotBlank() }
            ?: "Judge Taylor"
    }

    private fun getJudgeDisplaySpeaker(): String = getJudgeName()

    private fun createJudgeProfile(): JudgeProfile {
        val femaleJudges = listOf(
            "Judge Elena Brooks",
            "Judge Maya Reynolds",
            "Judge Olivia Carter",
            "Judge Priya Singh",
            "Judge Hannah Foster",
            "Judge Lauren Ortiz"
        )
        val maleJudges = listOf(
            "Judge Marcus Bennett",
            "Judge Daniel Hayes",
            "Judge Victor Cole",
            "Judge Anthony Price",
            "Judge Samuel Turner",
            "Judge Adrian Wells"
        )
        val allJudges = femaleJudges.map { it to "FEMALE" } + maleJudges.map { it to "MALE" }
        val selected = allJudges.random()
        return JudgeProfile(
            name = selected.first,
            gender = selected.second,
            voiceSeed = kotlin.random.Random.nextInt()
        )
    }
}
