package com.example.skinsmart.model

/**
 * Data class representing a User in the SkinSmart application.
 * This class is used to serialize and deserialize user data to and from Firebase Firestore.
 */
data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val skinType: String = "", // e.g., "Oily", "Dry", "Combination"
    val avatarUrl: String = ""
)
