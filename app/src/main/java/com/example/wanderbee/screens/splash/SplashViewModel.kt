package com.example.wanderbee.screens.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.utils.AppPreferences
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashState {
    object Loading : SplashState()
    object NavigateToOnboarding : SplashState()
    object NavigateToHome : SplashState()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow<SplashState>(SplashState.Loading)
    val state: StateFlow<SplashState> = _state.asStateFlow()

    init {
        checkNavigationState()
    }

    private fun checkNavigationState() {
        viewModelScope.launch {
            val isFirstLaunch = appPreferences.isFirstLaunch.collect { isFirstLaunch ->
                val isLoggedIn = auth.currentUser != null
                
                _state.value = when {
                    isFirstLaunch -> SplashState.NavigateToOnboarding
                    isLoggedIn -> SplashState.NavigateToHome
                    else -> SplashState.NavigateToOnboarding
                }
            }
        }
    }

    fun setOnboardingComplete() {
        viewModelScope.launch {
            appPreferences.setFirstLaunchComplete()
        }
    }

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }
} 