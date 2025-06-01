package com.example.wanderbee.data.remote.models.AI

import com.google.gson.annotations.SerializedName

typealias HuggingFaceResponse = List<GeneratedTextResult>

data class GeneratedTextResult(
    @SerializedName("generated_text")
    val generatedText: String
)
