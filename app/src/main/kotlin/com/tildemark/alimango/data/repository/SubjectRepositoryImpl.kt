package com.tildemark.alimango.data.repository

import com.tildemark.alimango.data.local.db.SubjectDao
import com.tildemark.alimango.data.local.entity.SubjectEntity
import com.tildemark.alimango.data.remote.dto.SubjectDto
import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.domain.repository.SubjectRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubjectRepositoryImpl @Inject constructor(
    private val subjectDao: SubjectDao,
    private val json: Json
) : SubjectRepository {

    override fun observeAllSubjects(): Flow<List<Subject>> {
        return subjectDao.observeAllSubjects().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeSubjectsByType(type: String): Flow<List<Subject>> {
        return subjectDao.observeSubjectsByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeSubjectsByLevel(level: Int): Flow<List<Subject>> {
        return subjectDao.observeSubjectsByLevel(level).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getSubjectById(id: Int): Subject? {
        return subjectDao.getSubjectById(id)?.toDomain()
    }

    override suspend fun getSubjectsByIds(ids: List<Int>): List<Subject> {
        return subjectDao.getSubjectsByIds(ids).map { it.toDomain() }
    }

    override suspend fun saveSubjects(subjects: List<Subject>) {
        // Note: For delta updates we typically save through the sync worker, but this is a useful helper.
        // In the data layer, we save DTOs or Entities. 
    }

    private fun SubjectEntity.toDomain(): Subject {
        val dto = try {
            json.decodeFromString<SubjectDto>(this.dataJson)
        } catch (e: Exception) {
            SubjectDto(createdAt = "", level = this.level, slug = this.slug, documentUrl = this.documentUrl)
        }
        return Subject(
            id = id,
            type = type,
            level = level,
            characters = characters,
            meanings = dto.meanings.map { it.meaning },
            readings = dto.readings.map { it.reading },
            partsOfSpeech = dto.partsOfSpeech,
            audioUrls = dto.pronunciationAudios.map { it.url },
            documentUrl = documentUrl,
            meaningMnemonic = dto.meaningMnemonic,
            readingMnemonic = dto.readingMnemonic
        )
    }
}
