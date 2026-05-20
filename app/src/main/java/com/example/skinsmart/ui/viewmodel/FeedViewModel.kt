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
     * Resets the post publication state and general actions.
     */
    fun resetPostState() {
        _postPublished.value = null
        _actionSuccess.value = null
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

    private val _userPosts = MutableLiveData<List<SocialPost>>()
    val userPosts: LiveData<List<SocialPost>> = _userPosts

    private val _actionSuccess = MutableLiveData<String?>()
    val actionSuccess: LiveData<String?> = _actionSuccess

    /**
     * Loads all posts belonging to the current user.
     */
    fun loadUserPosts(userId: String) {
        _isLoading.value = true
        viewModelScope.launch {
            val result = feedRepository.getUserPosts(userId)
            if (result.isSuccess) {
                _userPosts.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Failed to load posts"
            }
            _isLoading.value = false
        }
    }

    /**
     * Deletes the user's post from Firestore.
     */
    fun deletePost(postId: String, userId: String) {
        viewModelScope.launch {
            feedRepository.deletePost(postId)
            loadUserPosts(userId) // Refresh the list after deletion
            _actionSuccess.value = "Post deleted"
        }
    }

    /**
     * Updates an existing post (text + rating + optional new image).
     */
    fun updatePost(post: SocialPost, newImageUri: android.net.Uri? = null) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val finalPost = if (newImageUri != null) {
                    val uploadResult = storageRepository.uploadPostImage(newImageUri, post.userId)
                    if (uploadResult.isFailure) {
                        _error.value = uploadResult.exceptionOrNull()?.message ?: "Image upload failed"
                        _isLoading.value = false
                        return@launch
                    }
                    post.copy(imageUrl = uploadResult.getOrNull() ?: post.imageUrl)
                } else post
                feedRepository.updatePost(finalPost)
                _actionSuccess.value = "Post updated"
                loadUserPosts(post.userId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Update failed"
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
