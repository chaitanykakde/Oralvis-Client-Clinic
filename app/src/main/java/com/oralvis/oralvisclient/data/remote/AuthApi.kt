package com.oralvis.oralvisclient.data.remote

import com.oralvis.oralvisclient.data.remote.dto.ClinicStartRegistrationRequest
import com.oralvis.oralvisclient.data.remote.dto.ClinicVerifyRegistrationRequest
import com.oralvis.oralvisclient.data.remote.dto.LoginRequest
import com.oralvis.oralvisclient.data.remote.dto.LoginResponse
import com.oralvis.oralvisclient.data.remote.dto.LogoutResponse
import com.oralvis.oralvisclient.data.remote.dto.RefreshResponse
import com.oralvis.oralvisclient.data.remote.dto.SendOtpRequest
import com.oralvis.oralvisclient.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {

    @POST("api/login")
    suspend fun login(@Body body: LoginRequest): Response<LoginResponse>

    @POST("api/refresh-token")
    suspend fun refreshToken(): Response<RefreshResponse>

    @GET("api/me")
    suspend fun getCurrentUser(): Response<UserDto>

    @POST("api/logout")
    suspend fun logout(): Response<LogoutResponse>

    @POST("api/clinics/start-registration")
    suspend fun startClinicRegistration(@Body body: ClinicStartRegistrationRequest): Response<MessageResponse>

    @POST("api/clinics/verify-registration")
    suspend fun verifyClinicRegistration(@Body body: ClinicVerifyRegistrationRequest): Response<LoginResponse>

    @POST("api/clinics/send-otp")
    suspend fun sendClinicOtp(@Body body: SendOtpRequest): Response<MessageResponse>
}

data class MessageResponse(val message: String? = null)
