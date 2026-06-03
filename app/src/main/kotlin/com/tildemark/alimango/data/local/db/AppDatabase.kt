package com.tildemark.alimango.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tildemark.alimango.data.local.entity.AssignmentEntity
import com.tildemark.alimango.data.local.entity.SubjectEntity
import com.tildemark.alimango.data.local.entity.SyncMetaEntity

@Database(
    entities = [
        SubjectEntity::class,
        AssignmentEntity::class,
        SyncMetaEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun assignmentDao(): AssignmentDao
    abstract fun syncMetaDao(): SyncMetaDao
}
