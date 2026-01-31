package com.oralvis.oralvisclient.domain.repository

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.Booking

interface BookingRepository {
    suspend fun reschedule(bookingId: String, newSlotId: String): ApiResult<Booking>
}
