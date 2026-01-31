package com.oralvis.oralvisclient.data.repository.mapper

import com.oralvis.oralvisclient.data.remote.dto.BookingDto
import com.oralvis.oralvisclient.domain.model.Booking
import com.oralvis.oralvisclient.domain.model.toBookingStatus

fun BookingDto.toDomain(clinicId: String? = null): Booking = Booking(
    id = id,
    patientName = patientName ?: "Unknown",
    appointmentDate = appointmentDateString(),
    slotTime = slotTime ?: "",
    status = (status ?: "").toBookingStatus(),
    notes = notes,
    paymentId = paymentId,
    durationMinutes = durationMinutes,
    clinicId = clinicId,
    amountPaid = amountPaid,
    refundStatus = refundStatus
)
