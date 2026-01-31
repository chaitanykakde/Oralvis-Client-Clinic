package com.oralvis.oralvisclient.domain.model

/**
 * Appointment/booking (from appointments list, bookings-by-date, etc.).
 */
data class Booking(
    val id: String,
    val patientName: String,
    val appointmentDate: String,
    val slotTime: String,
    val status: BookingStatus,
    val notes: String? = null,
    val paymentId: String? = null,
    val durationMinutes: Int? = null,
    val clinicId: String? = null,
    val patientId: String? = null,
    val walkinPatientId: String? = null,
    val amountPaid: Double? = null,
    val refundStatus: String? = null
)

enum class BookingStatus {
    PENDING,
    CONFIRMED,
    COMPLETED,
    CANCELLED,
    PAID,
    REFUND_REQUESTED,
    REFUNDED,
    CANCELLED_NO_REFUND,
    UNKNOWN
}

fun String.toBookingStatus(): BookingStatus = when (this.lowercase()) {
    "pending" -> BookingStatus.PENDING
    "confirmed" -> BookingStatus.CONFIRMED
    "completed" -> BookingStatus.COMPLETED
    "cancelled" -> BookingStatus.CANCELLED
    "paid" -> BookingStatus.PAID
    "refund-requested" -> BookingStatus.REFUND_REQUESTED
    "refunded" -> BookingStatus.REFUNDED
    "cancelled-no-refund" -> BookingStatus.CANCELLED_NO_REFUND
    else -> BookingStatus.UNKNOWN
}
