package com.oralvis.oralvisclient.data.repository

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.core.network.safeApiCall
import com.oralvis.oralvisclient.data.remote.BookingApi
import com.oralvis.oralvisclient.data.remote.dto.RescheduleRequest
import com.oralvis.oralvisclient.data.repository.mapper.toDomain
import com.oralvis.oralvisclient.domain.model.Booking
import com.oralvis.oralvisclient.domain.repository.BookingRepository

class BookingRepositoryImpl(private val bookingApi: BookingApi) : BookingRepository {

    override suspend fun reschedule(bookingId: String, newSlotId: String): ApiResult<Booking> = safeApiCall {
        bookingApi.reschedule(bookingId, RescheduleRequest(newSlotId = newSlotId))
    }.let { result ->
        when (result) {
            is ApiResult.Success -> {
                val booking = result.data.booking?.toDomain(null)
                if (booking == null) ApiResult.Error("Invalid response", null)
                else ApiResult.Success(booking)
            }
            is ApiResult.Error -> result
        }
    }
}
