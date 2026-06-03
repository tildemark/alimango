package com.tildemark.alimango.data.repository

import com.tildemark.alimango.data.local.db.AssignmentDao
import com.tildemark.alimango.data.local.entity.AssignmentEntity
import com.tildemark.alimango.domain.model.Assignment
import com.tildemark.alimango.domain.repository.AssignmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepositoryImpl @Inject constructor(
    private val assignmentDao: AssignmentDao
) : AssignmentRepository {

    override fun observeAllAssignments(): Flow<List<Assignment>> {
        return assignmentDao.observeAllAssignments().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeAvailableReviews(currentTimeIso: String): Flow<List<Assignment>> {
        return assignmentDao.observeAvailableReviews(currentTimeIso).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAvailableReviews(currentTimeIso: String): List<Assignment> {
        return assignmentDao.getAvailableReviews(currentTimeIso).map { it.toDomain() }
    }

    override fun observeReviewsCount(currentTimeIso: String): Flow<Int> {
        return assignmentDao.observeReviewsCount(currentTimeIso)
    }

    override fun observeLessonsCount(): Flow<Int> {
        return assignmentDao.observeLessonsCount()
    }

    override suspend fun getAvailableLessons(): List<Assignment> {
        return assignmentDao.getAvailableLessons().map { it.toDomain() }
    }

    override suspend fun getAssignmentBySubjectId(subjectId: Int): Assignment? {
        return assignmentDao.getAssignmentBySubjectId(subjectId)?.toDomain()
    }

    override suspend fun saveAssignments(assignments: List<Assignment>) {
        val entities = assignments.map { it.toEntity() }
        assignmentDao.insertAssignments(entities)
    }

    override suspend fun saveNoteAndSynonyms(subjectId: Int, note: String, synonyms: List<String>) {
        val userSynonymsStr = synonyms.joinToString(",")
        assignmentDao.updateNoteAndSynonyms(subjectId, note, userSynonymsStr)
    }

    private fun AssignmentEntity.toDomain(): Assignment {
        return Assignment(
            id = id,
            subjectId = subjectId,
            subjectType = subjectType,
            srsStage = srsStage,
            unlockedAt = unlockedAt,
            availableAt = availableAt,
            burnedAt = burnedAt,
            startedAt = startedAt,
            passedAt = passedAt,
            userSynonyms = if (userSynonyms.isBlank()) emptyList() else userSynonyms.split(","),
            note = note
        )
    }

    private fun Assignment.toEntity(): AssignmentEntity {
        return AssignmentEntity(
            id = id,
            subjectId = subjectId,
            subjectType = subjectType,
            srsStage = srsStage,
            unlockedAt = unlockedAt,
            availableAt = availableAt,
            burnedAt = burnedAt,
            startedAt = startedAt,
            passedAt = passedAt,
            userSynonyms = userSynonyms.joinToString(","),
            note = note
        )
    }
}
