package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.Slot
import com.oralvis.oralvisclient.domain.repository.ClinicRepository

class GetSlotsUseCase(private val clinicRepository: ClinicRepository) {

    suspend operator fun invoke(clinicId: String, date: String): ApiResult<List<Slot>> =
        clinicRepository.getSlots(clinicId, date)
}
