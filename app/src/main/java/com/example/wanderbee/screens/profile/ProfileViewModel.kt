package com.example.wanderbee.screens.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.local.dao.ProfileDao
import com.example.wanderbee.data.local.entity.ProfileEntity
import com.example.wanderbee.data.remote.apiService.IdentityApiService
import com.example.wanderbee.data.repository.ImgBBRepository
import com.example.wanderbee.data.repository.DefaultPexelsRepository
import com.example.wanderbee.utils.AppPreferences
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val identityApiService: IdentityApiService,
    private val imgBBRepository: ImgBBRepository,
    private val profileDao: ProfileDao,
    private val appPreferences: AppPreferences,
    @ApplicationContext private val context: Context,
    private val defaultPexelsRepository: DefaultPexelsRepository
) : ViewModel() {

    private val _profileData = MutableStateFlow(ProfileData())
    val profileData: StateFlow<ProfileData> = _profileData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context)

    private val _cityImageUrls = mutableMapOf<String, String?>()
    val cityImageUrls: Map<String, String?> = _cityImageUrls

    /**
     * Load profile data in three layers:
     * 1. AppPreferences (instant – set during login).
     * 2. Room DB (extended travel preferences stored locally).
     * 3. identity-service /auth/validate (refresh from server, best-effort).
     */
    fun loadProfileData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val email = appPreferences.getUserEmailOnce() ?: ""
                val name  = appPreferences.userName.first() ?: ""

                // Layer 1: fast render from AppPreferences
                _profileData.value = _profileData.value.copy(
                    email  = email,
                    name   = name,
                    userId = email
                )

                // Layer 2: extended fields from Room DB
                profileDao.getProfile(email)?.let { local ->
                    _profileData.value = local.toProfileData().copy(
                        email = email,
                        name  = name.ifBlank { local.name }
                    )
                }

                // Layer 3: refresh from identity-service (best-effort)
                try {
                    val response = identityApiService.validateToken()
                    if (response.isSuccessful) {
                        response.body()?.let { validated ->
                            val updated = _profileData.value.copy(
                                email = validated.email,
                                name  = validated.name.ifBlank { name }
                            )
                            _profileData.value = updated
                            appPreferences.saveUserInfo(validated.email, validated.name)
                            profileDao.insertProfile(updated.toProfileEntity())
                        }
                    }
                } catch (_: Exception) { /* offline / 401 – use cached */ }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectProfileImage() { _selectedImageUri.value = null }

    fun uploadProfileImage(uri: Uri) {
        val email = _profileData.value.email.ifBlank { return }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val imageUrl = imgBBRepository.uploadToImgBB(context, uri)
                val updated = _profileData.value.copy(
                    profilePictureUrl = imageUrl,
                    updatedAt = System.currentTimeMillis()
                )
                profileDao.insertProfile(updated.toProfileEntity())
                _profileData.value = updated
            } catch (_: Exception) { /* keep previous URL */ } finally {
                _isLoading.value = false
            }
        }
    }

    /** Extended travel preferences are stored only in Room (not the identity-service). */
    fun updateProfile(updatedProfile: ProfileData) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val record = updatedProfile.copy(updatedAt = System.currentTimeMillis())
                profileDao.insertProfile(record.toProfileEntity())
                _profileData.value = record
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            appPreferences.clearJwtToken()
            appPreferences.clearUserInfo()
        }
    }

    /** Sync profile after a successful Google sign-in. */
    fun syncGoogleProfile(email: String, name: String, photoUrl: String = "") {
        viewModelScope.launch {
            val profile = ProfileData(
                userId = email,
                name   = name,
                email  = email,
                profilePictureUrl = photoUrl,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            profileDao.insertProfile(profile.toProfileEntity())
            _profileData.value = profile
        }
    }

    fun loadCityCoverImage(cityName: String, onLoaded: (String?) -> Unit = {}) {
        if (_cityImageUrls.containsKey(cityName)) { onLoaded(_cityImageUrls[cityName]); return }
        viewModelScope.launch {
            try {
                val url = defaultPexelsRepository.getBackendPhotos(cityName)
                    .photos.shuffled().random().src.medium
                _cityImageUrls[cityName] = url
                onLoaded(url)
            } catch (_: Exception) {
                _cityImageUrls[cityName] = null
                onLoaded(null)
            }
        }
    }

    /**
     * Get another user's profile picture URL.
     * TODO: Add a backend endpoint to fetch user profiles by ID.
     * For now returns null (no such endpoint exists).
     */
    suspend fun getUserProfilePictureUrl(userId: String): String? {
        return null
    }

    fun requestLocationPermission() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (ContextCompat.checkSelfPermission(
                        context, Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) getCurrentLocation()
            } finally { _isLoading.value = false }
        }
    }

    private suspend fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(
                    context, Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location: Location? = fusedLocationClient.lastLocation.await()
                location?.let { loc ->
                    val city = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                        ?.firstOrNull()?.locality ?: "Unknown Location"
                    updateProfile(_profileData.value.copy(location = city))
                }
            }
        } catch (_: Exception) {}
    }

    fun updateProfileWithLocation(location: String) =
        updateProfile(_profileData.value.copy(location = location))
}

// ── Mapping helpers ────────────────────────────────────────────────────────────

fun ProfileData.toProfileEntity(): ProfileEntity = ProfileEntity(
    userId = userId, name = name, email = email, phone = phone,
    location = location, profilePictureUrl = profilePictureUrl,
    travelStyle = travelStyle, favoriteDestinations = favoriteDestinations,
    travelCompanions = travelCompanions, budgetRange = budgetRange,
    preferredClimate = preferredClimate, travelFrequency = travelFrequency,
    languages = languages, dietaryRestrictions = dietaryRestrictions,
    createdAt = createdAt, updatedAt = updatedAt
)

fun ProfileEntity.toProfileData(): ProfileData = ProfileData(
    userId = userId, name = name, email = email, phone = phone,
    location = location, profilePictureUrl = profilePictureUrl,
    travelStyle = travelStyle, favoriteDestinations = favoriteDestinations,
    travelCompanions = travelCompanions, budgetRange = budgetRange,
    preferredClimate = preferredClimate, travelFrequency = travelFrequency,
    languages = languages, dietaryRestrictions = dietaryRestrictions,
    createdAt = createdAt, updatedAt = updatedAt
)
