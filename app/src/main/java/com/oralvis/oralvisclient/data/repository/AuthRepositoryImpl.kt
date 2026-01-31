package com.oralvis.oralvisclient.data.repository

import com.oralvis.oralvisclient.core.network.ApiClient
import com.oralvis.oralvisclient.core.network.ApiResult
import com.oralvis.oralvisclient.core.network.safeApiCall
import com.oralvis.oralvisclient.core.session.SessionManager
import com.oralvis.oralvisclient.data.remote.AuthApi
import com.oralvis.oralvisclient.data.remote.dto.ClinicStartRegistrationRequest
import com.oralvis.oralvisclient.data.remote.dto.ClinicVerifyRegistrationRequest
import com.oralvis.oralvisclient.data.remote.dto.LoginRequest
import com.oralvis.oralvisclient.data.remote.dto.SendOtpRequest
import com.oralvis.oralvisclient.data.repository.mapper.toDomain
import com.oralvis.oralvisclient.domain.model.User
import com.oralvis.oralvisclient.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(phoneNo: String?, email: String?, password: String): ApiResult<User> {
        val result = safeApiCall {
            authApi.login(LoginRequest(phoneNo = phoneNo, email = email, password = password))
        }
        return when (result) {
            is ApiResult.Success -> {
                val user = result.data.user?.toDomain()
                if (user == null) ApiResult.Error("Invalid response", null)
                else {
                    sessionManager.setUser(user)
                    ApiResult.Success(user)
                }
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun refreshToken(): ApiResult<Unit> = safeApiCall {
        authApi.refreshToken()
    }.let { if (it is ApiResult.Success) ApiResult.Success(Unit) else it as ApiResult.Error }

    override suspend fun getCurrentUser(): ApiResult<User> {
        val result = safeApiCall { authApi.getCurrentUser() }
        return when (result) {
            is ApiResult.Success -> {
                val user = result.data.toDomain()
                sessionManager.setUser(user)
                ApiResult.Success(user)
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun logout(): ApiResult<Unit> {
        val result = safeApiCall { authApi.logout() }
        if (result is ApiResult.Success) {
            sessionManager.clear()
            ApiClient.clearCookies()
        }
        return result.let { if (it is ApiResult.Success) ApiResult.Success(Unit) else it as ApiResult.Error }
    }

    override suspend fun startClinicRegistration(
        name: String,
        phoneNo: String,
        clinicemail: String,
        clinicpassword: String,
        website: String?
    ): ApiResult<Unit> = safeApiCall {
        authApi.startClinicRegistration(
            ClinicStartRegistrationRequest(
                name = name,
                phoneNo = phoneNo,
                clinicemail = clinicemail,
                clinicpassword = clinicpassword,
                website = website
            )
        )
    }.let { if (it is ApiResult.Success) ApiResult.Success(Unit) else it as ApiResult.Error }

    override suspend fun verifyClinicRegistration(phoneNo: String, otp: String): ApiResult<User> {
        val result = safeApiCall {
            authApi.verifyClinicRegistration(ClinicVerifyRegistrationRequest(phoneNo = phoneNo, otp = otp))
        }
        return when (result) {
            is ApiResult.Success -> {
                val user = result.data.user?.toDomain()
                if (user == null) ApiResult.Error("Invalid response", null)
                else {
                    sessionManager.setUser(user)
                    ApiResult.Success(user)
                }
            }
            is ApiResult.Error -> result
        }
    }

    override suspend fun sendClinicOtp(phoneNo: String): ApiResult<Unit> = safeApiCall {
        authApi.sendClinicOtp(SendOtpRequest(phoneNo = phoneNo))
    }.let { if (it is ApiResult.Success) ApiResult.Success(Unit) else it as ApiResult.Error }
}
