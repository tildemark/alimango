package com.tildemark.alimango.data.remote.api

import com.tildemark.alimango.data.remote.dto.AssignmentDto
import com.tildemark.alimango.data.remote.dto.SubjectDto
import com.tildemark.alimango.data.remote.dto.UserDto
import com.tildemark.alimango.data.remote.dto.WaniKaniCollection
import com.tildemark.alimango.data.remote.dto.WaniKaniResource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class WaniKaniApiService(private val client: HttpClient) {

    private val baseUrl = "https://api.wanikani.com/v2"

    suspend fun getUser(): WaniKaniResource<UserDto> {
        return client.get("$baseUrl/user").body()
    }

    suspend fun getSubjects(
        updatedAfter: String? = null,
        nextUrl: String? = null
    ): WaniKaniCollection<SubjectDto> {
        val url = nextUrl ?: "$baseUrl/subjects"
        return client.get(url) {
            if (nextUrl == null && updatedAfter != null) {
                parameter("updated_after", updatedAfter)
            }
        }.body()
    }

    suspend fun getAssignments(
        updatedAfter: String? = null,
        nextUrl: String? = null,
        immediatelyAvailableForReview: Boolean? = null
    ): WaniKaniCollection<AssignmentDto> {
        val url = nextUrl ?: "$baseUrl/assignments"
        return client.get(url) {
            if (nextUrl == null) {
                if (updatedAfter != null) {
                    parameter("updated_after", updatedAfter)
                }
                if (immediatelyAvailableForReview != null) {
                    parameter("immediately_available_for_review", immediatelyAvailableForReview)
                }
            }
        }.body()
    }

    suspend fun createReview(
        subjectId: Int,
        incorrectMeaningAnswers: Int,
        incorrectReadingAnswers: Int
    ): WaniKaniResource<AssignmentDto> {
        return client.post("$baseUrl/reviews") {
            contentType(ContentType.Application.Json)
            setBody(
                ReviewRequest(
                    review = ReviewDetails(
                        subjectId = subjectId,
                        incorrectMeaningAnswers = incorrectMeaningAnswers,
                        incorrectReadingAnswers = incorrectReadingAnswers
                    )
                )
            )
        }.body()
    }

    suspend fun startAssignment(assignmentId: Int): WaniKaniResource<AssignmentDto> {
        return client.put("$baseUrl/assignments/$assignmentId/start") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }.body()
    }

    @Serializable
    private data class ReviewRequest(
        @SerialName("review") val review: ReviewDetails
    )

    @Serializable
    private data class ReviewDetails(
        @SerialName("subject_id") val subjectId: Int,
        @SerialName("incorrect_meaning_answers") val incorrectMeaningAnswers: Int,
        @SerialName("incorrect_reading_answers") val incorrectReadingAnswers: Int
    )
}
