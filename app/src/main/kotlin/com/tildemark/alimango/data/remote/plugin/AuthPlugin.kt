package com.tildemark.alimango.data.remote.plugin

import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.header

class AuthPluginConfig {
    var tokenProvider: () -> String? = { null }
}

val AuthPlugin = createClientPlugin("AuthPlugin", ::AuthPluginConfig) {
    val tokenProvider = pluginConfig.tokenProvider
    
    onRequest { request, _ ->
        val token = tokenProvider()
        if (!token.isNullOrBlank()) {
            request.header("Authorization", "Bearer $token")
        }
    }
}
