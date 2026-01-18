package com.jurysim.data.model

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
    // Voting system fields
    val aiJurors: List<AIJuror> = emptyList(),
    val evidenceItems: List<Evidence> = emptyList(),
    val currentVotingRound: Int = 0,
    val maxVotingRounds: Int = 5,
    val isVotingPhase: Boolean = false,
    val isMistrial: Boolean = false,
    val lastVoteResults: Map<Int, VoteChoice> = emptyMap(),
    // Deliberation messages by juror for individual chat
    val jurorConversations: Map<Int, List<Message>> = emptyMap(),
    // Current juror being chatted with (-1 for group chat)
    val currentJurorChatId: Int = -1,
    // Interactive Notebook
    val facts: List<Fact> = emptyList(),
    val showNotebook: Boolean = false
)
