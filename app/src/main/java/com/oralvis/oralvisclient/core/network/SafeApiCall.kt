package com.oralvis.oralvisclient.core.network

import retrofit2.Response

/**
 * Executes a Retrofit call and maps to ApiResult. Handles body/error parsing.
 * Returns Success(response.body()) or Error from error body / exception.
 */
suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
    return try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                ApiResult.Success(body)
            } else {
                ApiResult.Error(message = "Empty response", code = response.code())
            }
        } else {
            ErrorMapper.map(response.errorBody(), response.code())
        }
    } catch (e: Exception) {
        ErrorMapper.map(e)
    }
}
