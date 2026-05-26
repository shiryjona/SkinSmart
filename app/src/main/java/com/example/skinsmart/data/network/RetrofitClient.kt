package com.example.skinsmart.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Singleton Retrofit Client to prevent memory leaks and handle API instantiation.
 */
object RetrofitClient {
    private const val BASE_URL = "https://world.openbeautyfacts.org/"

    val makeupApi: MakeupApi by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // Use GSON to parse JSON
            .build()
        retrofit.create(MakeupApi::class.java)
    }
}
