package com.oralvis.oralvisclient.data.repository

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.core.network.safeApiCall
import com.oralvis.oralvisclient.data.remote.ClinicalApi
import com.oralvis.oralvisclient.data.remote.dto.SaveClinicalRecordRequest
import com.oralvis.oralvisclient.data.remote.dto.SaveMedicalHistoryRequest
import com.oralvis.oralvisclient.data.repository.mapper.toDomain
import com.oralvis.oralvisclient.domain.model.ClinicalRecord
import com.oralvis.oralvisclient.domain.model.MedicalHistoryEntry
import com.oralvis.oralvisclient.domain.repository.ClinicalRepository
import com.oralvis.oralvisclient.domain.repository.UploadedFile
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class ClinicalRepositoryImpl(private val clinicalApi: ClinicalApi) : ClinicalRepository {

    override suspend fun getClinicalRecord(bookingId: String): ApiResult<ClinicalRecord?> = safeApiCall {
        clinicalApi.getClinicalRecord(bookingId)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.clinicalRecord?.toDomain())
            is ApiResult.Error -> result
        }
    }

    override suspend fun saveClinicalRecord(
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
    ): ApiResult<ClinicalRecord> = safeApiCall {
        clinicalApi.saveClinicalRecord(
            bookingId,
            SaveClinicalRecordRequest(
                complaints = complaints,
                observations = observations,
                diagnoses = diagnoses,
                notes = notes,
                prescriptions = prescriptions,
                vitalSigns = vitalSigns,
                labOrders = labOrders,
                files = files,
                treatmentPlan = treatmentPlan,
                shareWithPatient = shareWithPatient
            )
        )
    }.let { result ->
        when (result) {
            is ApiResult.Success -> {
                val record = result.data.clinicalRecord?.toDomain()
                if (record == null) ApiResult.Error("Invalid response", null)
                else ApiResult.Success(record)
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun uploadClinicalAttachment(
        bookingId: String,
        type: String,
        field: String,
        file: File
    ): ApiResult<ClinicalRecord> = safeApiCall {
        clinicalApi.uploadClinicalAttachment(
            bookingId,
            type.toRequestBody("text/plain".toMediaTypeOrNull()),
            field.toRequestBody("text/plain".toMediaTypeOrNull()),
            MultipartBody.Part.createFormData("file", file.name, file.asRequestBody(null))
        )
    }.let { result ->
        when (result) {
            is ApiResult.Success -> {
                val record = result.data.clinicalRecord?.toDomain()
                if (record == null) ApiResult.Error("Invalid response", null)
                else ApiResult.Success(record)
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun uploadPrescriptionImage(
        bookingId: String,
        file: File,
        prescriptionIndex: Int?
    ): ApiResult<String> = safeApiCall {
        clinicalApi.uploadPrescriptionImage(
            bookingId,
            MultipartBody.Part.createFormData("file", file.name, file.asRequestBody(null)),
            prescriptionIndex?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        )
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(result.data.imageUrl ?: "")
            is ApiResult.Error -> result
        }
    }

    override suspend fun uploadFile(bookingId: String, file: File): ApiResult<UploadedFile> = safeApiCall {
        clinicalApi.uploadFile(
            bookingId,
            MultipartBody.Part.createFormData("file", file.name, file.asRequestBody(null))
        )
    }.let { result ->
        when (result) {
            is ApiResult.Success -> {
                val f = result.data.file
                if (f == null) ApiResult.Error("Invalid response", null)
                else ApiResult.Success(
                    UploadedFile(
                        url = f.url,
                        fileName = f.fileName,
                        fileType = f.fileType,
                        uploadedAt = f.uploadedAt
                    )
                )
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun saveMedicalHistory(
        clinicId: String,
        condition: String,
        details: String?,
        patientId: String?,
        walkinPatientId: String?
    ): ApiResult<MedicalHistoryEntry> = safeApiCall {
        clinicalApi.saveMedicalHistory(
            SaveMedicalHistoryRequest(
                clinicId = clinicId,
                patientId = patientId,
                walkinPatientId = walkinPatientId,
                condition = condition,
                details = details
            )
        )
    }.let { result ->
        when (result) {
            is ApiResult.Success -> {
                val entry = result.data.medicalHistory?.toDomain()
                if (entry == null) ApiResult.Error("Invalid response", null)
                else ApiResult.Success(entry)
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun getMedicalHistory(
        clinicId: String,
        patientId: String?,
        walkinPatientId: String?
    ): ApiResult<List<MedicalHistoryEntry>> = safeApiCall {
        clinicalApi.getMedicalHistory(clinicId, patientId, walkinPatientId)
    }.let { result ->
        when (result) {
            is ApiResult.Success -> ApiResult.Success(
                (result.data.medicalHistory ?: emptyList()).map { it.toDomain() }
            )
            is ApiResult.Error -> result
        }
    }
}
