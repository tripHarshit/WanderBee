package com.example.wanderbee.data.repository

import android.util.Log
import com.example.wanderbee.data.remote.apiService.DestinationApiService
import com.example.wanderbee.data.remote.apiService.GenerationService
import com.example.wanderbee.data.remote.models.AI.CityInsights
import com.example.wanderbee.data.remote.models.destinations.BackendCity
import com.example.wanderbee.data.remote.models.destinations.City
import com.example.wanderbee.data.remote.models.destinations.Country
import com.example.wanderbee.data.local.dao.CityDataDao
import com.example.wanderbee.data.local.entity.CityDataEntity
import com.google.gson.Gson
import javax.inject.Inject

data class CityDetails(
    val city: City,
    val country: Country?,
    val tags: List<String>,
    val timezone: String,
    val currency: String,
    val language: String,
    val description: String = "",
    val bestTimeToVisit: String = "",
    val culturalTips: List<String> = emptyList()
)

class CityDataRepository @Inject constructor(
    private val destinationApiService: DestinationApiService,
    private val generationService: GenerationService,
    private val cityDataDao: CityDataDao
) {
    companion object {
        private const val TAG = "CityDataRepository"
    }

    suspend fun getCityDetails(cityName: String, countryName: String): CityDetails? {
        return try {
            Log.d(TAG, "Fetching city details for: $cityName, $countryName")

            // ── 1. Check Room cache first ────────────────────────────────
            val cityKey = "${cityName}_$countryName"
            val cachedData = cityDataDao.getCityData(cityKey)

            if (cachedData != null) {
                Log.d(TAG, "Found cached data for: $cityKey")
                return CityDetails(
                    city = City(0, cachedData.cityName, cachedData.countryName, 0.0, 0.0),
                    country = null,
                    tags = try {
                        Gson().fromJson(cachedData.tags, Array<String>::class.java).toList()
                    } catch (_: Exception) {
                        emptyList()
                    },
                    timezone = cachedData.timezone,
                    currency = cachedData.currency,
                    language = cachedData.language,
                    description = cachedData.description,
                    culturalTips = try {
                        Gson().fromJson(cachedData.highlights, Array<String>::class.java).toList()
                    } catch (_: Exception) {
                        emptyList()
                    }
                )
            }

            Log.d(TAG, "No cached data found, fetching from backend")

            // ── 2. Search for the city via backend DestinationController ─
            var city: City? = null
            try {
                val searchResponse = destinationApiService.searchCities(
                    namePrefix = cityName,
                    limit = 10
                )
                if (searchResponse.isSuccessful) {
                    val body = searchResponse.body()
                    if (body != null) {
                        Log.d(TAG, "Search response: ${body.data.size} cities found")

                        // Try exact match first
                        val exactMatch = body.data.find {
                            it.name.equals(cityName, ignoreCase = true) &&
                                    it.country.equals(countryName, ignoreCase = true)
                        }
                        // Fallback to city-name-only match
                        val matched = exactMatch ?: body.data.find {
                            it.name.equals(cityName, ignoreCase = true)
                        }

                        // Convert BackendCity → City
                        city = matched?.toCity()

                        if (city != null) {
                            Log.d(TAG, "Found city: ${city.name}, ${city.country}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "City search failed, continuing with defaults", e)
            }

            // Use a fallback City if search didn't find one
            val resolvedCity = city ?: City(0, cityName, countryName, 0.0, 0.0)

            // ── 3. Get insights from backend InsightController ───────────
            val insights = getCityInsightsFromBackend(cityName)

            val result = CityDetails(
                city = resolvedCity,
                country = null,
                tags = listOf(cityName, countryName), // tags from search or default
                timezone = "",
                currency = insights?.currency ?: "Unknown",
                language = insights?.language ?: "Unknown",
                description = insights?.description ?: "",
                bestTimeToVisit = insights?.bestTimeToVisit ?: "",
                culturalTips = insights?.culturalTips ?: emptyList()
            )

            Log.d(TAG, "Final result: currency=${result.currency}, language=${result.language}")

            // ── 4. Cache the result ──────────────────────────────────────
            cacheCityData(cityKey, cityName, countryName, result)

            result
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching city details", e)
            null
        }
    }

    // ── Backend InsightController call ───────────────────────────────────

    private suspend fun getCityInsightsFromBackend(cityName: String): CityInsights? {
        return try {
            Log.d(TAG, "Getting insights for: $cityName")
            val response = generationService.getCityInsights(cityName)

            if (response.isSuccessful) {
                val insights = response.body()
                Log.d(TAG, "Insights loaded: currency=${insights?.currency}, language=${insights?.language}")
                insights
            } else {
                Log.w(TAG, "Insights request failed with HTTP ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting insights from backend", e)
            null
        }
    }

    // ── Room cache helpers ───────────────────────────────────────────────

    private suspend fun cacheCityData(
        cityKey: String,
        cityName: String,
        countryName: String,
        cityDetails: CityDetails
    ) {
        try {
            val cityDataEntity = CityDataEntity(
                cityKey = cityKey,
                cityName = cityName,
                countryName = countryName,
                currency = cityDetails.currency,
                timezone = cityDetails.timezone,
                language = cityDetails.language,
                tags = Gson().toJson(cityDetails.tags),
                description = cityDetails.description,
                highlights = Gson().toJson(cityDetails.culturalTips)
            )

            cityDataDao.insertCityData(cityDataEntity)
            Log.d(TAG, "Cached city data for: $cityKey")

            // Clean up old data (keep only last 100 entries)
            val count = cityDataDao.getCityDataCount()
            if (count > 100) {
                val oldTimestamp = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000) // 7 days ago
                cityDataDao.deleteOldData(oldTimestamp)
                Log.d(TAG, "Cleaned up old city data")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error caching city data", e)
        }
    }
}

/** Convert a backend city DTO to the client-side [City] model. */
private fun BackendCity.toCity(): City = City(
    id = id,
    name = name,
    country = country,
    latitude = latitude,
    longitude = longitude
)