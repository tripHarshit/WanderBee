package com.example.wanderbee.data.remote.apiService

import com.example.wanderbee.data.remote.models.auth.AuthRequest
import com.example.wanderbee.data.remote.models.auth.GoogleAuthResponse
import com.example.wanderbee.data.remote.models.auth.GoogleTokenRequest
import com.example.wanderbee.data.remote.models.auth.UserCredentials
import com.example.wanderbee.data.remote.models.auth.ValidateTokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface IdentityApiService {

    @POST("auth/register")
    suspend fun register(@Body user: UserCredentials): Response<String>

    @POST("auth/login")
    suspend fun login(@Body authRequest: AuthRequest): Response<String>

    @POST("auth/token")
    suspend fun getToken(@Body authRequest: AuthRequest): Response<String>

    @POST("auth/google-login")
    suspend fun googleLogin(@Body request: GoogleTokenRequest): Response<GoogleAuthResponse>

    /**
     * Validates the JWT carried in the Authorization header (added by [AuthInterceptor])
     * and returns the caller's basic profile — email, name, and roles.
     * Used by [ProfileViewModel] to get fresh user info without Firebase.
     */
    @GET("auth/validate")
    suspend fun validateToken(): Response<ValidateTokenResponse>
}
