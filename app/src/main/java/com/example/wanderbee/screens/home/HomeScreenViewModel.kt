package com.example.wanderbee.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.models.Destination
import com.example.wanderbee.models.HomeUiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val firebaseFireStore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    init {
        loadDummyDestinations()
        fetchUserName()
    }

    fun loadDummyDestinations() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            delay(1000)
            _uiState.value = HomeUiState(
                isLoading = false,
                showDestination = listOf(
                    Destination("Kyoto", "Japan", "A cultural hub with ancient temples."),
                    Destination(
                        "Barcelona",
                        "Spain",
                        "Vibrant city with beaches and architecture."
                    ),
                    Destination(
                        "Bali",
                        "Indonesia",
                        "Island paradise known for its beaches and rice terraces."
                    )
                )
            )
        }
    }

    fun fetchUserName() {
        FirebaseAuth.getInstance().currentUser?.uid?.let {
            FirebaseFirestore
                .getInstance()
                .collection("users").document(it).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        _name.value = document.getString("name") ?: " "
                    }
                }
        }
    }
}

