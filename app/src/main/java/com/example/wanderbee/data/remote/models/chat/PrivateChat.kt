package com.example.wanderbee.data.remote.models.chat

data class PrivateChat(
    val id: String = "",
    val users: List<String> = emptyList()
)
