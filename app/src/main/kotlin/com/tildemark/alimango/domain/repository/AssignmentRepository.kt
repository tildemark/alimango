package com.tildemark.alimango.domain.repository

import com.tildemark.alimango.domain.model.Assignment
import kotlinx.coroutines.flow.Flow

interface AssignmentRepository {
    fun observeAllAssignments(userLevel: Int = 999): Flow<List<Assignment>>
    fun observeAvailableReviews(currentTimeIso: String, userLevel: Int = 999): Flow<List<Assignment>>
    suspend fun getAvailableReviews(currentTimeIso: String, userLevel: Int = 999): List<Assignment>
    fun observeReviewsCount(currentTimeIso: String, userLevel: Int = 999): Flow<Int>
    fun observeLessonsCount(userLevel: Int = 999): Flow<Int>
    suspend fun getAvailableLessons(userLevel: Int = 999): List<Assignment>
    suspend fun getAssignmentBySubjectId(subjectId: Int): Assignment?
    suspend fun saveAssignments(assignments: List<Assignment>)
    suspend fun saveNoteAndSynonyms(subjectId: Int, note: String, synonyms: List<String>)
}
