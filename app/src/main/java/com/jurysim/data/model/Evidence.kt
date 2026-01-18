package com.jurysim.data.model

data class Evidence(
    val id: Int,
    val name: String,
    val description: String,
    val significance: String,
    val presentedBy: String // "Prosecution" or "Defense"
)
