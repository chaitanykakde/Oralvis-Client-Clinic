package com.oralvis.oralvisclient.core.network

/**
 * Central place for API path prefixes and auth cookie names.
 * Base URL comes from BuildConfig.BASE_URL (debug: local, release: production).
 */
object NetworkConstants {
    const val API_PREFIX = "api"

    object Auth {
        const val COOKIE_ACCESS_TOKEN = "accessToken"
        const val COOKIE_REFRESH_TOKEN = "refreshToken"
    }
}
