package com.oralvis.oralvisclient.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.core.util.DispatcherProvider
import com.oralvis.oralvisclient.core.util.UiState
import com.oralvis.oralvisclient.domain.model.ClinicalRecord
import com.oralvis.oralvisclient.domain.model.MedicalHistoryEntry
import com.oralvis.oralvisclient.domain.usecase.GetClinicalRecordUseCase
import com.oralvis.oralvisclient.domain.usecase.SaveClinicalRecordUseCase
import com.oralvis.oralvisclient.domain.repository.ClinicalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ClinicalViewModel(
    private val getClinicalRecordUseCase: GetClinicalRecordUseCase,
    private val saveClinicalRecordUseCase: SaveClinicalRecordUseCase,
    private val clinicalRepository: ClinicalRepository,
    private val dispatcherProvider: DispatcherProvider
) : ViewModel() {

    private val _clinicalRecordState = MutableStateFlow<UiState<ClinicalRecord?>>(UiState.Loading)
    val clinicalRecordState: StateFlow<UiState<ClinicalRecord?>> = _clinicalRecordState.asStateFlow()

    private val _saveRecordState = MutableStateFlow<UiState<ClinicalRecord>>(UiState.Loading)
    val saveRecordState: StateFlow<UiState<ClinicalRecord>> = _saveRecordState.asStateFlow()

    private val _medicalHistoryState = MutableStateFlow<UiState<List<MedicalHistoryEntry>>>(UiState.Loading)
    val medicalHistoryState: StateFlow<UiState<List<MedicalHistoryEntry>>> = _medicalHistoryState.asStateFlow()

    fun loadClinicalRecord(bookingId: String) {
        viewModelScope.launch {
            _clinicalRecordState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = getClinicalRecordUseCase(bookingId)) {
                    is ApiResult.Success -> _clinicalRecordState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _clinicalRecordState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun saveClinicalRecord(
        bookingId: String,
        complaints: Any? = null,
        observations: Any? = null,
        diagnoses: List<String>? = null,
        notes: String? = null,
        prescriptions: List<Any>? = null,
        vitalSigns: Any? = null,
        labOrders: List<Any>? = null,
        files: List<Any>? = null,
        treatmentPlan: List<Any>? = null,
        shareWithPatient: Boolean? = null
    ) {
        viewModelScope.launch {
            _saveRecordState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = saveClinicalRecordUseCase(
                    bookingId, complaints, observations, diagnoses, notes,
                    prescriptions, vitalSigns, labOrders, files, treatmentPlan, shareWithPatient
                )) {
                    is ApiResult.Success -> _saveRecordState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _saveRecordState.value = UiState.Error(result.message)
                }
            }
        }
    }

    fun loadMedicalHistory(clinicId: String, patientId: String?, walkinPatientId: String?) {
        viewModelScope.launch {
            _medicalHistoryState.value = UiState.Loading
            withContext(dispatcherProvider.io) {
                when (val result = clinicalRepository.getMedicalHistory(clinicId, patientId, walkinPatientId)) {
                    is ApiResult.Success -> _medicalHistoryState.value = UiState.Success(result.data)
                    is ApiResult.Error -> _medicalHistoryState.value = UiState.Error(result.message)
                }
            }
        }
    }
}
