package com.example.wanderbee.screens.authentication

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.wanderbee.data.local.dao.ProfileDao
import com.example.wanderbee.data.repository.ImgBBRepository
import com.example.wanderbee.screens.profile.ProfileData
import com.example.wanderbee.screens.profile.toProfileEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

sealed class State {
    object Idle : State()
    object Success : State()
    object Error : State()
    object Loading : State()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFireStore: FirebaseFirestore,
    private val profileDao: ProfileDao,
    private val imgBBRepository: ImgBBRepository,
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
    fun updateEmail(email: String){
        _email.value = email
    }
    fun updatePass(password: String){
        _password.value = password
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        if (email.trim().isEmpty() || password.trim().isEmpty()){
            _loginState.value = State.Error
        } else {
            _loginState.value = State.Loading
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _loginState.value = State.Success
                        // Get and store FCM token after successful login
                        viewModelScope.launch {
                            notificationManager.getAndStoreFCMToken()
                        }
                    } else {
                        _loginState.value = State.Error
                    }
                }
        }
    }


    fun signUpWithEmailAndPassword(email: String, password: String, name: String) {
        if (email.trim().isEmpty() || password.trim().isEmpty() || name.trim().isEmpty() ){
            _signUpState.value = State.Error
        }else {
            _signUpState.value = State.Loading
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _signUpState.value = State.Success
                        _name.value = name
                        firebaseAuth.currentUser?.uid?.let { uid ->
                            firebaseFireStore.collection("users")
                                .document(uid)
                                .set(mapOf("name" to name))
                        }
                        // Get and store FCM token after successful signup
                        viewModelScope.launch {
                            notificationManager.getAndStoreFCMToken()
                        }
                    } else {
                        _signUpState.value = State.Error
                    }
                }
        }
    }

    fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
                Log.d("AuthViewModel", "Google Sign-In Successful")
                _loginState.value = State.Success
                firebaseAuth.currentUser?.uid?.let { uid ->
                    firebaseFireStore.collection("users")
                        .document(uid)
                        .set(mapOf("name" to firebaseAuth.currentUser?.displayName))
                }
                // Sync Google profile to Firestore and Room
                syncGoogleProfile()
                // Get and store FCM token after successful Google sign-in
                viewModelScope.launch {
                    notificationManager.getAndStoreFCMToken()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("AuthViewModel", "Google Sign-In Failed", exception)
                _loginState.value = State.Error
            }
    }

    private fun syncGoogleProfile() {
        val user = firebaseAuth.currentUser ?: return
        val userId = user.uid
        CoroutineScope(Dispatchers.IO).launch {
            val profileData = ProfileData(
                userId = userId,
                name = user.displayName ?: "",
                email = user.email ?: "",
                profilePictureUrl = user.photoUrl?.toString() ?: "",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            // Update Firestore
            firebaseFireStore.collection("users").document(userId).set(profileData)
            // Update Room
            profileDao.insertProfile(profileData.toProfileEntity())
        }
    }

    fun sendPasswordResetEmail(email: String) {
        _forgotState.value = State.Loading
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _forgotState.value = State.Success
                } else {
                    _forgotState.value = State.Error
                }
            }
    }

    fun logout() {
        viewModelScope.launch {
            // Delete FCM token before logging out
            notificationManager.deleteFCMToken()
            // Sign out from Firebase
            firebaseAuth.signOut()
        }
    }
}

// check if auth viewmodel is correct as State.error is introduced when parameters are empty
//also keep it for login screen
//handle the states and print apt messages below text fields when error state is present