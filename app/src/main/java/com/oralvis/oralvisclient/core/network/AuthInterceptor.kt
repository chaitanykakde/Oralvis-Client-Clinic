package com.oralvis.oralvisclient.core.network

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException

/**
 * On 401, attempts token refresh once and retries the original request.
 * Skips refresh for the refresh-token endpoint itself to avoid loops.
 */
class AuthInterceptor(
    private val refreshClient: OkHttpClient,
    private val baseUrl: String
) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code != 401) return response

        val url = request.url
        val path = url.encodedPath
        if (path.contains("refresh-token")) return response

        response.close()

        val refreshRequest = Request.Builder()
            .url(baseUrl.trimEnd('/') + "/api/refresh-token")
            .post(okhttp3.RequestBody.create(null, ByteArray(0)))
            .build()

        val refreshResponse = refreshClient.newCall(refreshRequest).execute()
        if (!refreshResponse.isSuccessful) {
            refreshResponse.close()
            return chain.proceed(request.newBuilder().build())
        }
        refreshResponse.close()

        return chain.proceed(request.newBuilder().build())
    }
}
