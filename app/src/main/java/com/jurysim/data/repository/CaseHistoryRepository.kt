package com.jurysim.data.repository

import com.jurysim.data.local.CaseDao
import com.jurysim.data.local.CaseEntity
import kotlinx.coroutines.flow.Flow

class CaseHistoryRepository(private val caseDao: CaseDao) {

    fun getAllCases(): Flow<List<CaseEntity>> {
        return caseDao.getAllCases()
    }

    suspend fun getCaseById(caseId: Long): CaseEntity? {
        return caseDao.getCaseById(caseId)
    }

    suspend fun saveCase(
        caseTitle: String,
        defendantName: String,
        charges: String,
        caseDescription: String,
        verdict: String,
        wasJurySelected: Boolean,
        modelUsed: String
    ): Long {
        val case = CaseEntity(
            caseTitle = caseTitle,
            defendantName = defendantName,
            charges = charges,
            caseDescription = caseDescription,
            verdict = verdict,
            wasJurySelected = wasJurySelected,
            completedTimestamp = System.currentTimeMillis(),
            modelUsed = modelUsed
        )
        return caseDao.insertCase(case)
    }

    suspend fun deleteCase(caseId: Long) {
        caseDao.deleteCase(caseId)
    }

    suspend fun deleteAllCases() {
        caseDao.deleteAllCases()
    }

    suspend fun getCaseCount(): Int {
        return caseDao.getCaseCount()
    }
}
