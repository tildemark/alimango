package com.tildemark.alimango.domain.usecase

import android.content.Context
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

sealed interface SyncStatus {
    object Idle : SyncStatus
    object SyncingSubjects : SyncStatus
    object SyncingAssignments : SyncStatus
    object Success : SyncStatus
    data class Failed(val error: String?) : SyncStatus
}

class ObserveSyncStatusUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    operator fun invoke(): Flow<SyncStatus> {
        return WorkManager.getInstance(context)
            .getWorkInfosForUniqueWorkFlow("alimango_sync")
            .map { workInfos ->
                val workInfo = workInfos.firstOrNull() ?: return@map SyncStatus.Idle
                
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        val progressStatus = workInfo.progress.getString("status")
                        when (progressStatus) {
                            "syncing_subjects" -> SyncStatus.SyncingSubjects
                            "syncing_assignments" -> SyncStatus.SyncingAssignments
                            else -> SyncStatus.SyncingSubjects
                        }
                    }
                    WorkInfo.State.SUCCEEDED -> SyncStatus.Success
                    WorkInfo.State.FAILED -> {
                        val error = workInfo.progress.getString("error")
                        SyncStatus.Failed(error)
                    }
                    WorkInfo.State.ENQUEUED -> SyncStatus.Idle
                    else -> SyncStatus.Idle
                }
            }
    }
}
