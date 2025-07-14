package com.example.wanderbee.data.repository

import android.content.Context
import android.net.Uri
import com.example.wanderbee.data.remote.RetrofitInstance.imgbbApi
import com.example.wanderbee.data.remote.apiService.ImgBBApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

interface  ImgBBRepository{
    suspend fun uploadToImgBB(
        context: Context,
        imageUri: Uri): String
}

class DefaultImgBBRepository @Inject constructor(
    private val imgBBApiService: ImgBBApiService
): ImgBBRepository{
    override suspend fun uploadToImgBB(context: Context, imageUri: Uri): String {
        val apiKey = "60cbb7ac33855887f4f745dc9280a327"

        val contentResolver = context.contentResolver
        val inputStream = contentResolver.openInputStream(imageUri)!!
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        outputStream.close()

        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", tempFile.name, requestFile)

        val response = imgbbApi.uploadImage(
            apiKey = apiKey,
            image = imagePart,
            name = tempFile.name.removeSuffix(".jpg").toRequestBody("text/plain".toMediaTypeOrNull()),
            expiration = null // or set to 600 (10 min), 3600 (1 hour), etc.
        )

        tempFile.delete()

        if (!response.success) {
            throw Exception("Upload failed with status ${response.status}")
        }

        return response.data.url
    }
}