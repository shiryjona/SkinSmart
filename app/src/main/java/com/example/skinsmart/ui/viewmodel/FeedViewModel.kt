package com.example.skinsmart.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinsmart.data.repository.FeedRepository
import com.example.skinsmart.data.repository.StorageRepository
import com.example.skinsmart.model.SocialPost
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val feedRepository = FeedRepository()
    private val storageRepository = StorageRepository()
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
        feedListener = feedRepository.listenToFeed(
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
     * Publishes a new post. If an imageUri is provided, uploads it to Firebase Storage first,
     * then saves the post with the resulting download URL.
     */
    fun publishPost(post: SocialPost, imageUri: Uri? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val finalPost = if (imageUri != null) {
                    val uploadResult = storageRepository.uploadPostImage(imageUri, post.userId)
                    if (uploadResult.isFailure) {
                        _error.value = uploadResult.exceptionOrNull()?.message ?: "Image upload failed"
                        _isLoading.value = false
                        _postPublished.value = false
                        return@launch
                    }
                    post.copy(imageUrl = uploadResult.getOrNull() ?: "")
                } else {
                    post
                }

                val result = feedRepository.publishPost(finalPost)
                if (result.isSuccess) {
                    _postPublished.value = true
                } else {
                    _error.value = result.exceptionOrNull()?.message ?: "Failed to publish post"
                    _postPublished.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unexpected error"
                _postPublished.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        feedListener?.remove()
    }
}
