package com.tildemark.alimango.domain.usecase

import com.tildemark.alimango.data.remote.api.WaniKaniApiService
import com.tildemark.alimango.domain.model.Assignment
import com.tildemark.alimango.domain.repository.AssignmentRepository
import javax.inject.Inject

class StartAssignmentUseCase @Inject constructor(
    private val apiService: WaniKaniApiService,
    private val assignmentRepository: AssignmentRepository
) {
    suspend operator fun invoke(assignmentId: Int): Boolean {
        return try {
            val response = apiService.startAssignment(assignmentId)
            val dto = response.data
            val assignment = Assignment(
                id = response.id,
                subjectId = dto.subjectId,
                subjectType = dto.subjectType,
                srsStage = dto.srsStage,
                unlockedAt = dto.unlockedAt,
                availableAt = dto.availableAt,
                burnedAt = dto.burnedAt,
                startedAt = dto.startedAt,
                passedAt = dto.passedAt
            )
            assignmentRepository.saveAssignments(listOf(assignment))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
