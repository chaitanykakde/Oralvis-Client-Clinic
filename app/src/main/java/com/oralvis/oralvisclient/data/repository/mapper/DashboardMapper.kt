package com.oralvis.oralvisclient.data.repository.mapper

import com.oralvis.oralvisclient.data.remote.dto.AppointmentOverTimeDto
import com.oralvis.oralvisclient.data.remote.dto.AppointmentStatusCountsDto
import com.oralvis.oralvisclient.data.remote.dto.DashboardStatsDto
import com.oralvis.oralvisclient.domain.model.AppointmentCountByDate
import com.oralvis.oralvisclient.domain.model.AppointmentStatusCounts
import com.oralvis.oralvisclient.domain.model.DashboardStats

fun DashboardStatsDto.toDomain(): DashboardStats = DashboardStats(
    totalPatients = totalPatients ?: 0,
    todaysAppointments = todaysAppointments ?: 0,
    completedAppointments = completedAppointments ?: 0,
    earnings = earnings ?: 0.0,
    appointmentsOverTime = appointmentsOverTime?.map { it.toDomain() },
    totalAppointments = totalAppointments
)

fun AppointmentOverTimeDto.toDomain(): AppointmentCountByDate = AppointmentCountByDate(
    date = date,
    count = count
)

fun AppointmentStatusCountsDto.toDomain(): AppointmentStatusCounts = AppointmentStatusCounts(
    paid = paid ?: 0,
    pending = pending ?: 0,
    confirmed = confirmed ?: 0,
    completed = completed ?: 0,
    cancelled = cancelled ?: 0
)
