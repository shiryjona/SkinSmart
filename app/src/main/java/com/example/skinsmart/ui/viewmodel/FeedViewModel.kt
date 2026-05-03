package com.example.skinsmart.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinsmart.data.repository.FeedRepository
import com.example.skinsmart.model.SocialPost
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val repository = FeedRepository()
    private var feedListener: ListenerRegistration? = null

    private val _posts = MutableLiveData<List<SocialPost>>()
    val posts: LiveData<List<SocialPost>> = _posts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _postPublished = MutableLiveData<Boolean?>()
    val postPublished: LiveData<Boolean?> = _postPublished

    init {
        startListeningToFeed()
    }

    /**
     * Starts real-time updates for the global social feed.
     */
    fun startListeningToFeed() {
        _isLoading.value = true
        feedListener?.remove()
        feedListener = repository.listenToFeed(
            onUpdate = { posts ->
                _posts.value = posts
                _isLoading.value = false
            },
            onError = { exception ->
                _error.value = exception.message ?: "Failed to fetch feed"
                _isLoading.value = false
            }
        )
    }

    /**
     * Resets the post publication state.
     */
    fun resetPostState() {
        _postPublished.value = null
    }

    /**
     * Publishes a new review to the global feed.
     */
    fun publishPost(post: SocialPost) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.publishPost(post)
            if (result.isSuccess) {
                _postPublished.value = true
                // No need to call fetchFeed() because we have a real-time listener
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to publish post"
                _postPublished.value = false
            }
            _isLoading.value = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        feedListener?.remove()
    }
}
