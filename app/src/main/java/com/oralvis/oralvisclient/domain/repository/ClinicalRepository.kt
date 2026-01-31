package com.oralvis.oralvisclient.domain.repository

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.ClinicalRecord
import com.oralvis.oralvisclient.domain.model.MedicalHistoryEntry
import java.io.File

interface ClinicalRepository {
    suspend fun getClinicalRecord(bookingId: String): ApiResult<ClinicalRecord?>
    suspend fun saveClinicalRecord(
        bookingId: String,
        complaints: Any?,
        observations: Any?,
        diagnoses: List<String>?,
        notes: String?,
        prescriptions: List<Any>?,
        vitalSigns: Any?,
        labOrders: List<Any>?,
        files: List<Any>?,
        treatmentPlan: List<Any>?,
        shareWithPatient: Boolean?
    ): ApiResult<ClinicalRecord>
    suspend fun uploadClinicalAttachment(
        bookingId: String,
        type: String,
        field: String,
        file: File
    ): ApiResult<ClinicalRecord>
    suspend fun uploadPrescriptionImage(bookingId: String, file: File, prescriptionIndex: Int?): ApiResult<String>
    suspend fun uploadFile(bookingId: String, file: File): ApiResult<UploadedFile>
    suspend fun saveMedicalHistory(
        clinicId: String,
        condition: String,
        details: String?,
        patientId: String?,
        walkinPatientId: String?
    ): ApiResult<MedicalHistoryEntry>
    suspend fun getMedicalHistory(
        clinicId: String,
        patientId: String?,
        walkinPatientId: String?
    ): ApiResult<List<MedicalHistoryEntry>>
}

data class UploadedFile(
    val url: String,
    val fileName: String,
    val fileType: String,
    val uploadedAt: String?
)
