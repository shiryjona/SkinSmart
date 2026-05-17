package com.example.skinsmart.data.network

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for the Open Beauty Facts API.
 * Base URL: https://world.openbeautyfacts.org/
 * Docs: https://wiki.openbeautyfacts.org/API
 */
interface MakeupApi {

    /**
     * Search beauty/skincare products by keyword.
     * Example: search_terms="moisturizer", "cerave", "sunscreen"
     */
    @GET("cgi/search.pl")
    suspend fun getProducts(
        @Query("search_terms") query: String? = "skincare",
        @Query("action") action: String = "process",
        @Query("json") json: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("search_simple") simple: Int = 1
    ): OpenBeautyResponse
}
