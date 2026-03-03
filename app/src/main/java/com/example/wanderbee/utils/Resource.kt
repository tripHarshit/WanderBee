package com.example.wanderbee.utils

/**
 * A generic wrapper for all remote API calls across every microservice.
 *
 * Usage in repositories:
 * ```
 * return try {
 *     val response = api.getSomething()
 *     if (response.isSuccessful) Resource.Success(response.body()!!)
 *     else Resource.httpError(response.code())
 * } catch (e: IOException) {
 *     Resource.Error("No internet connection. Please check your network.")
 * } catch (e: HttpException) {
 *     Resource.httpError(e.code())
 * }
 * ```
 *
 * - [Loading]  : request in flight; optional stale [data] for cached screens.
 * - [Success]  : HTTP 2xx, body available.
 * - [Error]    : any failure; human-readable [message] + optional raw [statusCode].
 */
sealed class Resource<out T> {

    data class Loading<T>(val data: T? = null) : Resource<T>()

    data class Success<T>(val data: T) : Resource<T>()

    data class Error(
        val message: String,
        val statusCode: Int? = null
    ) : Resource<Nothing>()

    companion object {
        /** Map an HTTP status code to a user-friendly [Error]. */
        fun httpError(code: Int): Error = Error(
            message = when (code) {
                400 -> "Bad request. Please check your input."
                401 -> "Session expired. Please log in again."
                403 -> "You don't have permission to do that."
                404 -> "The requested resource was not found."
                408 -> "Request timed out. Please try again."
                429 -> "Too many requests. Please wait a moment."
                500 -> "Internal server error. Please try again later."
                502 -> "Bad gateway. A backend service returned an invalid response."
                503 -> "Service unavailable. A required service is down. Please try again later."
                504 -> "Gateway timeout. The API Gateway could not reach the service in time."
                else -> "Unexpected server error (HTTP $code)."
            },
            statusCode = code
        )

        /** Shorthand for a [Loading] with no stale data. */
        fun <T> loading(): Resource<T> = Loading()
    }
}
