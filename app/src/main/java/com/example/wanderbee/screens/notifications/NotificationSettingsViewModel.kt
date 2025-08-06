package com.example.wanderbee.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.utils.NotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationSettingsState(
    val pushNotificationsEnabled: Boolean = true,
    val emailNotificationsEnabled: Boolean = true,
    val newDestinationsEnabled: Boolean = true,
    val travelDealsEnabled: Boolean = true,
    val weatherAlertsEnabled: Boolean = true,
    val chatMessagesEnabled: Boolean = true,
    val travelTipsEnabled: Boolean = true,
    val isLoading: Boolean = false,
    val showSuccessMessage: Boolean = false
)

@HiltViewModel
class NotificationSettingsViewModel @Inject constructor(
    private val notificationManager: NotificationManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationSettingsState())
    val uiState: StateFlow<NotificationSettingsState> = _uiState.asStateFlow()

    fun loadNotificationSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            // TODO: Load settings from Firestore or SharedPreferences
            // For now, we'll use default values
            
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun updatePushNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(pushNotificationsEnabled = enabled)
    }

    fun updateEmailNotifications(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(emailNotificationsEnabled = enabled)
    }

    fun updateNewDestinations(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(newDestinationsEnabled = enabled)
    }

    fun updateTravelDeals(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(travelDealsEnabled = enabled)
    }

    fun updateWeatherAlerts(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(weatherAlertsEnabled = enabled)
    }

    fun updateChatMessages(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(chatMessagesEnabled = enabled)
    }

    fun updateTravelTips(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(travelTipsEnabled = enabled)
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val currentState = _uiState.value
                
                // Subscribe/unsubscribe to topics based on settings
                if (currentState.newDestinationsEnabled) {
                    notificationManager.subscribeToTopic("new_destinations")
                } else {
                    notificationManager.unsubscribeFromTopic("new_destinations")
                }
                
                if (currentState.travelDealsEnabled) {
                    notificationManager.subscribeToTopic("travel_deals")
                } else {
                    notificationManager.unsubscribeFromTopic("travel_deals")
                }
                
                if (currentState.weatherAlertsEnabled) {
                    notificationManager.subscribeToTopic("weather_alerts")
                } else {
                    notificationManager.unsubscribeFromTopic("weather_alerts")
                }
                
                if (currentState.chatMessagesEnabled) {
                    notificationManager.subscribeToTopic("chat_messages")
                } else {
                    notificationManager.unsubscribeFromTopic("chat_messages")
                }
                
                if (currentState.travelTipsEnabled) {
                    notificationManager.subscribeToTopic("travel_tips")
                } else {
                    notificationManager.unsubscribeFromTopic("travel_tips")
                }
                
                // TODO: Save settings to Firestore or SharedPreferences
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showSuccessMessage = true
                )
                
                // Hide success message after 3 seconds
                kotlinx.coroutines.delay(3000)
                _uiState.value = _uiState.value.copy(showSuccessMessage = false)
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                // TODO: Show error message
            }
        }
    }
} 