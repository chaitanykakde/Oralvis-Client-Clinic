package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.User
import com.oralvis.oralvisclient.domain.repository.AuthRepository

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(): ApiResult<User> = authRepository.getCurrentUser()
}
