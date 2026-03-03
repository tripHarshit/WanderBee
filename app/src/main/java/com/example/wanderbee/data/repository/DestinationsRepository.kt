package com.example.wanderbee.data.repository

import android.util.Log
import com.example.wanderbee.data.remote.apiService.DestinationApiService
import com.example.wanderbee.data.remote.models.destinations.BackendCity
import com.example.wanderbee.data.remote.models.destinations.City
import com.example.wanderbee.data.remote.models.destinations.GeoDbResponse
import com.example.wanderbee.data.remote.models.destinations.SaveDestinationRequest
import com.example.wanderbee.data.remote.models.destinations.SavedDestinationResponse
import com.example.wanderbee.data.remote.models.destinations.StaticDestination
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

/**
 * Sealed result wrapper so callers can distinguish success from specific failures.
 */
sealed class DestinationResult<out T> {
    data class Success<T>(val data: T) : DestinationResult<T>()
    data class Error(val message: String) : DestinationResult<Nothing>()
}

interface DestinationsRepository {
    suspend fun getPopularCities(limit: Int): DestinationResult<GeoDbResponse>
    suspend fun getNearbyCities(latLong: String, limit: Int): DestinationResult<GeoDbResponse>
    suspend fun searchCities(
        namePrefix: String,
        limit: Int = 10,
        offset: Int = 0
    ): DestinationResult<GeoDbResponse>
    suspend fun getStaticDestinations(name: String): DestinationResult<List<StaticDestination>>
    suspend fun getIndianDestinations(): DestinationResult<List<StaticDestination>>
    suspend fun getAllDestinations(): DestinationResult<List<StaticDestination>>
    suspend fun saveDestination(cityId: String, cityName: String): DestinationResult<SavedDestinationResponse>
    suspend fun unsaveDestination(cityId: String): DestinationResult<Unit>
    suspend fun getSavedDestinations(userId: String): DestinationResult<List<SavedDestinationResponse>>
}

