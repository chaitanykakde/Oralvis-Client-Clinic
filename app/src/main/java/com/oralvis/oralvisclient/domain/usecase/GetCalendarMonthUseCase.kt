package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.CalendarMonth
import com.oralvis.oralvisclient.domain.repository.CalendarRepository

class GetCalendarMonthUseCase(private val calendarRepository: CalendarRepository) {

    suspend operator fun invoke(clinicId: String, month: Int, year: Int): ApiResult<CalendarMonth> =
        calendarRepository.getCalendarMonth(clinicId, month, year)
}
