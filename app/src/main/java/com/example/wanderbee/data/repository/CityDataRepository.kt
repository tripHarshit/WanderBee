package com.example.wanderbee.data.repository

import android.util.Log
import com.example.wanderbee.data.remote.apiService.GeoDbApiService
import com.example.wanderbee.data.remote.apiService.AiApiService
import com.example.wanderbee.data.remote.models.AI.AiRequest
import com.example.wanderbee.data.remote.models.AI.AiResponse
import com.example.wanderbee.data.remote.models.AI.AiMessage
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
    val language: String
)

data class AIInfo(
    val tags: List<String>,
    val currency: String,
    val timezone: String,
    val language: String
)

class CityDataRepository @Inject constructor(
    private val geoDbApiService: GeoDbApiService,
    private val aiApiService: AiApiService,
    private val cityDataDao: CityDataDao
) {
    companion object {
        private const val TAG = "CityDataRepository"
    }

    suspend fun getCityDetails(cityName: String, countryName: String): CityDetails? {
        return try {
            Log.d(TAG, "Fetching city details for: $cityName, $countryName")
            
            // Check database first
            val cityKey = "${cityName}_$countryName"
            val cachedData = cityDataDao.getCityData(cityKey)
            
            if (cachedData != null) {
                Log.d(TAG, "Found cached data for: $cityKey")
                return CityDetails(
                    city = City(0, cachedData.cityName, cachedData.countryName, 0.0, 0.0),
                    country = null,
                    tags = Gson().fromJson(cachedData.tags, Array<String>::class.java).toList(),
                    timezone = cachedData.timezone,
                    currency = cachedData.currency,
                    language = cachedData.language
                )
            }
            
            Log.d(TAG, "No cached data found, fetching from API")
            
            // First, search for the city to get its ID
            val searchResponse = geoDbApiService.searchCities(
                namePrefix = cityName,
                limit = 10
            )
            
            Log.d(TAG, "Search response: ${searchResponse.data.size} cities found")
            searchResponse.data.forEach { city ->
                Log.d(TAG, "Found city: ${city.name}, ${city.country}")
            }
            
            val city = searchResponse.data.find { 
                it.name.equals(cityName, ignoreCase = true) && 
                it.country.equals(countryName, ignoreCase = true) 
            }
            
            if (city != null) {
                Log.d(TAG, "Found exact match: ${city.name} with ID: ${city.id}")
                
                // Get country details for currency and timezone
                val countryDetails = getCountryDetails(city.country)
                
                // Get AI-generated tags and additional info
                val aiInfo = getCityInfoFromAI(cityName, countryName)
                
                val result = CityDetails(
                    city = city,
                    country = countryDetails,
                    tags = aiInfo.tags,
                    timezone = aiInfo.timezone,
                    currency = aiInfo.currency,
                    language = aiInfo.language
                )
                
                Log.d(TAG, "Final result: currency=${result.currency}, timezone=${result.timezone}, language=${result.language}")
                
                // Cache the result
                cacheCityData(cityKey, cityName, countryName, result, aiInfo)
                
                return result
            } else {
                Log.w(TAG, "City not found: $cityName, $countryName")
                // Try to find by just city name
                val fallbackCity = searchResponse.data.find { 
                    it.name.equals(cityName, ignoreCase = true) 
                }
                
                if (fallbackCity != null) {
                    Log.d(TAG, "Using fallback city: ${fallbackCity.name}, ${fallbackCity.country}")
                    val countryDetails = getCountryDetails(fallbackCity.country)
                    val aiInfo = getCityInfoFromAI(cityName, fallbackCity.country)
                    
                    val result = CityDetails(
                        city = fallbackCity,
                        country = countryDetails,
                        tags = aiInfo.tags,
                        timezone = aiInfo.timezone,
                        currency = aiInfo.currency,
                        language = aiInfo.language
                    )
                    
                    Log.d(TAG, "Fallback result: currency=${result.currency}, timezone=${result.timezone}, language=${result.language}")
                    
                    // Cache the result
                    cacheCityData(cityKey, cityName, countryName, result, aiInfo)
                    
                    return result
                }
                
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching city details", e)
            null
        }
    }

    private suspend fun cacheCityData(
        cityKey: String,
        cityName: String,
        countryName: String,
        cityDetails: CityDetails,
        aiInfo: AIInfo
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
                description = "", // No longer used
                highlights = "" // No longer used
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

    private suspend fun getCountryDetails(countryName: String): Country? {
        return try {
            Log.d(TAG, "Fetching country details for: $countryName")
            
            val countriesResponse = geoDbApiService.getCountries(
                limit = 20,
                namePrefix = countryName
            )
            
            Log.d(TAG, "Countries response: ${countriesResponse.data.size} countries found")
            
            val country = countriesResponse.data.find { 
                it.name.equals(countryName, ignoreCase = true) 
            }
            
            if (country != null) {
                Log.d(TAG, "Found exact country match: ${country.name}")
                return country
            } else {
                // Try partial match
                val partialMatch = countriesResponse.data.find { 
                    it.name.contains(countryName, ignoreCase = true) || 
                    countryName.contains(it.name, ignoreCase = true)
                }
                
                if (partialMatch != null) {
                    Log.d(TAG, "Found partial country match: ${partialMatch.name}")
                    return partialMatch
                }
                
                Log.w(TAG, "Country not found: $countryName")
            }
            
            null
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching country details", e)
            null
        }
    }

    private suspend fun getCityInfoFromAI(cityName: String, countryName: String): AIInfo {
        return try {
            Log.d(TAG, "Getting AI info for: $cityName, $countryName")
            
            val prompt = """
                Provide information about $cityName, $countryName in the following JSON format:
                {
                    "tags": ["tag1", "tag2", "tag3", "tag4", "tag5"],
                    "currency": "Official currency name and code",
                    "timezone": "Primary timezone",
                    "language": "Primary language spoken"
                }
                
                Focus on providing accurate currency, timezone, and language information for this city and country.
            """.trimIndent()
            
            val request = AiRequest(
                model = "gpt-3.5-turbo",
                messages = listOf(
                    AiMessage(role = "user", content = prompt)
                ),
                temperature = 0.7
            )
            
            val response = aiApiService.generate(
                authorization = "Bearer ${com.example.wanderbee.BuildConfig.AI_API_KEY}",
                request = request
            )
            Log.d(TAG, "AI response: ${response.choices.firstOrNull()?.message?.content}")
            
            val aiResponse = response.choices.firstOrNull()?.message?.content ?: ""
            
            AIInfo(
                tags = extractTagsFromAIResponse(aiResponse),
                currency = extractCurrencyFromAIResponse(aiResponse),
                timezone = extractTimezoneFromAIResponse(aiResponse),
                language = extractLanguageFromAIResponse(aiResponse)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting AI info", e)
            AIInfo(
                tags = listOf(cityName, countryName),
                currency = "Unknown",
                timezone = "Unknown",
                language = "Unknown"
            )
        }
    }

    private fun extractTagsFromAIResponse(content: String): List<String> {
        return try {
            // Simple regex extraction for tags
            val tagPattern = "\"tags\":\\s*\\[(.*?)\\]".toRegex()
            val match = tagPattern.find(content)
            match?.let {
                val tagsString = it.groupValues[1]
                tagsString.split(",")
                    .map { tag -> tag.trim().removeSurrounding("\"") }
                    .filter { it.isNotEmpty() }
            } ?: listOf("Tourism", "Culture", "Travel")
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting tags", e)
            listOf("Tourism", "Culture", "Travel")
        }
    }

    private fun extractCurrencyFromAIResponse(content: String): String {
        return try {
            val currencyPattern = "\"currency\":\\s*\"(.*?)\"".toRegex()
            val match = currencyPattern.find(content)
            match?.groupValues?.get(1) ?: "Unknown"
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting currency", e)
            "Unknown"
        }
    }

    private fun extractTimezoneFromAIResponse(content: String): String {
        return try {
            val timezonePattern = "\"timezone\":\\s*\"(.*?)\"".toRegex()
            val match = timezonePattern.find(content)
            match?.groupValues?.get(1) ?: "Unknown"
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting timezone", e)
            "Unknown"
        }
    }

    private fun extractLanguageFromAIResponse(content: String): String {
        return try {
            val languagePattern = "\"language\":\\s*\"(.*?)\"".toRegex()
            val match = languagePattern.find(content)
            match?.groupValues?.get(1) ?: "Unknown"
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting language", e)
            "Unknown"
        }
    }
} 