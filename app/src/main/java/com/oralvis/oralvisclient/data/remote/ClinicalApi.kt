package com.oralvis.oralvisclient.data.remote

import com.oralvis.oralvisclient.data.remote.dto.ClinicalRecordDto
import com.oralvis.oralvisclient.data.remote.dto.GetClinicalRecordResponse
import com.oralvis.oralvisclient.data.remote.dto.GetMedicalHistoryResponse
import com.oralvis.oralvisclient.data.remote.dto.MedicalHistoryEntryDto
import com.oralvis.oralvisclient.data.remote.dto.SaveClinicalRecordRequest
import com.oralvis.oralvisclient.data.remote.dto.SaveClinicalRecordResponse
import com.oralvis.oralvisclient.data.remote.dto.SaveMedicalHistoryRequest
import com.oralvis.oralvisclient.data.remote.dto.UploadFileResponse
import com.oralvis.oralvisclient.data.remote.dto.UploadPrescriptionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ClinicalApi {

    @GET("api/clinics/bookings/{bookingId}/clinical-records")
    suspend fun getClinicalRecord(@Path("bookingId") bookingId: String): Response<GetClinicalRecordResponse>

    @POST("api/clinics/bookings/{bookingId}/clinical-records")
    suspend fun saveClinicalRecord(
        @Path("bookingId") bookingId: String,
        @Body body: SaveClinicalRecordRequest
    ): Response<SaveClinicalRecordResponse>

    @Multipart
    @POST("api/clinics/bookings/{bookingId}/clinical-records/upload")
    suspend fun uploadClinicalAttachment(
        @Path("bookingId") bookingId: String,
        @Part("type") type: RequestBody,
        @Part("field") field: RequestBody,
        @Part file: MultipartBody.Part
    ): Response<UploadAttachmentResponse>

    @Multipart
    @POST("api/clinics/bookings/{bookingId}/clinical-records/upload-prescription-image")
    suspend fun uploadPrescriptionImage(
        @Path("bookingId") bookingId: String,
        @Part file: MultipartBody.Part,
        @Part("prescriptionIndex") prescriptionIndex: RequestBody?
    ): Response<UploadPrescriptionResponse>

    @Multipart
    @POST("api/clinics/bookings/{bookingId}/clinical-records/upload-file")
    suspend fun uploadFile(
        @Path("bookingId") bookingId: String,
        @Part file: MultipartBody.Part
    ): Response<UploadFileResponse>

    @POST("api/clinics/medical-history")
    suspend fun saveMedicalHistory(@Body body: SaveMedicalHistoryRequest): Response<SaveMedicalHistoryResponse>

    @GET("api/clinics/medical-history/{clinicId}")
    suspend fun getMedicalHistory(
        @Path("clinicId") clinicId: String,
        @Query("patientId") patientId: String? = null,
        @Query("walkinPatientId") walkinPatientId: String? = null
    ): Response<GetMedicalHistoryResponse>
}

data class UploadAttachmentResponse(
    val message: String? = null,
    val attachment: Any? = null,
    val clinicalRecord: ClinicalRecordDto? = null
)

data class SaveMedicalHistoryResponse(
    val message: String? = null,
    val medicalHistory: MedicalHistoryEntryDto? = null
)
