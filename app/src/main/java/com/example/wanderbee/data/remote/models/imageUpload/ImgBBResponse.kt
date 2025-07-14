package com.example.wanderbee.data.remote.models.imageUpload

import com.google.gson.annotations.SerializedName

data class ImgBBResponse(
    val data: ImgBBData,
    val success: Boolean,
    val status: Int
)

data class ImgBBData(
    val url: String,
    @SerializedName("display_url")
    val displayUrl: String,
    @SerializedName("delete_url")
    val deleteUrl: String
)
