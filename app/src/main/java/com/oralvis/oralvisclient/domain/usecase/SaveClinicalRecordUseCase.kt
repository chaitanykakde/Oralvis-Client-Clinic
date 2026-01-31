package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.ClinicalRecord
import com.oralvis.oralvisclient.domain.repository.ClinicalRepository

class SaveClinicalRecordUseCase(private val clinicalRepository: ClinicalRepository) {

    suspend operator fun invoke(
        bookingId: String,
        complaints: Any? = null,
        observations: Any? = null,
        diagnoses: List<String>? = null,
        notes: String? = null,
        prescriptions: List<Any>? = null,
        vitalSigns: Any? = null,
        labOrders: List<Any>? = null,
        files: List<Any>? = null,
        treatmentPlan: List<Any>? = null,
        shareWithPatient: Boolean? = null
    ): ApiResult<ClinicalRecord> = clinicalRepository.saveClinicalRecord(
        bookingId = bookingId,
        complaints = complaints,
        observations = observations,
        diagnoses = diagnoses,
        notes = notes,
        prescriptions = prescriptions,
        vitalSigns = vitalSigns,
        labOrders = labOrders,
        files = files,
        treatmentPlan = treatmentPlan,
        shareWithPatient = shareWithPatient
    )
}
