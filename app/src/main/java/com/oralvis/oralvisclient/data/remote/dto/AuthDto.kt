package com.oralvis.oralvisclient.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("phoneNo") val phoneNo: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("message") val message: String? = null,
    @SerializedName("user") val user: UserDto? = null
)

data class UserDto(
    @SerializedName("_id") val id: String? = null,
    @SerializedName("id") val idAlt: String? = null,
    @SerializedName("name") val name: String,
    @SerializedName("phoneNo") val phoneNo: String,
    @SerializedName("role") val role: String,
    @SerializedName("email") val email: String? = null,
    @SerializedName("image") val image: String? = null,
    @SerializedName("lastLogin") val lastLogin: String? = null,
    @SerializedName("clinicId") val clinicId: String? = null,
    @SerializedName("clinics") val clinics: List<String>? = null
) {
    /** Backend may send "_id" (Mongo) or "id"; accept both. */
    fun resolvedId(): String = (id?.takeIf { it.isNotBlank() } ?: idAlt).orEmpty()
}

data class RefreshResponse(
    @SerializedName("message") val message: String? = null
)

data class LogoutResponse(
    @SerializedName("message") val message: String? = null
)

data class ClinicStartRegistrationRequest(
    @SerializedName("name") val name: String,
    @SerializedName("phoneNo") val phoneNo: String,
    @SerializedName("clinicemail") val clinicemail: String,
    @SerializedName("clinicpassword") val clinicpassword: String,
    @SerializedName("website") val website: String? = null
)

data class ClinicVerifyRegistrationRequest(
    @SerializedName("phoneNo") val phoneNo: String,
    @SerializedName("otp") val otp: String
)

data class SendOtpRequest(
    @SerializedName("phoneNo") val phoneNo: String
)
