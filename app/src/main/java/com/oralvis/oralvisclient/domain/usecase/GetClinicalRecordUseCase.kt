package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.ClinicalRecord
import com.oralvis.oralvisclient.domain.repository.ClinicalRepository

class GetClinicalRecordUseCase(private val clinicalRepository: ClinicalRepository) {

    suspend operator fun invoke(bookingId: String): ApiResult<ClinicalRecord?> =
        clinicalRepository.getClinicalRecord(bookingId)
}
