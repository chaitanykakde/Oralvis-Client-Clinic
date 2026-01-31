package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.Booking
import com.oralvis.oralvisclient.domain.repository.ClinicRepository

class AddBookingNotesUseCase(private val clinicRepository: ClinicRepository) {

    suspend operator fun invoke(bookingId: String, notes: String): ApiResult<Booking> =
        clinicRepository.updateBookingNotes(bookingId, notes)
}
