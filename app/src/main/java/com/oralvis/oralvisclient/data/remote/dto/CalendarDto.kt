package com.oralvis.oralvisclient.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Month response: { "01": { "backend": 2, "walkin": 1 }, "15": { ... } }
 */
typealias CalendarMonthDto = Map<String, DayCountDto>

data class DayCountDto(
    @SerializedName("backend") val backend: Int? = null,
    @SerializedName("walkin") val walkin: Int? = null
)

/**
 * Date response: array of { patientName, slotTime, appointmentDate, durationMinutes }
 */
data class CalendarDateAppointmentDto(
    @SerializedName("patientName") val patientName: String? = null,
    @SerializedName("slotTime") val slotTime: String? = null,
    @SerializedName("appointmentDate") val appointmentDate: String? = null,
    @SerializedName("durationMinutes") val durationMinutes: Int? = null
)

/**
 * Week response: { "YYYY-MM-DD": [ CalendarDateAppointmentDto, ... ], ... }
 */
typealias CalendarWeekDto = Map<String, List<CalendarDateAppointmentDto>>
