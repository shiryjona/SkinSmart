package com.example.skinsmart.model

/**
 * Data class representing a User Post / Review in the social feed.
 */
data class Post(
    val postId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorSkinType: String = "",
    val authorAvatarUrl: String = "",
    val productName: String = "",
    val content: String = "",
    val rating: Float = 0f,
    val imageUrl: String = "",
    val timestamp: Long = 0L
)
