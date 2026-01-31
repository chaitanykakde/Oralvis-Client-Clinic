package com.oralvis.oralvisclient.data.repository

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.core.network.safeApiCall
import com.oralvis.oralvisclient.data.remote.CalendarApi
import com.oralvis.oralvisclient.data.remote.CalendarWeekRequest
import com.oralvis.oralvisclient.data.repository.mapper.toCalendarDayAppointments
import com.oralvis.oralvisclient.data.repository.mapper.toDomain
import com.oralvis.oralvisclient.domain.model.CalendarDayAppointments
import com.oralvis.oralvisclient.domain.model.CalendarMonth
import com.oralvis.oralvisclient.domain.model.CalendarWeek
import com.oralvis.oralvisclient.domain.repository.CalendarRepository

class CalendarRepositoryImpl(private val calendarApi: CalendarApi) : CalendarRepository {

    override suspend fun getCalendarMonth(clinicId: String, month: Int, year: Int): ApiResult<CalendarMonth> = safeApiCall {
        calendarApi.getCalendarMonth(clinicId, month, year)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getCalendarDate(clinicId: String, date: String): ApiResult<CalendarDayAppointments> = safeApiCall {
        calendarApi.getCalendarDate(clinicId, date)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toCalendarDayAppointments())
            is ApiResult.Error -> result
        }
    }

    override suspend fun getCalendarWeek(clinicId: String, dates: List<String>): ApiResult<CalendarWeek> = safeApiCall {
        calendarApi.getCalendarWeek(CalendarWeekRequest(clinicId = clinicId, dates = dates))
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.toDomain())
            is ApiResult.Error -> result
        }
    }
}
