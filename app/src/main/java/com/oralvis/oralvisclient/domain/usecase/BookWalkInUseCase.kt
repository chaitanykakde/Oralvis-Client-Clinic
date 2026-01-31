package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.Booking
import com.oralvis.oralvisclient.domain.repository.ClinicRepository

class BookWalkInUseCase(private val clinicRepository: ClinicRepository) {

    suspend operator fun invoke(
        clinicId: String,
        name: String,
        phoneNo: String,
        appointmentDate: String,
        slotTime: String,
        email: String? = null,
        abhaId: String? = null,
        tokenNumber: String? = null,
        notes: String? = null,
        plannedProcedures: String? = null,
        doctor: String? = null,
        duration: String? = null
    ): ApiResult<Booking> = clinicRepository.bookWalkIn(
        clinicId = clinicId,
        name = name,
        phoneNo = phoneNo,
        appointmentDate = appointmentDate,
        slotTime = slotTime,
        email = email,
        abhaId = abhaId,
        tokenNumber = tokenNumber,
        notes = notes,
        plannedProcedures = plannedProcedures,
        doctor = doctor,
        duration = duration
    )
}
