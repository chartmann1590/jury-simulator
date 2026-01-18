package com.jurysim.data.model

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
