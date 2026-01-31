package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.Booking
import com.oralvis.oralvisclient.domain.repository.ClinicRepository

class GetAppointmentsUseCase(private val clinicRepository: ClinicRepository) {

    suspend operator fun invoke(clinicId: String): ApiResult<List<Booking>> =
        clinicRepository.getAppointments(clinicId)
}
