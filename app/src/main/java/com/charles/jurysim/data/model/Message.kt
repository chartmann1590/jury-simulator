package com.charles.jurysim.data.model

data class Message(
    val content: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val speaker: String? = null // For trial phases: "Judge", "Prosecutor", "Defense", "Witness", etc.
)
