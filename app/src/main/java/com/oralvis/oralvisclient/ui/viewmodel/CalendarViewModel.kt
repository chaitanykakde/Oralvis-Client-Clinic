package com.oralvis.oralvisclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.core.util.DispatcherProvider
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.domain.model.CalendarDayAppointments
import com.oralvis.oralvisclient.domain.model.CalendarMonth
import com.oralvis.oralvisclient.domain.model.CalendarWeek
import com.oralvis.oralvisclient.domain.usecase.GetCalendarDateUseCase
import com.oralvis.oralvisclient.domain.usecase.GetCalendarMonthUseCase
import com.oralvis.oralvisclient.domain.usecase.GetCalendarWeekUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarViewModel(
    private val getCalendarMonthUseCase: GetCalendarMonthUseCase,
    private val getCalendarDateUseCase: GetCalendarDateUseCase,
    private val getCalendarWeekUseCase: GetCalendarWeekUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _calendarMonthState = MutableStateFlow<UiState<CalendarMonth>>(UiState.Loading)
    val calendarMonthState: StateFlow<UiState<CalendarMonth>> = _calendarMonthState.asStateFlow()

    private val _calendarDateState = MutableStateFlow<UiState<CalendarDayAppointments>>(UiState.Loading)
    val calendarDateState: StateFlow<UiState<CalendarDayAppointments>> = _calendarDateState.asStateFlow()

    private val _calendarWeekState = MutableStateFlow<UiState<CalendarWeek>>(UiState.Loading)
    val calendarWeekState: StateFlow<UiState<CalendarWeek>> = _calendarWeekState.asStateFlow()

    fun loadCalendarMonth(clinicId: String, month: Int, year: Int) {
        viewModelScope.launch {
            _calendarMonthState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = getCalendarMonthUseCase(clinicId, month, year)) {
                    is ApiResult.Success -> _calendarMonthState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _calendarMonthState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun loadCalendarDate(clinicId: String, date: String) {
        viewModelScope.launch {
            _calendarDateState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = getCalendarDateUseCase(clinicId, date)) {
                    is ApiResult.Success -> _calendarDateState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _calendarDateState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun loadCalendarWeek(clinicId: String, dates: List<String>) {
        viewModelScope.launch {
            _calendarWeekState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = getCalendarWeekUseCase(clinicId, dates)) {
                    is ApiResult.Success -> _calendarWeekState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _calendarWeekState.value = UiState.Error(result.message)
                }
            }
        }
    }
}
