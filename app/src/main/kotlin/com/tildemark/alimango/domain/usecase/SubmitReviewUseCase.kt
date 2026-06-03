package com.tildemark.alimango.domain.usecase

import com.tildemark.alimango.data.remote.api.WaniKaniApiService
import com.tildemark.alimango.domain.model.Assignment
import com.tildemark.alimango.domain.repository.AssignmentRepository
import javax.inject.Inject

class SubmitReviewUseCase @Inject constructor(
    private val apiService: WaniKaniApiService,
    private val assignmentRepository: AssignmentRepository
) {
    suspend operator fun invoke(
        subjectId: Int,
        incorrectMeanings: Int,
        incorrectReadings: Int
    ): Boolean {
        return try {
            val response = apiService.createReview(
                subjectId = subjectId,
                incorrectMeaningAnswers = incorrectMeanings,
                incorrectReadingAnswers = incorrectReadings
            )
            
            // Save updated assignment info locally
            val updatedDto = response.data
            val domainAssignment = Assignment(
                id = response.id,
                subjectId = updatedDto.subjectId,
                subjectType = updatedDto.subjectType,
                srsStage = updatedDto.srsStage,
                unlockedAt = updatedDto.unlockedAt,
                availableAt = updatedDto.availableAt,
                burnedAt = updatedDto.burnedAt,
                startedAt = updatedDto.startedAt,
                passedAt = updatedDto.passedAt
            )
            
            assignmentRepository.saveAssignments(listOf(domainAssignment))
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
