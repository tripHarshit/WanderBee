package com.example.wanderbee.models

import com.example.wanderbee.data.remote.models.destinations.Destination

data class HomeUiState(
    val isLoading: Boolean = true,
    val showDestination: List<Destination> = emptyList()
)
