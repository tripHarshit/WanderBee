package com.example.wanderbee.screens.home

import androidx.compose.runtime.mutableStateMapOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.repository.DefaultPexelsRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val defaultPexelsRepository: DefaultPexelsRepository,
) : ViewModel() {


    private val _imageUrls = mutableStateMapOf<String, String?>()
    val imageUrls: Map<String, String?> = _imageUrls


    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    fun fetchUserName() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return

            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { document ->
                    val name = document.getString("name")
                    _name.value = name
                        ?.split(" ")
                        ?.firstOrNull()
                        ?.uppercase(Locale.ROOT)
                        ?: " "
        }
    }

    fun loadCityCoverImage(cityName: String) {
        if (_imageUrls.containsKey(cityName)) return

        viewModelScope.launch {
            try {
                val response = defaultPexelsRepository.getPexelsPhotos(cityName)
                val url = response.photos.shuffled().random().src.medium
                _imageUrls[cityName] = url
            } catch (e: Exception) {
                _imageUrls[cityName] = null
            }
        }
    }
}




