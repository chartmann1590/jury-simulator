package com.jurysim.data.model

data class JurorProfile(
    val name: String = "",
    val age: Int = 30,
    val occupation: String = "",
    val education: String = "",
    val hasLegalExperience: Boolean = false,
    val hasBeenVictim: Boolean = false,
    val politicalLeaning: String = "Moderate",
    val additionalInfo: String = ""
)
