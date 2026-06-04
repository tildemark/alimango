package com.tildemark.alimango.data.work

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.tildemark.alimango.data.local.db.AssignmentDao
import com.tildemark.alimango.data.local.db.SubjectDao
import com.tildemark.alimango.data.local.db.SyncMetaDao
import com.tildemark.alimango.data.local.entity.AssignmentEntity
import com.tildemark.alimango.data.local.entity.SubjectEntity
import com.tildemark.alimango.data.local.entity.SyncMetaEntity
import com.tildemark.alimango.data.remote.api.WaniKaniApiService
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: WaniKaniApiService,
    private val subjectDao: SubjectDao,
    private val assignmentDao: AssignmentDao,
    private val syncMetaDao: SyncMetaDao,
    private val json: Json
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        try {
            Log.d("SyncWorker", "Starting sync process...")
            setProgress(workDataOf("status" to "syncing_subjects"))
            syncSubjects()

            setProgress(workDataOf("status" to "syncing_assignments"))
            syncAssignments()

            setProgress(workDataOf("status" to "success"))
            Log.d("SyncWorker", "Sync completed successfully!")
            return Result.success()
        } catch (e: Exception) {
            Log.e("SyncWorker", "Sync failed with error: ", e)
            setProgress(workDataOf("status" to "failed", "error" to e.localizedMessage))
            return Result.failure()
        }
    }

    private suspend fun syncSubjects() {
        val lastSynced = syncMetaDao.getLastSyncedAt("subjects")
        Log.d("SyncWorker", "Syncing subjects. Last synced timestamp: $lastSynced")

        var nextUrl: String? = null
        var lastSnapshotTime: String? = null
        var hasMore = true

        while (hasMore) {
            val response = apiService.getSubjects(updatedAfter = lastSynced, nextUrl = nextUrl)
            
            if (response.dataUpdatedAt != null) {
                lastSnapshotTime = response.dataUpdatedAt
            }
            
            val entities = response.data.map { resource ->
                val dto = resource.data
                SubjectEntity(
                    id = resource.id,
                    type = resource.objectType,
                    level = dto.level,
                    characters = dto.characters,
                    slug = dto.slug,
                    documentUrl = dto.documentUrl,
                    dataJson = json.encodeToString(dto)
                )
            }

            if (entities.isNotEmpty()) {
                subjectDao.insertSubjects(entities)
                Log.d("SyncWorker", "Inserted/updated ${entities.size} subjects")
            }

            nextUrl = response.pages.nextUrl
            hasMore = nextUrl != null
        }

        if (lastSnapshotTime != null) {
            syncMetaDao.insertSyncMeta(SyncMetaEntity("subjects", lastSnapshotTime))
        }
    }

    private suspend fun syncAssignments() {
        val sharedPrefs = applicationContext.getSharedPreferences("alimango_sync_prefs", Context.MODE_PRIVATE)
        val hasRunFullSyncCleanup = sharedPrefs.getBoolean("full_sync_cleanup_v2", false)
        
        var lastSynced = syncMetaDao.getLastSyncedAt("assignments")
        if (!hasRunFullSyncCleanup) {
            Log.d("SyncWorker", "Running one-time full sync cleanup to clear stale assignments")
            syncMetaDao.deleteSyncMeta("assignments")
            lastSynced = null
            sharedPrefs.edit().putBoolean("full_sync_cleanup_v2", true).apply()
        }

        Log.d("SyncWorker", "Syncing assignments. Last synced timestamp: $lastSynced")

        var nextUrl: String? = null
        var lastSnapshotTime: String? = null
        var hasMore = true
        val fetchedAssignmentIds = mutableSetOf<Int>()

        while (hasMore) {
            val response = apiService.getAssignments(updatedAfter = lastSynced, nextUrl = nextUrl)
            
            if (response.dataUpdatedAt != null) {
                lastSnapshotTime = response.dataUpdatedAt
            }

            val entities = response.data.map { resource ->
                val dto = resource.data
                val existing = assignmentDao.getAssignmentBySubjectId(dto.subjectId)
                fetchedAssignmentIds.add(resource.id)
                AssignmentEntity(
                    id = resource.id,
                    subjectId = dto.subjectId,
                    subjectType = dto.subjectType,
                    srsStage = dto.srsStage,
                    unlockedAt = dto.unlockedAt,
                    availableAt = dto.availableAt,
                    burnedAt = dto.burnedAt,
                    startedAt = dto.startedAt,
                    passedAt = dto.passedAt,
                    userSynonyms = dto.userSynonyms.joinToString(","),
                    note = existing?.note ?: ""
                )
            }

            if (entities.isNotEmpty()) {
                assignmentDao.insertAssignments(entities)
                Log.d("SyncWorker", "Inserted/updated ${entities.size} assignments")
            }

            nextUrl = response.pages.nextUrl
            hasMore = nextUrl != null
        }

        // If doing a full sync, clean up any assignments in local Room DB that do not exist in the remote API response
        if (lastSynced == null && fetchedAssignmentIds.isNotEmpty()) {
            val localAssignments = assignmentDao.getAllAssignmentsDirect()
            var deletedCount = 0
            var resetCount = 0
            for (local in localAssignments) {
                if (!fetchedAssignmentIds.contains(local.id)) {
                    if (local.note.isNotBlank() || local.userSynonyms.isNotBlank()) {
                        assignmentDao.resetAssignmentProgress(local.id)
                        resetCount++
                    } else {
                        assignmentDao.deleteAssignmentById(local.id)
                        deletedCount++
                    }
                }
            }
            Log.d("SyncWorker", "Stale assignment cleanup: deleted $deletedCount, reset progress for $resetCount due to level reset or deletion.")
        }

        if (lastSnapshotTime != null) {
            syncMetaDao.insertSyncMeta(SyncMetaEntity("assignments", lastSnapshotTime))
        }
    }
}
