package com.oralvis.oralvisclient.data.repository.mapper

import com.oralvis.oralvisclient.data.remote.dto.CalendarDateAppointmentDto
import com.oralvis.oralvisclient.data.remote.dto.CalendarMonthDto
import com.oralvis.oralvisclient.data.remote.dto.CalendarWeekDto
import com.oralvis.oralvisclient.data.remote.dto.DayCountDto
import com.oralvis.oralvisclient.domain.model.CalendarDayAppointment
import com.oralvis.oralvisclient.domain.model.CalendarDayAppointments
import com.oralvis.oralvisclient.domain.model.CalendarMonth
import com.oralvis.oralvisclient.domain.model.CalendarWeek
import com.oralvis.oralvisclient.domain.model.DayCount

fun CalendarMonthDto.toDomain(): CalendarMonth = CalendarMonth(
    dayCounts = mapValues { (_, v) ->
        DayCount(
            backend = v.backend ?: 0,
            walkin = v.walkin ?: 0
        )
    }
)

fun List<CalendarDateAppointmentDto>.toCalendarDayAppointments(): CalendarDayAppointments =
    CalendarDayAppointments(
        appointments = map { it.toDomain() }
    )

fun CalendarDateAppointmentDto.toDomain(): CalendarDayAppointment = CalendarDayAppointment(
    patientName = patientName ?: "",
    slotTime = slotTime ?: "",
    appointmentDate = appointmentDate ?: "",
    durationMinutes = durationMinutes
)

fun CalendarWeekDto.toDomain(): CalendarWeek = CalendarWeek(
    byDate = mapValues { (_: String, list: List<CalendarDateAppointmentDto>) ->
        list.map { item -> item.toDomain() }
    }
)
