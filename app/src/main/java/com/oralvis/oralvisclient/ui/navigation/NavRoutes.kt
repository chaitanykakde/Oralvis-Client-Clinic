package com.oralvis.oralvisclient.ui.navigation

object NavRoutes {
    const val Login = "login"
    const val Home = "home"
    const val Patients = "patients"
    const val PatientDetails = "patient_details"
    const val Calendar = "calendar"
    const val AppointmentDetails = "appointment_details"
    const val AppointmentForm = "appointment_form"
    const val Summary = "summary"
    const val SelectBooking = "select_booking"
    const val AddPrescription = "add_prescription"
    const val AddFile = "add_file"
    const val AddBill = "add_bill"

    fun appointmentDetails(bookingId: String) = "appointment_details/$bookingId"
    fun patientDetails(patientId: String) = "patient_details/$patientId"
    fun selectBooking(action: String) = "select_booking/$action"
    fun addPrescription(bookingId: String) = "add_prescription/$bookingId"
    fun addFile(bookingId: String) = "add_file/$bookingId"
    fun addBill(bookingId: String) = "add_bill/$bookingId"
}
