package com.oralvis.oralvisclient.data.repository.mapper

import com.oralvis.oralvisclient.data.remote.dto.UserDto
import com.oralvis.oralvisclient.domain.model.User

fun UserDto.toDomain(): User = User(
    id = resolvedId(),
    name = name,
    phoneNo = phoneNo,
    role = role,
    email = email,
    image = image,
    lastLogin = lastLogin,
    clinicId = clinicId ?: clinics?.firstOrNull()
)
