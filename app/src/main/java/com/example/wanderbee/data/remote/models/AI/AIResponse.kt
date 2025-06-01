package com.example.wanderbee.data.remote.models.AI



data class AiChoice(
    val message: AiMessage,
    val finish_reason: String,
    val index: Int
)

data class AiUsage(
    val prompt_tokens: Int,
    val completion_tokens: Int,
    val total_tokens: Int
)

data class AiResponse(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val usage: AiUsage,
    val choices: List<AiChoice>
)
