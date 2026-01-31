package com.oralvis.oralvisclient.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ClinicalRecordDto(
    @SerializedName("_id") val id: String,
    @SerializedName("booking") val bookingId: String? = null,
    @SerializedName("complaints") val complaints: TextWithAttachmentsDto? = null,
    @SerializedName("observations") val observations: TextWithAttachmentsDto? = null,
    @SerializedName("diagnoses") val diagnoses: List<String>? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("prescriptions") val prescriptions: List<Any>? = null,
    @SerializedName("vitalSigns") val vitalSigns: Any? = null,
    @SerializedName("labOrders") val labOrders: List<Any>? = null,
    @SerializedName("files") val files: List<Any>? = null,
    @SerializedName("treatmentPlan") val treatmentPlan: List<Any>? = null,
    @SerializedName("shareWithPatient") val shareWithPatient: Boolean? = null
)

data class TextWithAttachmentsDto(
    @SerializedName("text") val text: String? = null,
    @SerializedName("attachments") val attachments: List<AttachmentDto>? = null
)

data class AttachmentDto(
    @SerializedName("url") val url: String,
    @SerializedName("type") val type: String? = null,
    @SerializedName("uploadedAt") val uploadedAt: String? = null
)

data class SaveClinicalRecordRequest(
    @SerializedName("complaints") val complaints: Any? = null,
    @SerializedName("observations") val observations: Any? = null,
    @SerializedName("diagnoses") val diagnoses: List<String>? = null,
    @SerializedName("notes") val notes: String? = null,
    @SerializedName("prescriptions") val prescriptions: List<Any>? = null,
    @SerializedName("vitalSigns") val vitalSigns: Any? = null,
    @SerializedName("labOrders") val labOrders: List<Any>? = null,
    @SerializedName("files") val files: List<Any>? = null,
    @SerializedName("treatmentPlan") val treatmentPlan: List<Any>? = null,
    @SerializedName("shareWithPatient") val shareWithPatient: Boolean? = null
)

data class SaveClinicalRecordResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("clinicalRecord") val clinicalRecord: ClinicalRecordDto? = null
)

data class GetClinicalRecordResponse(
    @SerializedName("clinicalRecord") val clinicalRecord: ClinicalRecordDto? = null
)

data class UploadAttachmentResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("attachment") val attachment: Any? = null,
    @SerializedName("clinicalRecord") val clinicalRecord: ClinicalRecordDto? = null
)

data class UploadPrescriptionResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("imageUrl") val imageUrl: String? = null
)

data class UploadFileResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("file") val file: UploadedFileDto? = null
)

data class UploadedFileDto(
    @SerializedName("url") val url: String,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fileType") val fileType: String,
    @SerializedName("uploadedAt") val uploadedAt: String? = null
)

data class SaveMedicalHistoryRequest(
    @SerializedName("clinicId") val clinicId: String,
    @SerializedName("patientId") val patientId: String? = null,
    @SerializedName("walkinPatientId") val walkinPatientId: String? = null,
    @SerializedName("condition") val condition: String,
    @SerializedName("details") val details: String? = null
)

data class MedicalHistoryEntryDto(
    @SerializedName("_id") val id: String,
    @SerializedName("clinic") val clinicId: String? = null,
    @SerializedName("patient") val patientId: String? = null,
    @SerializedName("walkinPatient") val walkinPatientId: String? = null,
    @SerializedName("condition") val condition: String? = null,
    @SerializedName("details") val details: String? = null
)

data class GetMedicalHistoryResponse(
    @SerializedName("medicalHistory") val medicalHistory: List<MedicalHistoryEntryDto>? = null
)
