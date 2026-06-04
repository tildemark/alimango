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

    @Query("""
        SELECT assignments.* FROM assignments 
        JOIN subjects ON assignments.subjectId = subjects.id 
        WHERE subjects.level <= :userLevel
    """)
    fun observeAllAssignments(userLevel: Int): Flow<List<AssignmentEntity>>

    @Query("""
        SELECT assignments.* FROM assignments 
        JOIN subjects ON assignments.subjectId = subjects.id 
        WHERE assignments.availableAt IS NOT NULL 
          AND assignments.availableAt <= :currentTimeIso 
          AND subjects.level <= :userLevel
    """)
    fun observeAvailableReviews(currentTimeIso: String, userLevel: Int): Flow<List<AssignmentEntity>>

    @Query("""
        SELECT assignments.* FROM assignments 
        JOIN subjects ON assignments.subjectId = subjects.id 
        WHERE assignments.availableAt IS NOT NULL 
          AND assignments.availableAt <= :currentTimeIso 
          AND subjects.level <= :userLevel
    """)
    suspend fun getAvailableReviews(currentTimeIso: String, userLevel: Int): List<AssignmentEntity>

    @Query("""
        SELECT COUNT(assignments.id) FROM assignments 
        JOIN subjects ON assignments.subjectId = subjects.id 
        WHERE assignments.availableAt IS NOT NULL 
          AND assignments.availableAt <= :currentTimeIso 
          AND subjects.level <= :userLevel
    """)
    fun observeReviewsCount(currentTimeIso: String, userLevel: Int): Flow<Int>

    @Query("""
        SELECT COUNT(assignments.id) FROM assignments 
        JOIN subjects ON assignments.subjectId = subjects.id 
        WHERE assignments.unlockedAt IS NOT NULL 
          AND assignments.startedAt IS NULL 
          AND subjects.level <= :userLevel
    """)
    fun observeLessonsCount(userLevel: Int): Flow<Int>

    @Query("""
        SELECT assignments.* FROM assignments 
        JOIN subjects ON assignments.subjectId = subjects.id 
        WHERE assignments.unlockedAt IS NOT NULL 
          AND assignments.startedAt IS NULL 
          AND subjects.level <= :userLevel
    """)
    suspend fun getAvailableLessons(userLevel: Int): List<AssignmentEntity>

    @Query("UPDATE assignments SET note = :note, userSynonyms = :userSynonyms WHERE subjectId = :subjectId")
    suspend fun updateNoteAndSynonyms(subjectId: Int, note: String, userSynonyms: String)

    @Query("DELETE FROM assignments WHERE id = :id")
    suspend fun deleteAssignmentById(id: Int)

    @Query("""
        UPDATE assignments 
        SET unlockedAt = null, startedAt = null, srsStage = 0, availableAt = null, burnedAt = null, passedAt = null 
        WHERE id = :id
    """)
    suspend fun resetAssignmentProgress(id: Int)

    @Query("SELECT * FROM assignments")
    suspend fun getAllAssignmentsDirect(): List<AssignmentEntity>
}
