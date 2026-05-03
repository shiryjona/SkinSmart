package com.example.skinsmart.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinsmart.data.repository.FeedRepository
import com.example.skinsmart.model.SocialPost
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val repository = FeedRepository()

    private val _posts = MutableLiveData<List<SocialPost>>()
    val posts: LiveData<List<SocialPost>> = _posts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _postPublished = MutableLiveData<Boolean>()
    val postPublished: LiveData<Boolean> = _postPublished

    init {
        fetchFeed()
    }

    /**
     * Fetches the global social feed.
     */
    fun fetchFeed() {
        _isLoading.value = true
        viewModelScope.launch {
            val result = repository.getGlobalFeed()
            if (result.isSuccess) {
                _posts.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to fetch feed"
            }
            _isLoading.value = false
        }
    }

    /**
     * Publishes a new review to the global feed.
     */
    fun publishPost(post: SocialPost) {
        _isLoading.value = true
        _postPublished.value = false
        viewModelScope.launch {
            val result = repository.publishPost(post)
            if (result.isSuccess) {
                _postPublished.value = true
                fetchFeed() // Refresh feed
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to publish post"
            }
            _isLoading.value = false
        }
    }
}
