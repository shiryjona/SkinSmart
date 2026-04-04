package com.example.skinsmart.data.repository

import com.example.skinsmart.data.network.MakeupProduct
import com.example.skinsmart.data.network.RetrofitClient

/**
 * Repository for managing external product data from the Makeup API.
 * Uses Result wrapper to handle network failures cleanly.
 */
class ProductRepository {
    
    private val api = RetrofitClient.makeupApi

    /**
     * Fetches a list of cosmetic products matching given criteria.
     * @param productType E.g: "lipstick", "foundation"
     * @param brand E.g: "maybelline", "covergirl"
     */
    suspend fun searchProducts(productType: String? = null, brand: String? = null): Result<List<MakeupProduct>> {
        return try {
            val products = api.getProducts(productType, brand)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
