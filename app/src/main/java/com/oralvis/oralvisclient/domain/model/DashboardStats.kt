package com.oralvis.oralvisclient.domain.model

/**
 * Dashboard stats (dashboard-stats-clinic or earning-dashboard-stats).
 */
data class DashboardStats(
    val totalPatients: Int,
    val todaysAppointments: Int,
    val completedAppointments: Int,
    val earnings: Double,
    val appointmentsOverTime: List<AppointmentCountByDate>? = null,
    val totalAppointments: Int? = null
)

data class AppointmentCountByDate(
    val date: String,
    val count: Int
)

/**
 * Appointment status counts (paid, pending, confirmed, completed, cancelled).
 */
data class AppointmentStatusCounts(
    val paid: Int,
    val pending: Int,
    val confirmed: Int,
    val completed: Int,
    val cancelled: Int
)
