package com.oralvis.oralvisclient.core.network

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.oralvis.oralvisclient.core.network.ApiResult.Error
import com.oralvis.oralvisclient.core.network.ValidationDetail
import okhttp3.ResponseBody
import java.io.IOException

/**
 * Maps HTTP error bodies to [ApiResult.Error].
 * Backend may use "error" or "message"; validation uses "details" array.
 */
object ErrorMapper {

    private val gson = Gson()

    fun map(responseBody: ResponseBody?, code: Int): Error {
        val body = responseBody?.string().orEmpty()
        if (body.isBlank()) {
            return Error(message = defaultMessageForCode(code), code = code)
        }
        return try {
            val parsed = gson.fromJson(body, ErrorBody::class.java)
            val message = parsed.message?.takeIf { it.isNotBlank() }
                ?: parsed.error?.takeIf { it.isNotBlank() }
                ?: defaultMessageForCode(code)
            val details = parsed.details?.map { d ->
                ValidationDetail(
                    field = d.field ?: "",
                    message = d.message ?: "",
                    value = d.value
                )
            }
            Error(message = message, code = code, validationDetails = details)
        } catch (e: Exception) {
            Error(message = body.ifBlank { defaultMessageForCode(code) }, code = code)
        }
    }

    fun map(throwable: Throwable): Error {
        val message = when (throwable) {
            is IOException -> "Network error. Please check your connection."
            else -> (throwable.message ?: "An unexpected error occurred")
        }
        return Error(message = message, code = null)
    }

    private fun defaultMessageForCode(code: Int): String = when (code) {
        400 -> "Bad request"
        401 -> "Session expired. Please sign in again."
        403 -> "You don't have permission to perform this action."
        404 -> "Resource not found"
        409 -> "Conflict (e.g. slot already taken)"
        429 -> "Too many attempts. Please try again later."
        503 -> "Service temporarily unavailable"
        500 -> "Server error. Please try again later."
        else -> "Something went wrong (code $code)"
    }
}

private data class ErrorBody(
    @SerializedName("success") val success: Boolean?,
    @SerializedName("message") val message: String?,
    @SerializedName("error") val error: String?,
    @SerializedName("details") val details: List<DetailItem>?
)

private data class DetailItem(
    @SerializedName("field") val field: String?,
    @SerializedName("message") val message: String?,
    @SerializedName("value") val value: Any?
)
