package com.example.wanderbee.data.remote.models.huggingFace

import com.google.gson.annotations.SerializedName

typealias HuggingFaceResponse = List<GeneratedTextResult>

data class GeneratedTextResult(
    @SerializedName("generated_text")
    val generatedText: String
)
