package com.example.wanderbee.screens.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.local.dao.ProfileDao
import com.example.wanderbee.data.local.entity.ProfileEntity
import com.example.wanderbee.data.repository.ImgBBRepository
import com.example.wanderbee.data.repository.DefaultPexelsRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val imgBBRepository: ImgBBRepository,
    private val profileDao: ProfileDao,
    @ApplicationContext private val context: Context,
    private val defaultPexelsRepository: DefaultPexelsRepository
) : ViewModel() {

    private val _profileData = MutableStateFlow(ProfileData())
    val profileData: StateFlow<ProfileData> = _profileData.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    // Location related
    private val fusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context)

    // City cover image cache for group chats
    private val _cityImageUrls = mutableMapOf<String, String?>()
    val cityImageUrls: Map<String, String?> = _cityImageUrls

    fun loadProfileData() {
        val currUser = auth.currentUser?: return
        val userId = currUser.uid
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 1. Try to load from local Room DB first
                val localProfile = profileDao.getProfile(userId)
                if (localProfile != null) {
                    _profileData.value = localProfile.toProfileData()
                }
                // 2. Always fetch from Firestore for latest
                val document = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                if (document.exists()) {
                    val data = document.data
                    val profile = ProfileData(
                        userId = userId,
                        name = data?.get("name") as? String ?: "",
                        email = currUser.email ?: "",
                        location = data?.get("location") as? String ?: "",
                        profilePictureUrl = data?.get("profilePictureUrl") as? String ?: "",
                        travelStyle = data?.get("travelStyle") as? String ?: "",
                        favoriteDestinations = data?.get("favoriteDestinations") as? String ?: "",
                        travelCompanions = data?.get("travelCompanions") as? String ?: "",
                        budgetRange = data?.get("budgetRange") as? String ?: "",
                        preferredClimate = data?.get("preferredClimate") as? String ?: "",
                        travelFrequency = data?.get("travelFrequency") as? String ?: "",
                        languages = data?.get("languages") as? String ?: "",
                        dietaryRestrictions = data?.get("dietaryRestrictions") as? String ?: "",
                        createdAt = data?.get("createdAt") as? Long ?: 0L,
                        updatedAt = data?.get("updatedAt") as? Long ?: 0L
                    )
                    _profileData.value = profile
                    // Sync to Room
                    profileDao.insertProfile(profile.toProfileEntity())
                } else {
                    // Create default profile if user doesn't exist
                    createDefaultProfile(userId)
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun createDefaultProfile(userId: String) {
        val user = auth.currentUser
        val defaultProfile = ProfileData(
            userId = userId,
            name = user?.displayName ?: "",
            email = user?.email ?: "",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        try {
            firestore.collection("users")
                .document(userId)
                .set(defaultProfile)
                .await()
            _profileData.value = defaultProfile
            profileDao.insertProfile(defaultProfile.toProfileEntity())
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun selectProfileImage() {
        _selectedImageUri.value = null
    }

    fun uploadProfileImage(uri: Uri) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Upload to ImgBB
                val imageUrl = imgBBRepository.uploadToImgBB(context, uri)
                // Update Firestore
                firestore.collection("users")
                    .document(userId)
                    .update(
                        mapOf(
                            "profilePictureUrl" to imageUrl,
                            "updatedAt" to System.currentTimeMillis()
                        )
                    )
                    .await()
                // Update local Room DB
                val updatedProfile = _profileData.value.copy(
                    profilePictureUrl = imageUrl,
                    updatedAt = System.currentTimeMillis()
                )
                profileDao.insertProfile(updatedProfile.toProfileEntity())
                _profileData.value = updatedProfile
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(updatedProfile: ProfileData) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updateData = mapOf(
                    "name" to updatedProfile.name,
                    "phone" to updatedProfile.phone,
                    "location" to updatedProfile.location,
                    "travelStyle" to updatedProfile.travelStyle,
                    "favoriteDestinations" to updatedProfile.favoriteDestinations,
                    "travelCompanions" to updatedProfile.travelCompanions,
                    "budgetRange" to updatedProfile.budgetRange,
                    "preferredClimate" to updatedProfile.preferredClimate,
                    "travelFrequency" to updatedProfile.travelFrequency,
                    "languages" to updatedProfile.languages,
                    "dietaryRestrictions" to updatedProfile.dietaryRestrictions,
                    "updatedAt" to System.currentTimeMillis()
                )
                firestore.collection("users")
                    .document(userId)
                    .update(updateData)
                    .await()
                // Update local Room DB
                profileDao.insertProfile(updatedProfile.copy(updatedAt = System.currentTimeMillis()).toProfileEntity())
                _profileData.value = updatedProfile.copy(updatedAt = System.currentTimeMillis())
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun syncGoogleProfile() {
        val user = auth.currentUser ?: return
        val userId = user.uid
        viewModelScope.launch {
            val profileData = ProfileData(
                userId = userId,
                name = user.displayName ?: "",
                email = user.email ?: "",
                profilePictureUrl = user.photoUrl?.toString() ?: "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            // Update Firestore
            firestore.collection("users").document(userId).set(profileData)
            // Update Room
            profileDao.insertProfile(profileData.toProfileEntity())
            _profileData.value = profileData
        }
    }

    fun loadCityCoverImage(cityName: String, onLoaded: (String?) -> Unit = {}) {
        if (_cityImageUrls.containsKey(cityName)) {
            onLoaded(_cityImageUrls[cityName])
            return
        }
        viewModelScope.launch {
            try {
                val response = defaultPexelsRepository.getPexelsPhotos(cityName)
                val url = response.photos.shuffled().random().src.medium
                _cityImageUrls[cityName] = url
                onLoaded(url)
            } catch (e: Exception) {
                _cityImageUrls[cityName] = null
                onLoaded(null)
            }
        }
    }

    // Fetch another user's profile picture for private chat
    suspend fun getUserProfilePictureUrl(userId: String): String? {
        // Try local DB first
        val local = profileDao.getProfile(userId)
        if (local != null && local.profilePictureUrl.isNotEmpty()) {
            return local.profilePictureUrl
        }
        // Fallback to Firestore
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            val url = doc.getString("profilePictureUrl")
            if (!url.isNullOrEmpty()) url else null
        } catch (e: Exception) {
            null
        }
    }

    fun requestLocationPermission() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Check if location permission is granted
                if (ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    getCurrentLocation()
                } else {
                    // Permission not granted, this will be handled by the UI layer
                    // The UI should request permission using rememberLauncherForActivityResult
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun getCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                val location: Location? = fusedLocationClient.lastLocation.await()
                location?.let { loc ->
                    val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                    val cityName = addresses?.firstOrNull()?.locality ?: "Unknown Location"
                    
                    // Update profile with current location
                    val updatedProfile = _profileData.value.copy(location = cityName)
                    updateProfile(updatedProfile)
                }
            }
        } catch (e: Exception) {
            // Handle error
        }
    }

    fun updateProfileWithLocation(location: String) {
        val updatedProfile = _profileData.value.copy(location = location)
        updateProfile(updatedProfile)
    }
}

// Mapping functions
fun ProfileData.toProfileEntity(): ProfileEntity = ProfileEntity(
    userId = userId,
    name = name,
    email = email,
    phone = phone,
    location = location,
    profilePictureUrl = profilePictureUrl,
    travelStyle = travelStyle,
    favoriteDestinations = favoriteDestinations,
    travelCompanions = travelCompanions,
    budgetRange = budgetRange,
    preferredClimate = preferredClimate,
    travelFrequency = travelFrequency,
    languages = languages,
    dietaryRestrictions = dietaryRestrictions,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ProfileEntity.toProfileData(): ProfileData = ProfileData(
    userId = userId,
    name = name,
    email = email,
    phone = phone,
    location = location,
    profilePictureUrl = profilePictureUrl,
    travelStyle = travelStyle,
    favoriteDestinations = favoriteDestinations,
    travelCompanions = travelCompanions,
    budgetRange = budgetRange,
    preferredClimate = preferredClimate,
    travelFrequency = travelFrequency,
    languages = languages,
    dietaryRestrictions = dietaryRestrictions,
    createdAt = createdAt,
    updatedAt = updatedAt
) 