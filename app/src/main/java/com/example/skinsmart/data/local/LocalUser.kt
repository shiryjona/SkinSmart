package com.example.skinsmart.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Cached version of the user profile for offline availability.
 */
@Entity(tableName = "user_profile")
data class LocalUser(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val skinType: String,
    val avatarUrl: String
)
