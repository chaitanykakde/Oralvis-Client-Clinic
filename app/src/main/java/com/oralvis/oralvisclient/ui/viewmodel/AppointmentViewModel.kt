package com.oralvis.oralvisclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.core.util.DispatcherProvider
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.domain.model.Booking
import com.oralvis.oralvisclient.domain.usecase.AddBookingNotesUseCase
import com.oralvis.oralvisclient.domain.usecase.CancelBookingUseCase
import com.oralvis.oralvisclient.domain.usecase.CancelBookingsByDateUseCase
import com.oralvis.oralvisclient.domain.usecase.GetAppointmentsByDateUseCase
import com.oralvis.oralvisclient.domain.usecase.GetAppointmentsUseCase
import com.oralvis.oralvisclient.domain.usecase.MarkBookingPaidUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppointmentViewModel(
    private val getAppointmentsUseCase: GetAppointmentsUseCase,
    private val getAppointmentsByDateUseCase: GetAppointmentsByDateUseCase,
    private val cancelBookingUseCase: CancelBookingUseCase,
    private val cancelBookingsByDateUseCase: CancelBookingsByDateUseCase,
    private val markBookingPaidUseCase: MarkBookingPaidUseCase,
    private val addBookingNotesUseCase: AddBookingNotesUseCase,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _appointmentsState = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val appointmentsState: StateFlow<UiState<List<Booking>>> = _appointmentsState.asStateFlow()

    private val _appointmentsByDateState = MutableStateFlow<UiState<List<Booking>>>(UiState.Loading)
    val appointmentsByDateState: StateFlow<UiState<List<Booking>>> = _appointmentsByDateState.asStateFlow()

    private val _actionState = MutableStateFlow<UiState<Booking>>(UiState.Loading)
    val actionState: StateFlow<UiState<Booking>> = _actionState.asStateFlow()

    private val _cancelByDateState = MutableStateFlow<UiState<Int>>(UiState.Loading)
    val cancelByDateState: StateFlow<UiState<Int>> = _cancelByDateState.asStateFlow()

    fun loadAppointments(clinicId: String) {
        viewModelScope.launch {
            _appointmentsState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = getAppointmentsUseCase(clinicId)) {
                    is ApiResult.Success -> _appointmentsState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _appointmentsState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun loadAppointmentsByDate(clinicId: String, date: String) {
        viewModelScope.launch {
            _appointmentsByDateState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = getAppointmentsByDateUseCase(clinicId, date)) {
                    is ApiResult.Success -> _appointmentsByDateState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _appointmentsByDateState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun cancelBooking(bookingId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = cancelBookingUseCase(bookingId)) {
                    is ApiResult.Success -> _actionState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _actionState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun cancelBookingsByDate(clinicId: String, date: String) {
        viewModelScope.launch {
            _cancelByDateState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = cancelBookingsByDateUseCase(clinicId, date)) {
                    is ApiResult.Success -> _cancelByDateState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _cancelByDateState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun markPaid(bookingId: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = markBookingPaidUseCase(bookingId)) {
                    is ApiResult.Success -> _actionState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _actionState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun updateNotes(bookingId: String, notes: String) {
        viewModelScope.launch {
            _actionState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = addBookingNotesUseCase(bookingId, notes)) {
                    is ApiResult.Success -> _actionState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _actionState.value = UiState.Error(result.message)
                }
            }
        }
    }
}
