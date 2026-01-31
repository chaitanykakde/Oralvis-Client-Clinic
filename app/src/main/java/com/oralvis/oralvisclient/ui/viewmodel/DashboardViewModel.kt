package com.oralvis.oralvisclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.core.util.DispatcherProvider
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.domain.model.AppointmentStatusCounts
import com.oralvis.oralvisclient.domain.model.DashboardStats
import com.oralvis.oralvisclient.domain.repository.ClinicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardViewModel(
    private val clinicRepository: ClinicRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _dashboardStatsState = MutableStateFlow<UiState<DashboardStats>>(UiState.Loading)
    val dashboardStatsState: StateFlow<UiState<DashboardStats>> = _dashboardStatsState.asStateFlow()

    private val _statusCountsState = MutableStateFlow<UiState<AppointmentStatusCounts>>(UiState.Loading)
    val statusCountsState: StateFlow<UiState<AppointmentStatusCounts>> = _statusCountsState.asStateFlow()

    fun loadDashboardStats(clinicId: String) {
        viewModelScope.launch {
            _dashboardStatsState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = clinicRepository.getDashboardStats(clinicId)) {
                    is ApiResult.Success -> _dashboardStatsState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _dashboardStatsState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun loadStatusCounts(clinicId: String) {
        viewModelScope.launch {
            _statusCountsState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = clinicRepository.getAppointmentStatusCounts(clinicId)) {
                    is ApiResult.Success -> _statusCountsState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _statusCountsState.value = UiState.Error(result.message)
                }
            }
        }
    }
}
