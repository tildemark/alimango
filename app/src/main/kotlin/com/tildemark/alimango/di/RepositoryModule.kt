package com.tildemark.alimango.di

import com.tildemark.alimango.data.repository.AssignmentRepositoryImpl
import com.tildemark.alimango.data.repository.SubjectRepositoryImpl
import com.tildemark.alimango.data.repository.UserRepositoryImpl
import com.tildemark.alimango.domain.repository.AssignmentRepository
import com.tildemark.alimango.domain.repository.SubjectRepository
import com.tildemark.alimango.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindSubjectRepository(
        impl: SubjectRepositoryImpl
    ): SubjectRepository

    @Binds
    @Singleton
    abstract fun bindAssignmentRepository(
        impl: AssignmentRepositoryImpl
    ): AssignmentRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl
    ): UserRepository
}
