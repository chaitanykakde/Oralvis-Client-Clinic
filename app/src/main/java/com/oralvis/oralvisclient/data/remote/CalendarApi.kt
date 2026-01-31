package com.oralvis.oralvisclient.data.remote

import com.oralvis.oralvisclient.data.remote.dto.CalendarDateAppointmentDto
import com.oralvis.oralvisclient.data.remote.dto.CalendarMonthDto
import com.oralvis.oralvisclient.data.remote.dto.CalendarWeekDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CalendarApi {

    /**
     * Month view: ?month=1&year=2025 -> { "01": { "backend", "walkin" }, ... }
     */
    @GET("api/clinics/calendar/{clinicId}")
    suspend fun getCalendarMonth(
        @Path("clinicId") clinicId: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<CalendarMonthDto>

    /**
     * Single date: ?date=2025-02-01 -> array of appointments
     */
    @GET("api/clinics/calendar/{clinicId}")
    suspend fun getCalendarDate(
        @Path("clinicId") clinicId: String,
        @Query("date") date: String
    ): Response<List<CalendarDateAppointmentDto>>

    /**
     * Week: POST body { clinicId, dates: ["2025-02-01", ...] } -> { "2025-02-01": [...], ... }
     */
    @POST("api/clinics/calendar/week")
    suspend fun getCalendarWeek(@Body body: CalendarWeekRequest): Response<CalendarWeekDto>
}

data class CalendarWeekRequest(
    val clinicId: String,
    val dates: List<String>
)
