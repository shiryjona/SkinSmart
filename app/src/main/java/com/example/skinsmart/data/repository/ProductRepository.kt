package com.example.skinsmart.data.repository

import com.example.skinsmart.data.network.MakeupProduct
import com.example.skinsmart.data.network.RetrofitClient

/**
 * Repository for managing external product data from the Open Beauty Facts API.
 * Uses Result wrapper to handle network failures cleanly.
 */
class ProductRepository {

    private val api = RetrofitClient.makeupApi

    /**
     * Searches skincare/beauty products by keyword (e.g. "moisturizer", "cerave", "sunscreen").
     * If no query is provided, returns a default "skincare" search.
     */
    suspend fun searchProducts(query: String? = null): Result<List<MakeupProduct>> {
        return try {
            val response = api.getProducts(query = query?.takeIf { it.isNotEmpty() } ?: "skincare")
            val filtered = response.products.filter { it.name.isNotEmpty() }
            Result.success(filtered)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
