package com.example.wanderbee.screens.details

import com.example.wanderbee.data.repository.CityDetails

sealed class CityDataState {
    object Idle : CityDataState()
    object Loading : CityDataState()
    data class Success(val cityDetails: CityDetails) : CityDataState()
    data class Error(val message: String) : CityDataState()
} 