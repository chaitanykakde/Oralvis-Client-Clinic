package com.oralvis.oralvisclient.domain.repository

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.CalendarDayAppointments
import com.oralvis.oralvisclient.domain.model.CalendarMonth
import com.oralvis.oralvisclient.domain.model.CalendarWeek

interface CalendarRepository {
    suspend fun getCalendarMonth(clinicId: String, month: Int, year: Int): ApiResult<CalendarMonth>
    suspend fun getCalendarDate(clinicId: String, date: String): ApiResult<CalendarDayAppointments>
    suspend fun getCalendarWeek(clinicId: String, dates: List<String>): ApiResult<CalendarWeek>
}
