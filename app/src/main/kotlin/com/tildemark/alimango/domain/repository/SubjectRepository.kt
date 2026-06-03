package com.tildemark.alimango.domain.repository

import com.tildemark.alimango.domain.model.Subject
import kotlinx.coroutines.flow.Flow

interface SubjectRepository {
    fun observeAllSubjects(): Flow<List<Subject>>
    fun observeSubjectsByType(type: String): Flow<List<Subject>>
    fun observeSubjectsByLevel(level: Int): Flow<List<Subject>>
    suspend fun getSubjectById(id: Int): Subject?
    suspend fun getSubjectsByIds(ids: List<Int>): List<Subject>
    suspend fun saveSubjects(subjects: List<Subject>)
}
