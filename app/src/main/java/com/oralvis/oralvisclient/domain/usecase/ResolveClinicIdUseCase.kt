package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.repository.ClinicRepository

class ResolveClinicIdUseCase(private val clinicRepository: ClinicRepository) {

    suspend operator fun invoke(userId: String): ApiResult<String> = clinicRepository.getClinicId(userId)
}
