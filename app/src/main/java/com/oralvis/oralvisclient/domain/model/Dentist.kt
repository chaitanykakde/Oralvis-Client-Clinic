package com.oralvis.oralvisclient.domain.model

data class Dentist(
    val id: String,
    val name: String,
    val qualification: String? = null,
    val image: String? = null
)
