package com.oralvis.oralvisclient.core.network

/**
 * Central place for API base URL and path prefixes.
 * Backend uses port 4000; replace host for staging/production.
 */
object NetworkConstants {
    const val BASE_URL = "https://your-api-host:4000/"
    const val API_PREFIX = "api"

    object Auth {
        const val COOKIE_ACCESS_TOKEN = "accessToken"
        const val COOKIE_REFRESH_TOKEN = "refreshToken"
    }
}
