package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.repository.ClinicRepository

class CancelBookingsByDateUseCase(private val clinicRepository: ClinicRepository) {

    suspend operator fun invoke(clinicId: String, date: String): ApiResult<Int> =
        clinicRepository.cancelBookingsByDate(clinicId, date)
}