class DestinationRepository @Inject constructor(
    private val destinationApi: DestinationApiService
) : DestinationsRepository {

    companion object {
        private const val TAG = "DestinationRepository"
    }

    override suspend fun getPopularCities(limit: Int): DestinationResult<GeoDbResponse> {
        return try {
            val response = destinationApi.getPopularCities(limit)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    DestinationResult.Success(body.toGeoDbResponse())
                } else {
                    DestinationResult.Error("Empty response from server")
                }
            } else {
                handleHttpError(response.code(), "getPopularCities")
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout fetching popular cities", e)
            DestinationResult.Error("Connection timed out. Please try again.")
        } catch (e: IOException) {
            Log.e(TAG, "No network – getPopularCities", e)
            DestinationResult.Error("No internet connection. Please check your network.")
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP ${e.code()} – getPopularCities", e)
            DestinationResult.Error(httpErrorMessage(e.code()))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching popular cities", e)
            DestinationResult.Error("Unable to reach the server. Please check your connection.")
        }
    }

    override suspend fun getNearbyCities(latLong: String, limit: Int): DestinationResult<GeoDbResponse> {
        return try {
            val response = destinationApi.getNearbyCities(latLong, limit)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    DestinationResult.Success(body.toGeoDbResponse())
                } else {
                    DestinationResult.Error("Empty response from server")
                }
            } else {
                handleHttpError(response.code(), "getNearbyCities")
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout fetching nearby cities", e)
            DestinationResult.Error("Connection timed out. Please try again.")
        } catch (e: IOException) {
            Log.e(TAG, "No network – getNearbyCities", e)
            DestinationResult.Error("No internet connection. Please check your network.")
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP ${e.code()} – getNearbyCities", e)
            DestinationResult.Error(httpErrorMessage(e.code()))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching nearby cities", e)
            DestinationResult.Error("Unable to reach the server. Please check your connection.")
        }
    }

    override suspend fun searchCities(
        namePrefix: String,
        limit: Int,
        offset: Int
    ): DestinationResult<GeoDbResponse> {
        return try {
            val response = destinationApi.searchCities(namePrefix, limit, offset)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    DestinationResult.Success(body.toGeoDbResponse())
                } else {
                    DestinationResult.Error("Empty response from server")
                }
            } else {
                handleHttpError(response.code(), "searchCities")
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout searching cities", e)
            DestinationResult.Error("Connection timed out. Please try again.")
        } catch (e: IOException) {
            Log.e(TAG, "No network – searchCities", e)
            DestinationResult.Error("No internet connection. Please check your network.")
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP ${e.code()} – searchCities", e)
            DestinationResult.Error(httpErrorMessage(e.code()))
        } catch (e: Exception) {
            Log.e(TAG, "Error searching cities", e)
            DestinationResult.Error("Unable to reach the server. Please check your connection.")
        }
    }

    override suspend fun getStaticDestinations(name: String): DestinationResult<List<StaticDestination>> {
        return try {
            val response = destinationApi.getStaticDestinations(name)
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                    DestinationResult.Success(body)
                } else {
                    DestinationResult.Error("Empty response from server")
                }
            } else {
                handleHttpError(response.code(), "getStaticDestinations")
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout fetching static destinations", e)
            DestinationResult.Error("Connection timed out. Please try again.")
        } catch (e: IOException) {
            Log.e(TAG, "No network – getStaticDestinations", e)
            DestinationResult.Error("No internet connection. Please check your network.")
        } catch (e: HttpException) {
            Log.e(TAG, "HTTP ${e.code()} – getStaticDestinations", e)
            DestinationResult.Error(httpErrorMessage(e.code()))
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching static destinations", e)
            DestinationResult.Error("Unable to reach the server. Please check your connection.")
        }
    }

    override suspend fun getIndianDestinations(): DestinationResult<List<StaticDestination>> {
        return getStaticDestinations("india")
    }

    override suspend fun getAllDestinations(): DestinationResult<List<StaticDestination>> {
        return getStaticDestinations("all")
    }

    override suspend fun saveDestination(
        cityId: String,
        cityName: String
    ): DestinationResult<SavedDestinationResponse> {
        return try {
            val response = destinationApi.saveDestination(SaveDestinationRequest(cityId, cityName))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) DestinationResult.Success(body)
                else DestinationResult.Error("Empty response from server")
            } else {
                // 409 = already saved, treat as success-ish
                if (response.code() == 409) {
                    Log.w(TAG, "Destination already saved on server")
                    DestinationResult.Error("Already saved")
                } else {
                    handleHttpError(response.code(), "saveDestination")
                }
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout saving destination", e)
            DestinationResult.Error("Connection timed out. Please try again.")
        } catch (e: IOException) {
            Log.e(TAG, "No network – saveDestination", e)
            DestinationResult.Error("No internet connection. Please check your network.")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving destination", e)
            DestinationResult.Error("Unable to save destination: ${e.message}")
        }
    }

    override suspend fun unsaveDestination(cityId: String): DestinationResult<Unit> {
        return try {
            val response = destinationApi.unsaveDestination(cityId)
            if (response.isSuccessful) {
                DestinationResult.Success(Unit)
            } else {
                handleHttpError(response.code(), "unsaveDestination")
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout unsaving destination", e)
            DestinationResult.Error("Connection timed out. Please try again.")
        } catch (e: IOException) {
            Log.e(TAG, "No network – unsaveDestination", e)
            DestinationResult.Error("No internet connection. Please check your network.")
        } catch (e: Exception) {
            Log.e(TAG, "Error unsaving destination", e)
            DestinationResult.Error("Unable to unsave destination: ${e.message}")
        }
    }

    override suspend fun getSavedDestinations(userId: String): DestinationResult<List<SavedDestinationResponse>> {
        return try {
            val response = destinationApi.getSavedDestinations(userId)
            if (response.isSuccessful) {
                DestinationResult.Success(response.body() ?: emptyList())
            } else {
                handleHttpError(response.code(), "getSavedDestinations")
            }
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "Timeout fetching saved destinations", e)
            DestinationResult.Error("Connection timed out. Please try again.")
        } catch (e: IOException) {
            Log.e(TAG, "No network – getSavedDestinations", e)
            DestinationResult.Error("No internet connection. Please check your network.")
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching saved destinations", e)
            DestinationResult.Error("Unable to fetch saved destinations: ${e.message}")
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private fun handleHttpError(code: Int, source: String): DestinationResult.Error {
        Log.e(TAG, "$source failed with HTTP $code")
        return DestinationResult.Error(httpErrorMessage(code))
    }
}

/** Shared HTTP→human message mapping for destination-service and chat-service. */
internal fun httpErrorMessage(code: Int): String = when (code) {
    400 -> "Bad request. Please check your input."
    401 -> "Session expired. Please log in again."
    403 -> "You don't have permission to do that."
    404 -> "The requested resource was not found."
    408 -> "Request timed out. Please try again."
    429 -> "Too many requests. Please wait a moment."
    500 -> "Internal server error. Please try again later."
    502 -> "Bad gateway. A backend service returned an invalid response."
    503 -> "Service unavailable. The destination service is currently down. Please try again later."
    504 -> "Gateway timeout. The API Gateway (port 8082) could not reach the destination service in time."
    else -> "Unexpected server error (HTTP $code)."
}


/**
 * Extension to convert the backend DTO to the existing client model
 * so that downstream layers (ViewModel / UI) stay unchanged.
 */
private fun com.example.wanderbee.data.remote.models.destinations.BackendGeoDbResponse.toGeoDbResponse(): GeoDbResponse {
    return GeoDbResponse(
        data = this.data.map { it.toCity() }
    )
}

private fun BackendCity.toCity(): City {
    return City(
        id = this.id,
        name = this.name,
        country = this.country,
        latitude = this.latitude,
        longitude = this.longitude
    )
}





