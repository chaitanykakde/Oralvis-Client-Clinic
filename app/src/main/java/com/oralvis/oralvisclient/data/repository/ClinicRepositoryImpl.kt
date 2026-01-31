package com.oralvis.oralvisclient.data.repository

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.core.network.safeApiCall
import com.oralvis.oralvisclient.data.remote.ClinicApi
import com.oralvis.oralvisclient.data.remote.UpdateClinicProfileBody
import com.oralvis.oralvisclient.data.remote.dto.BookWalkInRequest
import com.oralvis.oralvisclient.data.remote.dto.CancelBookingsByDateRequest
import com.oralvis.oralvisclient.data.remote.dto.UpdateNotesRequest
import com.oralvis.oralvisclient.data.repository.mapper.toDomain
import com.oralvis.oralvisclient.domain.model.AppointmentStatusCounts
import com.oralvis.oralvisclient.domain.model.Booking
import com.oralvis.oralvisclient.domain.model.Clinic
import com.oralvis.oralvisclient.domain.model.ClinicEarnings
import com.oralvis.oralvisclient.domain.model.DashboardStats
import com.oralvis.oralvisclient.domain.model.Dentist
import com.oralvis.oralvisclient.domain.model.Slot
import com.oralvis.oralvisclient.domain.repository.ClinicRepository

class ClinicRepositoryImpl(private val clinicApi: ClinicApi) : ClinicRepository {

    override suspend fun getClinicId(userId: String): ApiResult<String> = safeApiCall {
        clinicApi.getClinicId(userId)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.clinicId)
            is ApiResult.Error -> result
        }
    }

    override suspend fun getClinicProfile(userId: String): ApiResult<Clinic?> = safeApiCall {
        clinicApi.getClinicProfile(userId)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.clinic?.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getDashboardStats(clinicId: String): ApiResult<DashboardStats> = safeApiCall {
        clinicApi.getDashboardStats(clinicId)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getEarningDashboardStats(clinicId: String): ApiResult<DashboardStats> = safeApiCall {
        clinicApi.getEarningDashboardStats(clinicId)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getAppointmentStatusCounts(clinicId: String): ApiResult<AppointmentStatusCounts> = safeApiCall {
        clinicApi.getAppointmentStatusCounts(clinicId)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getAppointments(clinicId: String): ApiResult<List<Booking>> = safeApiCall {
        clinicApi.getAppointments(clinicId)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain(clinicId) })
            is ApiResult.Error -> result
        }
    }

    override suspend fun getBookingsByDate(clinicId: String, date: String): ApiResult<List<Booking>> = safeApiCall {
        clinicApi.getBookingsByDate(clinicId, date)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success((result.data.bookings ?: emptyList()).map { it.toDomain(clinicId) })
            is ApiResult.Error -> result
        }
    }

    override suspend fun getSlots(clinicId: String, date: String): ApiResult<List<Slot>> = safeApiCall {
        clinicApi.getSlots(clinicId, date)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    override suspend fun bookWalkIn(
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
    ): ApiResult<Booking> = safeApiCall {
        clinicApi.bookWalkIn(
            BookWalkInRequest(
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
        )
    }.let { result ->
        when (result) {
            is ApiResult.Success -> {
                val booking = result.data.booking?.toDomain(clinicId)
                if (booking == null) ApiResult.Error("Invalid response", null)
                else ApiResult.Success(booking)
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun cancelBooking(bookingId: String): ApiResult<Booking> = safeApiCall {
        clinicApi.cancelBooking(bookingId)
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

    override suspend fun cancelBookingsByDate(clinicId: String, date: String): ApiResult<Int> = safeApiCall {
        clinicApi.cancelBookingsByDate(clinicId, CancelBookingsByDateRequest(date = date))
    }.let { result ->
        when (result) {
            is ApiResult.Success -> {
                val msg = result.data.message ?: ""
                val num = Regex("Cancelled (\\d+)").find(msg)?.groupValues?.get(1)?.toIntOrNull() ?: 0
                ApiResult.Success(num)
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun markBookingPaid(bookingId: String): ApiResult<Booking> = safeApiCall {
        clinicApi.markBookingPaid(bookingId)
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

    override suspend fun updateBookingNotes(bookingId: String, notes: String): ApiResult<Booking> = safeApiCall {
        clinicApi.updateBookingNotes(bookingId, UpdateNotesRequest(notes))
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

    override suspend fun getClinicEarnings(clinicId: String): ApiResult<ClinicEarnings> = safeApiCall {
        clinicApi.getClinicEarnings(clinicId)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getDentists(clinicId: String): ApiResult<List<Dentist>> = safeApiCall {
        clinicApi.getDentists(clinicId)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success((result.data.dentists ?: emptyList()).map { it.toDomain() })
            is ApiResult.Error -> result
        }
    }

    override suspend fun createClinicProfile(userId: String, clinicData: Map<String, Any?>): ApiResult<Clinic> = safeApiCall {
        clinicApi.createClinicProfile(userId, clinicData)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun updateClinicProfile(
        userId: String,
        clinicData: Map<String, Any?>,
        userData: Map<String, Any?>?
    ): ApiResult<Clinic> = safeApiCall {
        clinicApi.updateClinicProfile(userId, UpdateClinicProfileBody(clinicData, userData))
    }.let { result ->
        when (result) {
            is ApiResult.Success -> {
                val clinic = result.data.clinic?.toDomain()
                if (clinic == null) ApiResult.Error("Invalid response", null)
                else ApiResult.Success(clinic)
            }
            is ApiResult.Error -> result
        }
    }
}
