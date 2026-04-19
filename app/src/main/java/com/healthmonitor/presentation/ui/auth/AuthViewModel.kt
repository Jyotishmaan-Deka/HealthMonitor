package com.healthmonitor.presentation.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.healthmonitor.data.remote.firebase.FirebaseService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSignUpMode: Boolean = false
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebase: FirebaseService,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    val isLoggedIn: StateFlow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = firebase.isLoggedIn()
    )

    fun signIn(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            firebase.signIn(email, password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(error = friendlyError(e.message), isLoading = false) }
                }
        }
    }

    fun signUp(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            firebase.signUp(email, password)
                .onSuccess {
                    _state.update { it.copy(isLoading = false) }
                    onSuccess()
                }
                .onFailure { e ->
                    _state.update { it.copy(error = friendlyError(e.message), isLoading = false) }
                }
        }
    }

    fun signOut() {
        firebase.signOut()
    }

    fun toggleMode() {
        _state.update { it.copy(isSignUpMode = !it.isSignUpMode, error = null) }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    private fun friendlyError(raw: String?): String = when {
        raw == null                                        -> "An unexpected error occurred"
        "api key" in raw.lowercase() ||
        "api_key" in raw.lowercase() ||
        "key not valid" in raw.lowercase()                ->
            "⚙️ Firebase not configured — add your google-services.json file from the Firebase Console, " +
            "or tap \"Continue as Guest\" to explore with demo data."
        "no user record" in raw.lowercase()               -> "No account found with that email"
        "password is invalid" in raw.lowercase()          -> "Incorrect password"
        "email address is badly" in raw.lowercase()       -> "Please enter a valid email"
        "already in use" in raw.lowercase()               -> "An account already exists for this email"
        "network error" in raw.lowercase()                -> "No internet connection — check your network and try again"
        "too many requests" in raw.lowercase()            -> "Too many attempts. Please wait a moment and try again"
        "user disabled" in raw.lowercase()                -> "This account has been disabled"
        else                                              -> raw
    }
}
