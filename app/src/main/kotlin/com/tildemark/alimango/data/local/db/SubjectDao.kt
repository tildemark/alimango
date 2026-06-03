package com.tildemark.alimango.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tildemark.alimango.data.local.entity.SubjectEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubjectDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubjects(subjects: List<SubjectEntity>)

    @Query("SELECT * FROM subjects WHERE id = :id")
    suspend fun getSubjectById(id: Int): SubjectEntity?

    @Query("SELECT * FROM subjects WHERE id IN (:ids)")
    suspend fun getSubjectsByIds(ids: List<Int>): List<SubjectEntity>

    @Query("SELECT * FROM subjects")
    fun observeAllSubjects(): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE type = :type")
    fun observeSubjectsByType(type: String): Flow<List<SubjectEntity>>

    @Query("SELECT * FROM subjects WHERE level = :level")
    fun observeSubjectsByLevel(level: Int): Flow<List<SubjectEntity>>

    @Query("SELECT COUNT(*) FROM subjects")
    suspend fun getSubjectsCount(): Int
}
