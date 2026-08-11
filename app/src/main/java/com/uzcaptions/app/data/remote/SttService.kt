package com.uzcaptions.app.data.remote

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/** https://service.muxlisa.uz/api/v2/stt — speech-to-text endpoint. */
interface SttService {
    @Multipart
    @POST("api/v2/stt")
    suspend fun speechToText(
        @Header("x-api-key") apiKey: String,
        @Part audio: MultipartBody.Part
    ): Response<SttSuccessResponse>
}
