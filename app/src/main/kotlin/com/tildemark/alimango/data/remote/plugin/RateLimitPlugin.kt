package com.tildemark.alimango.data.remote.plugin

import android.util.Log
import io.ktor.client.plugins.api.createClientPlugin

val RateLimitPlugin = createClientPlugin("RateLimitPlugin") {
    onResponse { response ->
        val remaining = response.headers["RateLimit-Remaining"]?.toIntOrNull()
        val limit = response.headers["RateLimit-Limit"]?.toIntOrNull()
        
        if (remaining != null && limit != null) {
            Log.d("RateLimitPlugin", "Rate Limit: $remaining / $limit remaining")
            if (remaining < 10) {
                Log.w("RateLimitPlugin", "WARNING: Rate limit is running low! ($remaining left)")
            }
        }
    }
}
