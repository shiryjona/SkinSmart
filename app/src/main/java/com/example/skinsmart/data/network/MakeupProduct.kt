package com.example.skinsmart.data.network

import com.google.gson.annotations.SerializedName

/**
 * Wrapper for the Open Beauty Facts search API response.
 * Endpoint: GET /cgi/search.pl?search_terms=...&action=process&json=1
 */
data class OpenBeautyResponse(
    val products: List<MakeupProduct> = emptyList(),
    val count: Int = 0
)

/**
 * Represents a single beauty/skincare product from Open Beauty Facts.
 * Field names mapped from the JSON response via @SerializedName.
 */
data class MakeupProduct(
    val code: String = "",
    @SerializedName("product_name")
    val name: String = "",
    val brands: String? = "",
    @SerializedName("image_front_url")
    val imageUrl: String? = "",
    val categories: String? = ""
) {
    /** Convenience alias so existing code using product.id still works */
    val id: String get() = code

    /** Convenience alias so existing code using product.brand still works */
    val brand: String? get() = brands

    /** Price is not provided by Open Beauty Facts */
    val price: String? get() = null
}
