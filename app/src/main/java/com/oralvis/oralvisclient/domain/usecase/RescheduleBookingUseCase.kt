package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.Booking
import com.oralvis.oralvisclient.domain.repository.BookingRepository

class RescheduleBookingUseCase(private val bookingRepository: BookingRepository) {

    suspend operator fun invoke(bookingId: String, newSlotId: String): ApiResult<Booking> =
        bookingRepository.reschedule(bookingId, newSlotId)
}
