package com.oralvis.oralvisclient.data.repository.mapper

import com.oralvis.oralvisclient.data.remote.dto.AttachmentDto
import com.oralvis.oralvisclient.data.remote.dto.ClinicalRecordDto
import com.oralvis.oralvisclient.data.remote.dto.MedicalHistoryEntryDto
import com.oralvis.oralvisclient.data.remote.dto.TextWithAttachmentsDto
import com.oralvis.oralvisclient.domain.model.Attachment
import com.oralvis.oralvisclient.domain.model.ClinicalRecord
import com.oralvis.oralvisclient.domain.model.MedicalHistoryEntry
import com.oralvis.oralvisclient.domain.model.TextWithAttachments

fun ClinicalRecordDto.toDomain(): ClinicalRecord = ClinicalRecord(
    id = id,
    bookingId = bookingId ?: "",
    complaints = complaints?.toDomain(),
    observations = observations?.toDomain(),
    diagnoses = diagnoses,
    notes = notes,
    prescriptions = prescriptions,
    vitalSigns = vitalSigns,
    labOrders = labOrders,
    files = files,
    treatmentPlan = treatmentPlan,
    shareWithPatient = shareWithPatient ?: false
)

private fun TextWithAttachmentsDto.toDomain(): TextWithAttachments = TextWithAttachments(
    text = text,
    attachments = attachments?.map { it.toDomain() }
)

private fun AttachmentDto.toDomain(): Attachment = Attachment(
    url = url,
    type = type,
    uploadedAt = uploadedAt
)

fun MedicalHistoryEntryDto.toDomain(): MedicalHistoryEntry = MedicalHistoryEntry(
    id = id,
    clinicId = clinicId ?: "",
    patientId = patientId,
    walkinPatientId = walkinPatientId,
    condition = condition ?: "",
    details = details
)
