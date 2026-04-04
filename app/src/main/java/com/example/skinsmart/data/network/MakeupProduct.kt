package com.example.skinsmart.data.network

import com.google.gson.annotations.SerializedName

/**
 * Data model matching the response structure of the Makeup API.
 * The @SerializedName annotation maps the JSON keys to Kotlin properties.
 */
data class MakeupProduct(
    val id: Int,
    val brand: String? = "",
    val name: String = "",
    val price: String? = "0.0",
    @SerializedName("image_link") 
    val imageUrl: String? = "",
    val description: String? = "",
    @SerializedName("product_type") 
    val productType: String? = ""
)
