package com.tildemark.alimango.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tildemark.alimango.data.local.entity.AssignmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssignmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAssignments(assignments: List<AssignmentEntity>)

    @Query("SELECT * FROM assignments WHERE subjectId = :subjectId")
    suspend fun getAssignmentBySubjectId(subjectId: Int): AssignmentEntity?

    @Query("SELECT * FROM assignments")
    fun observeAllAssignments(): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE availableAt IS NOT NULL AND availableAt <= :currentTimeIso")
    fun observeAvailableReviews(currentTimeIso: String): Flow<List<AssignmentEntity>>

    @Query("SELECT * FROM assignments WHERE availableAt IS NOT NULL AND availableAt <= :currentTimeIso")
    suspend fun getAvailableReviews(currentTimeIso: String): List<AssignmentEntity>

    @Query("SELECT COUNT(*) FROM assignments WHERE availableAt IS NOT NULL AND availableAt <= :currentTimeIso")
    fun observeReviewsCount(currentTimeIso: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM assignments WHERE unlockedAt IS NOT NULL AND startedAt IS NULL")
    fun observeLessonsCount(): Flow<Int>

    @Query("SELECT * FROM assignments WHERE unlockedAt IS NOT NULL AND startedAt IS NULL")
    suspend fun getAvailableLessons(): List<AssignmentEntity>

    @Query("UPDATE assignments SET note = :note, userSynonyms = :userSynonyms WHERE subjectId = :subjectId")
    suspend fun updateNoteAndSynonyms(subjectId: Int, note: String, userSynonyms: String)
}
