package com.tildemark.alimango.di

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.tildemark.alimango.BuildConfig
import com.tildemark.alimango.data.remote.api.WaniKaniApiService
import com.tildemark.alimango.data.remote.plugin.AuthPlugin
import com.tildemark.alimango.data.remote.plugin.RateLimitPlugin
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            coerceInputValues = true
            prettyPrint = true
            encodeDefaults = true
        }
    }

    @Provides
    @Singleton
    fun provideHttpClient(
        @ApplicationContext context: Context,
        json: Json
    ): HttpClient {
        return HttpClient(Android) {
            install(ContentNegotiation) {
                json(json)
            }
            
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }

            install(AuthPlugin) {
                tokenProvider = {
                    try {
                        val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
                        val prefs = EncryptedSharedPreferences.create(
                            "alimango_secure_prefs",
                            masterKeyAlias,
                            context,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                        )
                        val token = prefs.getString("wanikani_pat", null)
                        if (token.isNullOrBlank() && BuildConfig.WANIKANI_PAT.isNotBlank() && BuildConfig.WANIKANI_PAT != "PASTE_YOUR_TOKEN_HERE") {
                            BuildConfig.WANIKANI_PAT
                        } else {
                            token
                        }
                    } catch (e: Exception) {
                        if (BuildConfig.WANIKANI_PAT.isNotBlank() && BuildConfig.WANIKANI_PAT != "PASTE_YOUR_TOKEN_HERE") {
                            BuildConfig.WANIKANI_PAT
                        } else {
                            null
                        }
                    }
                }
            }

            install(RateLimitPlugin)
        }
    }

    @Provides
    @Singleton
    fun provideWaniKaniApiService(client: HttpClient): WaniKaniApiService {
        return WaniKaniApiService(client)
    }
}
