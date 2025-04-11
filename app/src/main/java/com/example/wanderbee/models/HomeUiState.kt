package com.example.wanderbee.models

import androidx.navigation.ActivityNavigator

data class HomeUiState(
    val isLoading: Boolean = true,
    val showDestination: List<Destination> = emptyList()
)
