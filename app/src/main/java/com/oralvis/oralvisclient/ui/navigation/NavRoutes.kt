package com.oralvis.oralvisclient.ui.navigation

object NavRoutes {
    const val Home = "home"
    const val Patients = "patients"
    const val PatientDetails = "patient_details"
    const val Calendar = "calendar"
    const val AppointmentDetails = "appointment_details"
    const val AppointmentForm = "appointment_form"
    const val Summary = "summary"

    fun appointmentDetails(bookingId: String) = "appointment_details/$bookingId"
    fun patientDetails(patientId: String) = "patient_details/$patientId"
}
