package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.data.remote.models.imageUpload.ImgBBResponse
import dagger.Provides
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

interface ImgBBApiService {
    @Multipart
    @POST("1/upload")
    suspend fun uploadImage(
        @Query("key") apiKey: String,
        @Part image: MultipartBody.Part,
        @Part("name") name: RequestBody? = null,
        @Query("expiration") expiration: Int? = null
    ): ImgBBResponse
}
