package com.example.wanderbee.models

import androidx.navigation.ActivityNavigator
import com.example.wanderbee.data.remote.models.Destination

data class HomeUiState(
    val isLoading: Boolean = true,
    val showDestination: List<Destination> = emptyList()
)
