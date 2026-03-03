package com.example.wanderbee.screens.saved

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.local.dao.SavedDestinationDao
import com.example.wanderbee.data.local.entity.SavedDestinationEntity
import com.example.wanderbee.data.repository.DefaultPexelsRepository
import com.example.wanderbee.data.repository.DestinationResult
import com.example.wanderbee.data.repository.DestinationsRepository
import com.example.wanderbee.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.collections.set

@HiltViewModel
class SavedViewModel @Inject constructor(
    private val savedDestinationDao: SavedDestinationDao,
    private val appPreferences: AppPreferences,
    private val defaultPexelsRepository: DefaultPexelsRepository,
    private val destinationsRepository: DestinationsRepository
) : ViewModel() {

    private val _savedDestinations = MutableStateFlow<List<SavedDestinationEntity>>(emptyList())
    val savedDestinations: StateFlow<List<SavedDestinationEntity>> = _savedDestinations.asStateFlow()

    private val _savedImageUrl = mutableMapOf<String, String?>()
    val savedImageUrl: Map<String, String?> = _savedImageUrl

    fun loadSavedDestinations() {
        viewModelScope.launch {
            val userId = appPreferences.getUserEmailOnce()
            if (userId.isNullOrBlank()) return@launch

            try {
                // Show local data immediately
                val localDestinations = savedDestinationDao.getAllSavedDestinations(userId)
                _savedDestinations.value = localDestinations

                // Sync from backend in background
                val backendResult = destinationsRepository.getSavedDestinations(userId)
                if (backendResult is DestinationResult.Success) {
                    val backendEntities = backendResult.data.map { response ->
                        val parts = response.cityName.split(", ", limit = 2)
                        SavedDestinationEntity(
                            destinationId = response.cityId,
                            city = parts.getOrElse(0) { response.cityName },
                            destination = parts.getOrElse(1) { "" },
                            userId = response.userId,
                            savedAt = System.currentTimeMillis()
                        )
                    }
                    // Merge: insert any backend entries missing locally
                    backendEntities.forEach { entity ->
                        if (!savedDestinationDao.isDestinationSaved(entity.destinationId, userId)) {
                            savedDestinationDao.saveDestination(entity)
                        }
                    }
                    // Refresh from local after sync
                    _savedDestinations.value = savedDestinationDao.getAllSavedDestinations(userId)
                    Log.d("SavedViewModel", "Synced ${backendResult.data.size} destinations from backend")
                }
            } catch (e: Exception) {
                Log.e("SavedViewModel", "Error loading saved destinations: ${e.message}", e)
            }
        }
    }

    fun unsaveDestination(destinationId: String) {
        viewModelScope.launch {
            val userId = appPreferences.getUserEmailOnce()
            if (userId.isNullOrBlank()) return@launch

            try {
                // Remove locally
                savedDestinationDao.unsaveDestination(destinationId, userId)

                // Remove on backend
                try {
                    destinationsRepository.unsaveDestination(destinationId)
                    Log.d("SavedViewModel", "Unsaved on backend: $destinationId")
                } catch (e: Exception) {
                    Log.e("SavedViewModel", "Error unsaving on backend: ${e.message}", e)
                }

                // Reload the list after unsaving
                loadSavedDestinations()
            } catch (e: Exception) {
                Log.e("SavedViewModel", "Error unsaving destination: ${e.message}", e)
            }
        }
    }

    fun savedCityCoverImage(cityName: String) {
        if (_savedImageUrl.containsKey(cityName)) return

        viewModelScope.launch {
            try {
                val response = defaultPexelsRepository.getBackendPhotos(cityName)
                val url = response.photos.shuffled().random().src.medium
                _savedImageUrl[cityName] = url
            } catch (e: Exception) {
                _savedImageUrl[cityName] = null
            }
        }
    }
} 