package com.example.skinsmart.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinsmart.data.repository.AuthRepository
import com.example.skinsmart.model.User
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadCurrentUser()
    }

    /**
     * Fetch user data if Firebase already holds a persistent auth session.
     */
    private fun loadCurrentUser() {
        if (repository.isUserLoggedIn()) {
            _isLoading.value = true
            viewModelScope.launch {
                val result = repository.getCurrentUser()
                if (result.isSuccess) {
                    _currentUser.value = result.getOrNull()
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to sync profile data"
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Attempts to register a new user using Firebase
     */
    fun register(email: String, pass: String, name: String, skinType: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(email, pass, name, skinType)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            } else {
                val exception = result.exceptionOrNull()
                android.util.Log.e("AuthViewModel", "Registration failed", exception)
                _error.value = exception?.message ?: "Unknown registration error"
            }
            _isLoading.value = false
        }
    }

    /**
     * Attempts to log in a user using Firebase
     */
    fun login(email: String, pass: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.login(email, pass)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Invalid credentials"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        repository.logout()
        _currentUser.value = null
    }
}
