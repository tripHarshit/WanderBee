package com.example.wanderbee.data.remote.models.auth

data class GoogleAuthResponse(
    val token: String,
    val email: String,
    val name: String,
    val newUser: Boolean
)
