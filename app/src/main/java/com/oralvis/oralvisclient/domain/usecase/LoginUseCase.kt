package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.User
import com.oralvis.oralvisclient.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(
        phoneNo: String?,
        email: String?,
        password: String
    ): ApiResult<User> = authRepository.login(phoneNo, email, password)
}
