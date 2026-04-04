package com.example.skinsmart.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface representing the required endpoints for Makeup API.
 */
interface MakeupApi {
    
    @GET("api/v1/products.json")
    suspend fun getProducts(
        @Query("product_type") productType: String? = null,
        @Query("brand") brand: String? = null
    ): List<MakeupProduct>
    
}
