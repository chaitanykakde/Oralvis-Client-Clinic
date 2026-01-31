package com.oralvis.oralvisclient.domain.model

/**
 * Clinic earnings (clinic-earnings API).
 */
data class ClinicEarnings(
    val totalEarnings: Double,
    val monthlyEarnings: List<MonthlyEarning>
)

data class MonthlyEarning(
    val month: String,
    val amount: Double
)
