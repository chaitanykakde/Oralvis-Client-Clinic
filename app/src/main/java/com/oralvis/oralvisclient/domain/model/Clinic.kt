package com.oralvis.oralvisclient.domain.model

/**
 * Clinic profile (from clinic-profile or clinic by id).
 */
data class Clinic(
    val id: String,
    val name: String,
    val mainarea: String? = null,
    val address: String? = null,
    val phoneNo: String? = null,
    val city: String? = null,
    val image: String? = null,
    val coverimage: String? = null,
    val fees: Int = 0,
    val owner: String? = null,
    val mainDoctor: String? = null,
    val dentists: List<String>? = null,
    val about: ClinicAbout? = null,
    val location: ClinicLocation? = null,
    val services: List<String>? = null,
    val introline: String? = null,
    val noofpatients: Int? = null,
    val yearsofexp: Int? = null
)

data class ClinicAbout(
    val parah: String? = null,
    val points_to_be_highlighted: List<String>? = null
)

data class ClinicLocation(
    val type: String? = null,
    val coordinates: List<Double>? = null
)
