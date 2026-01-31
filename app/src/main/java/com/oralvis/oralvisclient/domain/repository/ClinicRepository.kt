package com.oralvis.oralvisclient.domain.repository

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.AppointmentStatusCounts
import com.oralvis.oralvisclient.domain.model.Booking
import com.oralvis.oralvisclient.domain.model.Clinic
import com.oralvis.oralvisclient.domain.model.ClinicEarnings
import com.oralvis.oralvisclient.domain.model.DashboardStats
import com.oralvis.oralvisclient.domain.model.Dentist
import com.oralvis.oralvisclient.domain.model.Slot

interface ClinicRepository {
    suspend fun getClinicId(userId: String): ApiResult<String>
    suspend fun getClinicProfile(userId: String): ApiResult<Clinic?>
    suspend fun getDashboardStats(clinicId: String): ApiResult<DashboardStats>
    suspend fun getEarningDashboardStats(clinicId: String): ApiResult<DashboardStats>
    suspend fun getAppointmentStatusCounts(clinicId: String): ApiResult<AppointmentStatusCounts>
    suspend fun getAppointments(clinicId: String): ApiResult<List<Booking>>
    suspend fun getBookingsByDate(clinicId: String, date: String): ApiResult<List<Booking>>
    suspend fun getSlots(clinicId: String, date: String): ApiResult<List<Slot>>
    suspend fun bookWalkIn(
        clinicId: String,
        name: String,
        phoneNo: String,
        appointmentDate: String,
        slotTime: String,
        email: String?,
        abhaId: String?,
        tokenNumber: String?,
        notes: String?,
        plannedProcedures: String?,
        doctor: String?,
        duration: String?
    ): ApiResult<Booking>
    suspend fun cancelBooking(bookingId: String): ApiResult<Booking>
    suspend fun cancelBookingsByDate(clinicId: String, date: String): ApiResult<Int>
    suspend fun markBookingPaid(bookingId: String): ApiResult<Booking>
    suspend fun updateBookingNotes(bookingId: String, notes: String): ApiResult<Booking>
    suspend fun getClinicEarnings(clinicId: String): ApiResult<ClinicEarnings>
    suspend fun getDentists(clinicId: String): ApiResult<List<Dentist>>
    suspend fun createClinicProfile(userId: String, clinicData: Map<String, Any?>): ApiResult<Clinic>
    suspend fun updateClinicProfile(userId: String, clinicData: Map<String, Any?>, userData: Map<String, Any?>?): ApiResult<Clinic>
}
