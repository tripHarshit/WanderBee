package com.example.wanderbee.data.remote.models.auth

/**
 * Response body for GET /auth/validate.
 *
 * The identity-service validates the JWT and returns the caller's
 * core profile fields.  Extended preferences (travelStyle, etc.) are
 * stored locally in the Room [ProfileEntity].
 */
data class ValidateTokenResponse(
    val email: String,
    val name: String,
    val roles: List<String> = emptyList()
)
