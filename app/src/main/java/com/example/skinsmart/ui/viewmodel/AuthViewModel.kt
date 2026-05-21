package com.example.skinsmart.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.skinsmart.data.local.LocalUser
import com.example.skinsmart.data.local.SkinSmartDatabase
import com.example.skinsmart.data.repository.AuthRepository
import com.example.skinsmart.model.User
import kotlinx.coroutines.launch

/**
 * AuthViewModel handles user authentication and profile management.
 * Explicitly inherits from AndroidViewModel to provide the required Application context for Room.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SkinSmartDatabase.getDatabase(application).skinSmartDao()
    private val repository = AuthRepository(dao)

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    // Cached user for offline availability (Room)
    val cachedUser: LiveData<LocalUser?> = dao.getCachedUser()

    // Listen for changes in the user's shelf count
    val shelfCount: LiveData<Int> = _currentUser.switchMap { user ->
        if (user != null) {
            dao.getShelfCount(user.id)
        } else {
            MutableLiveData(0)
        }
    }
    private val _reviewCount = MutableLiveData<Int>(0)
    val reviewCount: LiveData<Int> = _reviewCount

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        if (repository.isUserLoggedIn()) {
            _isLoading.value = true
            viewModelScope.launch {
                val result = repository.getCurrentUser()
                if (result.isSuccess) {
                    val user = result.getOrNull()
                    _currentUser.value = user
                    user?.let { refreshStats(it.id) }
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to sync profile data"
                }
                _isLoading.value = false
            }
        }
    }

    /**
     * Re-fetches the review count from Firestore for the specific user.
     */
    fun refreshStats(userId: String) {
        viewModelScope.launch {
            _reviewCount.value = repository.getReviewCount(userId)
        }
    }

    fun register(email: String, pass: String, name: String, skinType: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.register(email, pass, name, skinType)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Registration failed"
            }
            _isLoading.value = false
        }
    }

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

    fun updateUserProfile(name: String, skinType: String, imageBitmap: Bitmap?) {
        val userId = _currentUser.value?.id ?: return
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.updateUserProfile(userId, name, skinType, imageBitmap)
            if (result.isSuccess) {
                _currentUser.value = result.getOrNull()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Update failed"
            }
            _isLoading.value = false
        }
    }
}
