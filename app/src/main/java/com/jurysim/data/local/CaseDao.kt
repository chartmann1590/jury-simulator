package com.jurysim.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CaseDao {
    @Query("SELECT * FROM cases ORDER BY completedTimestamp DESC")
    fun getAllCases(): Flow<List<CaseEntity>>

    @Query("SELECT * FROM cases WHERE id = :caseId")
    suspend fun getCaseById(caseId: Long): CaseEntity?

    @Insert
    suspend fun insertCase(caseEntity: CaseEntity): Long

    @Query("DELETE FROM cases WHERE id = :caseId")
    suspend fun deleteCase(caseId: Long)

    @Query("DELETE FROM cases")
    suspend fun deleteAllCases()

    @Query("SELECT COUNT(*) FROM cases")
    suspend fun getCaseCount(): Int
}
