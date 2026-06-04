package com.tildemark.alimango.domain.usecase

import com.tildemark.alimango.domain.repository.AssignmentRepository
import com.tildemark.alimango.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.Instant
import java.time.format.DateTimeFormatter
import android.util.Log
import javax.inject.Inject

data class DashboardSummary(
    val reviewCount: Int,
    val lessonCount: Int,
    val apprenticeCount: Int,
    val guruCount: Int,
    val masterCount: Int,
    val enlightenedCount: Int,
    val burnedCount: Int,
    val hourlyForecast: List<Int>
)

class GetDashboardSummaryUseCase @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    private val userRepository: UserRepository
) {
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<DashboardSummary> {
        return flow {
            val pat = userRepository.getSavedPat() ?: ""
            val user = userRepository.getOrFetchUser(pat)
            val userLevel = user?.level ?: 1
            emit(userLevel)
        }.flatMapLatest { userLevel ->
            val currentTimeIso = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            
            combine(
                assignmentRepository.observeReviewsCount(currentTimeIso, userLevel),
                assignmentRepository.observeLessonsCount(userLevel),
                assignmentRepository.observeAllAssignments(userLevel)
            ) { reviewsCount, lessonsCount, assignments ->
                var apprentice = 0
                var guru = 0
                var master = 0
                var enlightened = 0
                var burned = 0

                val forecast = IntArray(24)
                val now = Instant.now()

                val lessonAssignments = assignments.filter { it.unlockedAt != null && it.startedAt == null }
                Log.d("DashboardSummary", "DB Lessons count: ${lessonAssignments.size}")
                lessonAssignments.forEach {
                    Log.d("DashboardSummary", "Lesson assignment: subjectId=${it.subjectId}, type=${it.subjectType}, srsStage=${it.srsStage}, unlockedAt=${it.unlockedAt}, startedAt=${it.startedAt}")
                }

                assignments.forEach { assignment ->
                    when (assignment.srsStage) {
                        in 1..4 -> apprentice++
                        in 5..6 -> guru++
                        7 -> master++
                        8 -> enlightened++
                        9 -> burned++
                    }

                    // Calculate upcoming forecast (only started, unburned, future items)
                    val availableAtStr = assignment.availableAt
                    if (availableAtStr != null && assignment.startedAt != null && assignment.burnedAt == null) {
                        try {
                            val availableInstant = Instant.parse(availableAtStr)
                            val duration = java.time.Duration.between(now, availableInstant)
                            val hours = duration.toHours().toInt()
                            if (hours in 0..23) {
                                forecast[hours]++
                            }
                        } catch (e: Exception) {
                            // ignore format parsing exception
                        }
                    }
                }

                DashboardSummary(
                    reviewCount = reviewsCount,
                    lessonCount = lessonsCount,
                    apprenticeCount = apprentice,
                    guruCount = guru,
                    masterCount = master,
                    enlightenedCount = enlightened,
                    burnedCount = burned,
                    hourlyForecast = forecast.toList()
                )
            }
        }
    }
}
