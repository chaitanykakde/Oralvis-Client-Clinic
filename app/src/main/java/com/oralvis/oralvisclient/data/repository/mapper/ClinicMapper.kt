package com.oralvis.oralvisclient.data.repository.mapper

import com.oralvis.oralvisclient.data.remote.dto.AboutDto
import com.oralvis.oralvisclient.data.remote.dto.ClinicDto
import com.oralvis.oralvisclient.data.remote.dto.LocationDto
import com.oralvis.oralvisclient.domain.model.Clinic
import com.oralvis.oralvisclient.domain.model.ClinicAbout
import com.oralvis.oralvisclient.domain.model.ClinicLocation

fun ClinicDto.toDomain(): Clinic = Clinic(
    id = id,
    name = name,
    mainarea = mainarea,
    address = address,
    phoneNo = phoneNo,
    city = city,
    image = image,
    coverimage = coverimage,
    fees = fees ?: 0,
    owner = owner,
    mainDoctor = mainDoctor?.toString(),
    dentists = dentists,
    about = about?.toDomain(),
    location = location?.toDomain(),
    services = services,
    introline = introline,
    noofpatients = noofpatients,
    yearsofexp = yearsofexp
)

private fun AboutDto.toDomain(): ClinicAbout = ClinicAbout(
    parah = parah,
    points_to_be_highlighted = points_to_be_highlighted
)

private fun LocationDto.toDomain(): ClinicLocation = ClinicLocation(
    type = type,
    coordinates = coordinates
)
