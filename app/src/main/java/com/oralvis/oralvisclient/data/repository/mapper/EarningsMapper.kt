package com.oralvis.oralvisclient.data.repository.mapper

import com.oralvis.oralvisclient.data.remote.dto.ClinicEarningsDto
import com.oralvis.oralvisclient.data.remote.dto.MonthlyEarningDto
import com.oralvis.oralvisclient.domain.model.ClinicEarnings
import com.oralvis.oralvisclient.domain.model.MonthlyEarning

fun ClinicEarningsDto.toDomain(): ClinicEarnings = ClinicEarnings(
    totalEarnings = totalEarnings ?: 0.0,
    monthlyEarnings = monthlyEarnings?.map { it.toDomain() } ?: emptyList()
)

fun MonthlyEarningDto.toDomain(): MonthlyEarning = MonthlyEarning(
    month = month,
    amount = amount
)
