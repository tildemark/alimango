package com.tildemark.alimango.di

import android.content.Context
import androidx.room.Room
import com.tildemark.alimango.data.local.db.AppDatabase
import com.tildemark.alimango.data.local.db.AssignmentDao
import com.tildemark.alimango.data.local.db.SubjectDao
import com.tildemark.alimango.data.local.db.SyncMetaDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "alimango.db"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideSubjectDao(database: AppDatabase): SubjectDao {
        return database.subjectDao()
    }

    @Provides
    fun provideAssignmentDao(database: AppDatabase): AssignmentDao {
        return database.assignmentDao()
    }

    @Provides
    fun provideSyncMetaDao(database: AppDatabase): SyncMetaDao {
        return database.syncMetaDao()
    }
}
