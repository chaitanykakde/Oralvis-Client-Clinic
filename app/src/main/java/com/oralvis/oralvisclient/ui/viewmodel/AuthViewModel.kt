package com.oralvis.oralvisclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.core.util.DispatcherProvider
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.core.util.ValidationError
import com.oralvis.oralvisclient.domain.model.User
import com.oralvis.oralvisclient.domain.usecase.GetCurrentUserUseCase
import com.oralvis.oralvisclient.domain.usecase.LoginUseCase
import com.oralvis.oralvisclient.domain.usecase.LogoutUseCase
import com.oralvis.oralvisclient.domain.usecase.RefreshTokenUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    private val _currentUserState = MutableStateFlow<UiState<User>>(UiState.Loading)
    val currentUserState: StateFlow<UiState<User>> = _currentUserState.asStateFlow()

    fun login(phoneNo: String?, email: String?, password: String) {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = loginUseCase(phoneNo, email, password)) {
                    is ApiResult.Success -> _loginState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _loginState.value = UiState.Error(
                        result.message,
                        result.validationDetails?.map { ValidationError(it.field, it.message) }
                    )
                }
            }
        }
    }

    fun refreshToken() {
        viewModelScope.launch {
            withContext(dispatcherProvider.io) {
                when (val result = refreshTokenUseCase()) {
                    is ApiResult.Success -> { /* tokens refreshed */ }
                    is ApiResult.Error -> { /* caller may trigger logout on 401 */ }
                }
            }
        }
    }

    fun loadCurrentUser() {
        viewModelScope.launch {
            _currentUserState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = getCurrentUserUseCase()) {
                    is ApiResult.Success -> _currentUserState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _currentUserState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            withContext(dispatcherProvider.io) {
                logoutUseCase()
            }
        }
    }
}
