package com.oralvis.oralvisclient.core.network

import com.oralvis.oralvisclient.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Provides Retrofit and OkHttp clients with cookie-based auth and 401 refresh retry.
 * Backend expects cookies (accessToken, refreshToken); AuthInterceptor retries once after refresh.
 */
object ApiClient {

    private val cookieJar = CookieJarImpl()

    private val gson = GsonBuilder()
        .setLenient()
        .serializeNulls()
        .create()

    /** Client used only for refresh-token call (no AuthInterceptor to avoid loop). */
    private val refreshOkHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                }
            }
            .build()
    }

    private val authInterceptor = AuthInterceptor(refreshOkHttpClient, NetworkConstants.BASE_URL)

    val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .apply {
                if (BuildConfig.DEBUG) {
                    addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                }
            }
            .build()
    }

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(NetworkConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /** Clear cookies (e.g. on logout). */
    fun clearCookies() {
        cookieJar.clearAll()
    }

    fun <T> create(service: Class<T>): T = retrofit.create(service)
}
