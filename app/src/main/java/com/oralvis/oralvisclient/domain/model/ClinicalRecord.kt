package com.oralvis.oralvisclient.domain.model

/**
 * Clinical record for a booking.
 */
data class ClinicalRecord(
    val id: String,
    val bookingId: String,
    val complaints: TextWithAttachments? = null,
    val observations: TextWithAttachments? = null,
    val diagnoses: List<String>? = null,
    val notes: String? = null,
    val prescriptions: List<Any>? = null,
    val vitalSigns: Any? = null,
    val labOrders: List<Any>? = null,
    val files: List<Any>? = null,
    val treatmentPlan: List<Any>? = null,
    val shareWithPatient: Boolean = false
)

data class TextWithAttachments(
    val text: String? = null,
    val attachments: List<Attachment>? = null
)

data class Attachment(
    val url: String,
    val type: String? = null,
    val uploadedAt: String? = null
)

/**
 * Medical history entry.
 */
data class MedicalHistoryEntry(
    val id: String,
    val clinicId: String,
    val patientId: String? = null,
    val walkinPatientId: String? = null,
    val condition: String,
    val details: String? = null
)
