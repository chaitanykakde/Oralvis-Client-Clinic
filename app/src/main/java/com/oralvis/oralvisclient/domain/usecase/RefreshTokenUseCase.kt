package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.repository.AuthRepository

class RefreshTokenUseCase(private val authRepository: AuthRepository) {

    suspend operator fun invoke(): ApiResult<Unit> = authRepository.refreshToken()
}
