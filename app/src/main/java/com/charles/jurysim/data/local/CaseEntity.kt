package com.charles.jurysim.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

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
