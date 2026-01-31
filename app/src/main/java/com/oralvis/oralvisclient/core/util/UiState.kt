package com.oralvis.oralvisclient.core.util

/**
 * UI state for screens: Loading, Success with data, or Error with message.
 * ViewModels expose this; no raw exceptions.
 */
sealed class UiState<out T> {
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String, val validationDetails: List<ValidationError>? = null) : UiState<Nothing>()
}

data class ValidationError(val field: String, val message: String)
