package com.example.wanderbee.data.remote

import com.example.wanderbee.utils.AppPreferences
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * OkHttp interceptor that attaches two security headers to every authenticated request:
 *
 * 1. `Authorization: Bearer <jwt>` — required by the API gateway's AuthenticationFilter.
 * 2. `X-User-Id: <email>` — the gateway also forwards this to downstream microservices
 *    (chat-service, destination-service). Including it explicitly from the client makes
 *    the contract clear and ensures compatibility if any service validates it directly.
 *
 * Public endpoints still pass through; the gateway's RouteValidator skips them.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val appPreferences: AppPreferences
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val token = runBlocking { appPreferences.getJwtTokenOnce() }
        val email = runBlocking { appPreferences.getUserEmailOnce() }

        val request = if (!token.isNullOrBlank()) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .apply { if (!email.isNullOrBlank()) header("X-User-Id", email) }
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(request)
    }
}
