package com.example.skinsmart.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a saved product in the user's local "Smart Shelf".
 * This entity is stored exclusively in the local Room database to ensure privacy
 * for the user's personal notes.
 */
@Entity(tableName = "shelf_products")
data class ShelfProduct(
    @PrimaryKey
    val id: String, // E.g., from the Makeup API
    val name: String,
    val brand: String,
    val imageUrl: String,
    val price: String,
    var privateNote: String = "" // Fulfills the "Private Notes" MVP requirement
)
