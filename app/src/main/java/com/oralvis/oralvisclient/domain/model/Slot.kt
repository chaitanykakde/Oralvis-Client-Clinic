package com.oralvis.oralvisclient.domain.model

/**
 * Time slot for a clinic on a date (from slotss API).
 */
data class Slot(
    val id: String,
    val clinicId: String,
    val date: String,
    val time: String,
    val isAvailable: Boolean
)
