package com.example.codesageai.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface Judge0ApiService {
    @POST("submissions")
    suspend fun executeCode(
        @Body request: Judge0SubmissionRequest,
        @Query("base64_encoded") base64Encoded: Boolean = false,
        @Query("wait") wait: Boolean = true,
        @Header("X-RapidAPI-Key") rapidApiKey: String? = null,
        @Header("X-RapidAPI-Host") rapidApiHost: String? = null
    ): Judge0Response
}
