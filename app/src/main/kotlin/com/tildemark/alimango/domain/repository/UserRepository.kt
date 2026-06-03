package com.tildemark.alimango.domain.repository

import com.tildemark.alimango.domain.model.User

interface UserRepository {
    suspend fun getOrFetchUser(pat: String): User?
    suspend fun getSavedPat(): String?
    suspend fun savePat(pat: String)
    suspend fun clearUserData()
}
