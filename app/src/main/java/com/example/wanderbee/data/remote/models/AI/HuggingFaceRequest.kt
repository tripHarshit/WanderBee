package com.example.wanderbee.data.remote.models.AI

import com.google.gson.annotations.SerializedName

data class HuggingFaceRequest(
    @SerializedName("inputs")
    val inputs: String,

    // Optional parameters depending on the model
    @SerializedName("parameters")
    val parameters: Map<String, Any>? = null,

    @SerializedName("options")
    val options: Map<String, Any>? = null
)
