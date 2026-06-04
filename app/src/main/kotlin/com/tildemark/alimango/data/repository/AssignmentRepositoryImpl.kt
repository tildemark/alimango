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

    override fun observeAllAssignments(userLevel: Int): Flow<List<Assignment>> {
        return assignmentDao.observeAllAssignments(userLevel).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeAvailableReviews(currentTimeIso: String, userLevel: Int): Flow<List<Assignment>> {
        return assignmentDao.observeAvailableReviews(currentTimeIso, userLevel).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getAvailableReviews(currentTimeIso: String, userLevel: Int): List<Assignment> {
        return assignmentDao.getAvailableReviews(currentTimeIso, userLevel).map { it.toDomain() }
    }

    override fun observeReviewsCount(currentTimeIso: String, userLevel: Int): Flow<Int> {
        return assignmentDao.observeReviewsCount(currentTimeIso, userLevel)
    }

    override fun observeLessonsCount(userLevel: Int): Flow<Int> {
        return assignmentDao.observeLessonsCount(userLevel)
    }

    override suspend fun getAvailableLessons(userLevel: Int): List<Assignment> {
        return assignmentDao.getAvailableLessons(userLevel).map { it.toDomain() }
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
