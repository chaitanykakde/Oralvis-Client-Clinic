package com.oralvis.oralvisclient.data.repository.mapper

import com.oralvis.oralvisclient.data.remote.dto.DentistDto
import com.oralvis.oralvisclient.domain.model.Dentist

fun DentistDto.toDomain(): Dentist = Dentist(
    id = id,
    name = name ?: "",
    qualification = qualification,
    image = image
)
