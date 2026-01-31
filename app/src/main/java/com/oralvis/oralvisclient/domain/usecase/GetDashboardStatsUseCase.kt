package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.DashboardStats
import com.oralvis.oralvisclient.domain.repository.ClinicRepository

class GetDashboardStatsUseCase(private val clinicRepository: ClinicRepository) {

    suspend operator fun invoke(clinicId: String): ApiResult<DashboardStats> =
        clinicRepository.getDashboardStats(clinicId)
}
