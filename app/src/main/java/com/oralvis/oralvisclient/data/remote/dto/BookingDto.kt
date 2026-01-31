package com.oralvis.oralvisclient.data.remote.dto

import com.google.gson.annotations.SerializedName

data class RescheduleRequest(
    @SerializedName("newSlotId") val newSlotId: String
)

data class RescheduleResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("booking") val booking: BookingDto? = null
)
