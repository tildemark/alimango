package com.tildemark.alimango.data.repository

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.tildemark.alimango.BuildConfig
import com.tildemark.alimango.data.local.db.AppDatabase
import com.tildemark.alimango.data.remote.api.WaniKaniApiService
import com.tildemark.alimango.domain.model.User
import com.tildemark.alimango.domain.repository.UserRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import android.util.Log
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val apiService: WaniKaniApiService,
    private val database: AppDatabase
) : UserRepository {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
    
    private val prefs by lazy {
        EncryptedSharedPreferences.create(
            "alimango_secure_prefs",
            masterKeyAlias,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val keyPat = "wanikani_pat"
    private val keyUsername = "user_username"
    private val keyLevel = "user_level"
    private val keyProfileUrl = "user_profile_url"

    override suspend fun getOrFetchUser(pat: String): User? {
        // If PAT is provided, temporarily save it to try validating, then fetch
        val originalPat = getSavedPat()
        savePat(pat)
        return try {
            val response = apiService.getUser()
            val userDto = response.data
            // Cache user info locally in prefs
            prefs.edit().apply {
                putString(keyUsername, userDto.username)
                putInt(keyLevel, userDto.level)
                putString(keyProfileUrl, userDto.profileUrl)
                apply()
            }
            User(
                username = userDto.username,
                level = userDto.level,
                profileUrl = userDto.profileUrl
            )
        } catch (e: Exception) {
            Log.e("UserRepositoryImpl", "getOrFetchUser failed with exception: ", e)
            // Fallback to cached credentials if available offline
            val cachedUsername = prefs.getString(keyUsername, null)
            val cachedLevel = prefs.getInt(keyLevel, -1)
            val cachedProfileUrl = prefs.getString(keyProfileUrl, null)

            if (!cachedUsername.isNullOrEmpty() && cachedLevel != -1) {
                // Restore PAT if it was temporarily modified
                if (originalPat != null) {
                    savePat(originalPat)
                }
                User(
                    username = cachedUsername,
                    level = cachedLevel,
                    profileUrl = cachedProfileUrl ?: ""
                )
            } else {
                // Revert on failure
                if (originalPat != null) {
                    savePat(originalPat)
                } else {
                    prefs.edit().remove(keyPat).apply()
                }
                null
            }
        }
    }

    override suspend fun getSavedPat(): String? {
        val pat = prefs.getString(keyPat, null)
        if (pat.isNullOrBlank()) {
            // Fallback to BuildConfig if provided
            if (BuildConfig.WANIKANI_PAT.isNotBlank() && BuildConfig.WANIKANI_PAT != "PASTE_YOUR_TOKEN_HERE") {
                return BuildConfig.WANIKANI_PAT
            }
        }
        return pat
    }

    override suspend fun savePat(pat: String) {
        prefs.edit().putString(keyPat, pat).apply()
    }

    override suspend fun clearUserData() {
        prefs.edit().clear().apply()
        database.clearAllTables()
    }
}
