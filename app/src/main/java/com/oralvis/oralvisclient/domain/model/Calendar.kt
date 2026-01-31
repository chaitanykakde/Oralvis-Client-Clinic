package com.oralvis.oralvisclient.domain.model

/**
 * Calendar month view: day -> { backend count, walkin count }.
 */
data class CalendarMonth(
    val dayCounts: Map<String, DayCount>
)

data class DayCount(
    val backend: Int,
    val walkin: Int
)

/**
 * Calendar date view: list of appointments for that day.
 */
data class CalendarDayAppointments(
    val appointments: List<CalendarDayAppointment>
)

data class CalendarDayAppointment(
    val patientName: String,
    val slotTime: String,
    val appointmentDate: String,
    val durationMinutes: Int? = null
)

/**
 * Calendar week: date -> list of appointments.
 */
data class CalendarWeek(
    val byDate: Map<String, List<CalendarDayAppointment>>
)
