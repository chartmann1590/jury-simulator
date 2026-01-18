package com.jurysim.data.model

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
    val source: String, // e.g., "Witness 1", "Opening Statement"
    var userNotes: String = ""
)
