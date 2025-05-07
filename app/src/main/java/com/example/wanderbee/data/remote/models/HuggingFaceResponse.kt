package com.example.wanderbee.data.remote.models

import com.google.gson.annotations.SerializedName

data class HuggingFaceRequest(
    val inputs: String
)

data class HuggingFaceResponse(
    @SerializedName("generated_text")
    val generatedText: String
)