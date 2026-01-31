package com.oralvis.oralvisclient.domain.usecase

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.CalendarWeek
import com.oralvis.oralvisclient.domain.repository.CalendarRepository

class GetCalendarWeekUseCase(private val calendarRepository: CalendarRepository) {

    suspend operator fun invoke(clinicId: String, dates: List<String>): ApiResult<CalendarWeek> =
        calendarRepository.getCalendarWeek(clinicId, dates)
}
