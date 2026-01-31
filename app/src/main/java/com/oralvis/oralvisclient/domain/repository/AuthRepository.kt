package com.oralvis.oralvisclient.domain.repository

import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.domain.model.User

interface AuthRepository {
    suspend fun login(phoneNo: String?, email: String?, password: String): ApiResult<User>
    suspend fun refreshToken(): ApiResult<Unit>
    suspend fun getCurrentUser(): ApiResult<User>
    suspend fun logout(): ApiResult<Unit>
    suspend fun startClinicRegistration(
        name: String,
        phoneNo: String,
        clinicemail: String,
        clinicpassword: String,
        website: String?
    ): ApiResult<Unit>
    suspend fun verifyClinicRegistration(phoneNo: String, otp: String): ApiResult<User>
    suspend fun sendClinicOtp(phoneNo: String): ApiResult<Unit>
}
