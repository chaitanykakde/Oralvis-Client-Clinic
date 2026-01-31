package com.oralvis.oralvisclient.data.remote

import com.oralvis.oralvisclient.data.remote.dto.RescheduleRequest
import com.oralvis.oralvisclient.data.remote.dto.RescheduleResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface BookingApi {

    @POST("api/reschedule/{bookingId}")
    suspend fun reschedule(
        @Path("bookingId") bookingId: String,
        @Body body: RescheduleRequest
    ): Response<RescheduleResponse>
}
