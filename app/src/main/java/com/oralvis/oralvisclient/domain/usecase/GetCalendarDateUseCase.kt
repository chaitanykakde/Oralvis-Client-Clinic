package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.CalendarDayAppointments
import com.oralvis.oralvisclient.domain.repository.CalendarRepository

class GetCalendarDateUseCase(private val calendarRepository: CalendarRepository) {

    suspend operator fun invoke(clinicId: String, date: String): ApiResult<CalendarDayAppointments> =
        calendarRepository.getCalendarDate(clinicId, date)
}
