package com.oralvis.oralvisclient.core.network

/**
 * Sealed result for API calls. Repositories return this; ViewModels never see raw exceptions.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(
        val message: String,
        val code: Int? = null,
        val validationDetails: List<ValidationDetail>? = null
    ) : ApiResult<Nothing>()
}

/**
 * Validation error from backend (express-validator details array).
 */
data class ValidationDetail(
    val field: String,
    val message: String,
    val value: Any?
)
