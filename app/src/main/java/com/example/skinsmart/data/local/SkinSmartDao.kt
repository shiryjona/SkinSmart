package com.example.skinsmart.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao
@JvmSuppressWildcards
interface SkinSmartDao {
    
    // ======== Smart Shelf Operations ========
    
    @Query("SELECT * FROM shelf_products")
    fun getAllShelfProducts(): LiveData<List<ShelfProduct>>
    
    @Query("SELECT * FROM shelf_products WHERE id = :productId LIMIT 1")
    suspend fun getShelfProductById(productId: String): ShelfProduct?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShelfProduct(product: ShelfProduct): Long

    @Update
    suspend fun updateShelfProduct(product: ShelfProduct): Int

    @Delete
    suspend fun deleteShelfProduct(product: ShelfProduct): Int
}
