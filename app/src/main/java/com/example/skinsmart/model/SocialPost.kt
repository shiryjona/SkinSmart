package com.example.skinsmart.model

import java.io.Serializable

/**
 * Data class representing an entry in the global SkinSmart social feed.
 */
data class SocialPost(
    val postId: String = "",
    val userId: String = "",
    val authorName: String = "",
    val authorAvatarUrl: String = "",
    val authorSkinType: String = "",
    val productName: String = "",
    val reviewText: String = "",
    val rating: Int = 0,
    val imageUrl: String = "",
    val timestamp: Long = System.currentTimeMillis()
) : Serializable

