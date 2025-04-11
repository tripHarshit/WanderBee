package com.example.wanderbee.screens.authentication

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

sealed class State {
    object Idle : State()
    object Success : State()
    object Error : State()
}

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFireStore: FirebaseFirestore
) : ViewModel() {

    private val _loginState = MutableStateFlow<State>(State.Idle)
    val loginState: StateFlow<State> = _loginState.asStateFlow()

    private val _signUpState = MutableStateFlow<State>(State.Idle)
    val signUpState: StateFlow<State> = _signUpState.asStateFlow()

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    fun updateName(newName: String) {
        _name.value = newName
    }

    fun signInWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _loginState.value = if (task.isSuccessful) State.Success else State.Error
            }
    }

    fun signUpWithEmailAndPassword(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _signUpState.value = State.Success
                    firebaseAuth.currentUser?.uid?.let { uid ->
                        firebaseFireStore.collection("users")
                            .document(uid)
                            .set(mapOf("name" to _name.value))
                    }
                } else {
                    _signUpState.value = State.Error
                }
            }
    }
}
