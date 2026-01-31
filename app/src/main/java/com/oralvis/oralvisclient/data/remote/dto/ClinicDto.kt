package com.oralvis.oralvisclient.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClinicIdResponse(
    @SerializedName("clinicId") val clinicId: String
)

data class ClinicProfileResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("clinic") val clinic: ClinicDto? = null
)

data class ClinicDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("mainarea") val mainarea: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("phoneNo") val phoneNo: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("coverimage") val coverimage: String? = null,
    @SerializedName("fees") val fees: Int? = null,
    @SerializedName("owner") val owner: String? = null,
    @SerializedName("mainDoctor") val mainDoctor: String? = null,
    @SerializedName("dentists") val dentists: List<String>? = null,
    @SerializedName("about") val about: AboutDto? = null,
    @SerializedName("location") val location: LocationDto? = null,
    @SerializedName("services") val services: List<String>? = null,
    @SerializedName("introline") val introline: String? = null,
    @SerializedName("noofpatients") val noofpatients: Int? = null,
    @SerializedName("yearsofexp") val yearsofexp: Int? = null,
    @SerializedName("coordinates") val coordinates: List<Double>? = null
)

data class AboutDto(
    @SerializedName("parah") val parah: String? = null,
    @SerializedName("points_to_be_highlighted") val points_to_be_highlighted: List<String>? = null
)

data class LocationDto(
    @SerializedName("type") val type: String? = null,
    @SerializedName("coordinates") val coordinates: List<Double>? = null
)

data class DashboardStatsDto(
    @SerializedName("totalPatients") val totalPatients: Int? = null,
    @SerializedName("todaysAppointments") val todaysAppointments: Int? = null,
    @SerializedName("completedAppointments") val completedAppointments: Int? = null,
    @SerializedName("earnings") val earnings: Double? = null,
    @SerializedName("totalAppointments") val totalAppointments: Int? = null,
    @SerializedName("appointmentsOverTime") val appointmentsOverTime: List<AppointmentOverTimeDto>? = null
)

data class AppointmentOverTimeDto(
    @SerializedName("_id") val date: String,
    @SerializedName("count") val count: Int
)

data class AppointmentStatusCountsDto(
    @SerializedName("paid") val paid: Int? = null,
    @SerializedName("pending") val pending: Int? = null,
    @SerializedName("confirmed") val confirmed: Int? = null,
    @SerializedName("completed") val completed: Int? = null,
    @SerializedName("cancelled") val cancelled: Int? = null
)

data class BookingDto(
    @SerializedName("_id") val id: String,
    @SerializedName("patientName") val patientName: String? = null,
    @SerializedName("appointmentDate") val appointmentDate: String? = null,
    @SerializedName("slotTime") val slotTime: String? = null,
    @SerializedName("status") val status: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("paymentId") val paymentId: String? = null,
    @SerializedName("durationMinutes") val durationMinutes: Int? = null,
    @SerializedName("clinic") val clinic: Any? = null,
    @SerializedName("patient") val patient: Any? = null,
    @SerializedName("walkinPatient") val walkinPatient: Any? = null,
    @SerializedName("amountPaid") val amountPaid: Double? = null,
    @SerializedName("refundStatus") val refundStatus: String? = null
) {
    fun appointmentDateString(): String = appointmentDate ?: ""
}

data class BookingsByDateResponse(
    @SerializedName("bookings") val bookings: List<BookingDto>? = null
)

data class SlotDto(
    @SerializedName("_id") val id: String,
    @SerializedName("clinic") val clinic: String? = null,
    @SerializedName("date") val date: String? = null,
    @SerializedName("time") val time: String? = null,
    @SerializedName("isAvailable") val isAvailable: Boolean? = null
)

data class BookWalkInRequest(
    @SerializedName("clinicId") val clinicId: String,
    @SerializedName("name") val name: String,
    @SerializedName("phoneNo") val phoneNo: String,
    @SerializedName("appointmentDate") val appointmentDate: String,
    @SerializedName("slotTime") val slotTime: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("abhaId") val abhaId: String? = null,
    @SerializedName("tokenNumber") val tokenNumber: String? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("plannedProcedures") val plannedProcedures: String? = null,
    @SerializedName("doctor") val doctor: String? = null,
    @SerializedName("duration") val duration: String? = null
)

data class CancelBookingResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("booking") val booking: BookingDto? = null
)

data class CancelBookingsByDateRequest(
    @SerializedName("date") val date: String
)

data class CancelBookingsByDateResponse(
    @SerializedName("message") val message: String? = null
)

data class MarkPaidResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("booking") val booking: BookingDto? = null
)

data class UpdateNotesRequest(
    @SerializedName("notes") val notes: String
)

data class UpdateNotesResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("booking") val booking: BookingDto? = null
)

data class ClinicEarningsDto(
    @SerializedName("totalEarnings") val totalEarnings: Double? = null,
    @SerializedName("monthlyEarnings") val monthlyEarnings: List<MonthlyEarningDto>? = null
)

data class MonthlyEarningDto(
    @SerializedName("month") val month: String,
    @SerializedName("amount") val amount: Double
)

data class DentistDto(
    @SerializedName("_id") val id: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("qualification") val qualification: String? = null,
    @SerializedName("image") val image: String? = null
)

data class DentistsResponse(
    @SerializedName("dentists") val dentists: List<DentistDto>? = null
)
