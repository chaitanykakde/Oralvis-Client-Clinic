package com.oralvis.oralvisclient.data.remote

import com.oralvis.oralvisclient.data.remote.dto.AppointmentStatusCountsDto
import com.oralvis.oralvisclient.data.remote.dto.BookWalkInRequest
import com.oralvis.oralvisclient.data.remote.dto.BookingDto
import com.oralvis.oralvisclient.data.remote.dto.BookingsByDateResponse
import com.oralvis.oralvisclient.data.remote.dto.CancelBookingsByDateRequest
import com.oralvis.oralvisclient.data.remote.dto.CancelBookingsByDateResponse
import com.oralvis.oralvisclient.data.remote.dto.ClinicDto
import com.oralvis.oralvisclient.data.remote.dto.ClinicEarningsDto
import com.oralvis.oralvisclient.data.remote.dto.ClinicIdResponse
import com.oralvis.oralvisclient.data.remote.dto.ClinicProfileResponse
import com.oralvis.oralvisclient.data.remote.dto.DashboardStatsDto
import com.oralvis.oralvisclient.data.remote.dto.DentistsResponse
import com.oralvis.oralvisclient.data.remote.dto.MarkPaidResponse
import com.oralvis.oralvisclient.data.remote.dto.SlotDto
import com.oralvis.oralvisclient.data.remote.dto.CancelBookingResponse
import com.oralvis.oralvisclient.data.remote.dto.UpdateNotesRequest
import com.oralvis.oralvisclient.data.remote.dto.UpdateNotesResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ClinicApi {

    @GET("api/clinics/clinic-id/{userId}")
    suspend fun getClinicId(@Path("userId") userId: String): Response<ClinicIdResponse>

    @GET("api/clinics/clinic-profile/{userId}")
    suspend fun getClinicProfile(@Path("userId") userId: String): Response<ClinicProfileResponse>

    @GET("api/clinics/dashboard-stats-clinic/{clinicId}")
    suspend fun getDashboardStats(@Path("clinicId") clinicId: String): Response<DashboardStatsDto>

    @GET("api/clinics/earning-dashboard-stats/{clinicId}")
    suspend fun getEarningDashboardStats(@Path("clinicId") clinicId: String): Response<DashboardStatsDto>

    @GET("api/clinics/appointment-status-counts/{clinicId}")
    suspend fun getAppointmentStatusCounts(@Path("clinicId") clinicId: String): Response<AppointmentStatusCountsDto>

    @GET("api/clinics/appointments/{clinicId}")
    suspend fun getAppointments(@Path("clinicId") clinicId: String): Response<List<BookingDto>>

    @GET("api/clinics/bookings-by-date/{clinicId}")
    suspend fun getBookingsByDate(
        @Path("clinicId") clinicId: String,
        @Query("date") date: String
    ): Response<BookingsByDateResponse>

    @GET("api/clinics/slotss/{clinicId}")
    suspend fun getSlots(
        @Path("clinicId") clinicId: String,
        @Query("date") date: String
    ): Response<List<SlotDto>>

    @POST("api/clinics/book-walkin")
    suspend fun bookWalkIn(@Body body: BookWalkInRequest): Response<BookWalkInResponse>

    @PATCH("api/clinics/cancel-booking/{bookingId}")
    suspend fun cancelBooking(@Path("bookingId") bookingId: String): Response<CancelBookingResponse>

    @PATCH("api/clinics/cancel-bookings/date/{clinicId}")
    suspend fun cancelBookingsByDate(
        @Path("clinicId") clinicId: String,
        @Body body: CancelBookingsByDateRequest
    ): Response<CancelBookingsByDateResponse>

    @PATCH("api/clinics/bookings/{bookingId}/mark-paid")
    suspend fun markBookingPaid(@Path("bookingId") bookingId: String): Response<MarkPaidResponse>

    @PATCH("api/clinics/bookings/{bookingId}/notes")
    suspend fun updateBookingNotes(
        @Path("bookingId") bookingId: String,
        @Body body: UpdateNotesRequest
    ): Response<UpdateNotesResponse>

    @GET("api/clinics/clinic-earnings/{clinicId}")
    suspend fun getClinicEarnings(@Path("clinicId") clinicId: String): Response<ClinicEarningsDto>

    @GET("api/clinics/dentists/{clinicId}")
    suspend fun getDentists(@Path("clinicId") clinicId: String): Response<DentistsResponse>

    @POST("api/clinics/clinic-profile/{userId}")
    suspend fun createClinicProfile(
        @Path("userId") userId: String,
        @Body body: Map<String, Any?>
    ): Response<ClinicDto>

    @PATCH("api/clinics/clinic-profile/{userId}")
    suspend fun updateClinicProfile(
        @Path("userId") userId: String,
        @Body body: UpdateClinicProfileBody
    ): Response<UpdateClinicProfileResponse>
}

data class BookWalkInResponse(
    val message: String? = null,
    val booking: BookingDto? = null
)

data class UpdateClinicProfileBody(
    val clinicData: Map<String, Any?>? = null,
    val userData: Map<String, Any?>? = null
)

data class UpdateClinicProfileResponse(
    val message: String? = null,
    val clinic: ClinicDto? = null,
    val user: Any? = null
)
