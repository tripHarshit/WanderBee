package com.example.wanderbee.screens.saved

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.local.dao.SavedDestinationDao
import com.example.wanderbee.data.local.entity.SavedDestinationEntity
import com.example.wanderbee.data.repository.DefaultPexelsRepository
import com.google.firebase.auth.FirebaseAuth
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
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val defaultPexelsRepository: DefaultPexelsRepository
) : ViewModel() {

    private val _savedDestinations = MutableStateFlow<List<SavedDestinationEntity>>(emptyList())
    val savedDestinations: StateFlow<List<SavedDestinationEntity>> = _savedDestinations.asStateFlow()

    private val _savedImageUrl = mutableMapOf<String, String?>()
    val savedImageUrl: Map<String, String?> = _savedImageUrl

    fun loadSavedDestinations() {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                val destinations = savedDestinationDao.getAllSavedDestinations(userId)
                _savedDestinations.value = destinations
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun unsaveDestination(destinationId: String) {
        val userId = auth.currentUser?.uid ?: return
        
        viewModelScope.launch {
            try {
                savedDestinationDao.unsaveDestination(destinationId, userId)
                // Reload the list after unsaving
                loadSavedDestinations()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun savedCityCoverImage(cityName: String) {
        if (_savedImageUrl.containsKey(cityName)) return

        viewModelScope.launch {
            try {
                val response = defaultPexelsRepository.getPexelsPhotos(cityName)
                val url = response.photos.shuffled().random().src.medium
                _savedImageUrl[cityName] = url
            } catch (e: Exception) {
                _savedImageUrl[cityName] = null
            }
        }
    }
} 