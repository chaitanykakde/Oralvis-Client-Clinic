package com.oralvis.oralvisclient.data.repository.mapper

import com.oralvis.oralvisclient.data.remote.dto.SlotDto
import com.oralvis.oralvisclient.domain.model.Slot

fun SlotDto.toDomain(): Slot = Slot(
    id = id,
    clinicId = clinic ?: "",
    date = date ?: "",
    time = time ?: "",
    isAvailable = isAvailable ?: true
)
