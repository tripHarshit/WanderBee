package com.example.wanderbee.screens.authentication

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.remote.apiService.IdentityApiService
import com.example.wanderbee.data.remote.models.auth.AuthRequest
import com.example.wanderbee.data.remote.models.auth.GoogleTokenRequest
import com.example.wanderbee.data.remote.models.auth.UserCredentials
import com.example.wanderbee.utils.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import javax.inject.Inject

sealed class State {
    object Idle : State()
    object Success : State()
    data class Error(val message: String) : State()
    object Loading : State()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val identityApi: IdentityApiService,
    private val appPreferences: AppPreferences,
    private val notificationManager: com.example.wanderbee.utils.NotificationManager
) : ViewModel() {

    private val _loginState = MutableStateFlow<State>(State.Idle)
    val loginState: StateFlow<State> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow<State>(State.Idle)
    val signUpState: StateFlow<State> = _signUpState.asStateFlow()

    private val _forgotState = MutableStateFlow<State>(State.Idle)
    val forgotState: StateFlow<State> = _forgotState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun updateName(newName: String) {
        _name.value = newName
    }
    fun updateEmail(email: String) {
        _email.value = email
    }
    fun updatePass(password: String) {
        _password.value = password
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            _loginState.value = State.Error("Please fill in all fields")
            return
        }
        _loginState.value = State.Loading
        viewModelScope.launch {
            try {
                val response = identityApi.login(AuthRequest(email.trim(), password))
                if (response.isSuccessful) {
                    val jwt = response.body()
                    if (!jwt.isNullOrBlank()) {
                        appPreferences.saveJwtToken(jwt)
                        appPreferences.saveUserInfo(email.trim(), "")
                        _loginState.value = State.Success
                        notificationManager.getAndStoreFCMToken()
                    } else {
                        _loginState.value = State.Error("Invalid credentials")
                    }
                } else {
                    _loginState.value = State.Error("Invalid credentials")
                }
            } catch (e: SocketTimeoutException) {
                _loginState.value = State.Error("Connection timed out. Please try again")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed", e)
                _loginState.value = State.Error("Connection failed. Is the server running?")
            }
        }
    }

    fun signUpWithEmailAndPassword(email: String, password: String, name: String) {
        if (email.trim().isEmpty() || password.trim().isEmpty() || name.trim().isEmpty()) {
            _signUpState.value = State.Error("Please fill in all fields")
            return
        }
        _signUpState.value = State.Loading
        viewModelScope.launch {
            try {
                val response = identityApi.register(
                    UserCredentials(
                        name = name.trim(),
                        email = email.trim(),
                        password = password
                    )
                )
                if (response.isSuccessful) {
                    val body = response.body() ?: ""
                    if (body.contains("already exists", ignoreCase = true)) {
                        _signUpState.value = State.Error("User already exists")
                    } else {
                        _name.value = name
                        _signUpState.value = State.Success
                        // Auto-login after successful registration
                        signInAfterRegister(email.trim(), password)
                    }
                } else {
                    _signUpState.value = State.Error("Registration failed. Please try again")
                }
            } catch (e: SocketTimeoutException) {
                _signUpState.value = State.Error("Connection timed out. Please try again")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration failed", e)
                _signUpState.value = State.Error("Connection failed. Is the server running?")
            }
        }
    }

    private suspend fun signInAfterRegister(email: String, password: String) {
        try {
            val response = identityApi.login(AuthRequest(email, password))
            if (response.isSuccessful) {
                val jwt = response.body()
                if (!jwt.isNullOrBlank()) {
                    appPreferences.saveJwtToken(jwt)
                    appPreferences.saveUserInfo(email, _name.value)
                    notificationManager.getAndStoreFCMToken()
                }
            }
        } catch (e: Exception) {
            Log.e("AuthViewModel", "Auto-login after register failed", e)
        }
    }

    fun signInWithGoogle(idToken: String) {
        _loginState.value = State.Loading
        viewModelScope.launch {
            try {
                val response = identityApi.googleLogin(GoogleTokenRequest(idToken))
                if (response.isSuccessful) {
                    val googleAuth = response.body()
                    if (googleAuth != null) {
                        appPreferences.saveJwtToken(googleAuth.token)
                        appPreferences.saveUserInfo(googleAuth.email, googleAuth.name)
                        _name.value = googleAuth.name
                        _loginState.value = State.Success
                        notificationManager.getAndStoreFCMToken()
                    } else {
                        _loginState.value = State.Error("Google sign-in failed")
                    }
                } else {
                    _loginState.value = State.Error("Google sign-in failed")
                }
            } catch (e: SocketTimeoutException) {
                _loginState.value = State.Error("Connection timed out. Please try again")
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google Sign-In Failed", e)
                _loginState.value = State.Error("Connection failed. Is the server running?")
            }
        }
    }

    fun sendPasswordResetEmail(email: String) {
        // Password reset is not yet supported by the backend.
        // This is a placeholder for future implementation.
        _forgotState.value = State.Error("Password reset is not yet available")
    }

    fun logout() {
        viewModelScope.launch {
            notificationManager.deleteFCMToken()
            appPreferences.clearSession()
        }
    }
}