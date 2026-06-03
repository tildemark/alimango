package com.tildemark.alimango.domain.repository

import com.tildemark.alimango.domain.model.Assignment
import kotlinx.coroutines.flow.Flow

interface AssignmentRepository {
    fun observeAllAssignments(): Flow<List<Assignment>>
    fun observeAvailableReviews(currentTimeIso: String): Flow<List<Assignment>>
    suspend fun getAvailableReviews(currentTimeIso: String): List<Assignment>
    fun observeReviewsCount(currentTimeIso: String): Flow<Int>
    fun observeLessonsCount(): Flow<Int>
    suspend fun getAvailableLessons(): List<Assignment>
    suspend fun getAssignmentBySubjectId(subjectId: Int): Assignment?
    suspend fun saveAssignments(assignments: List<Assignment>)
    suspend fun saveNoteAndSynonyms(subjectId: Int, note: String, synonyms: List<String>)
}
