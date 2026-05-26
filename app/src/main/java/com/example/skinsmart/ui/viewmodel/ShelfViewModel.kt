package com.example.skinsmart.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.skinsmart.data.local.ShelfProduct
import com.example.skinsmart.data.local.SkinSmartDatabase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class ShelfViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SkinSmartDatabase.getDatabase(application).skinSmartDao()

    private val currentUserId: String?
        get() = FirebaseAuth.getInstance().currentUser?.uid

    // Fetch all products from the local database by the current user's ID
    fun getUserShelfProducts(): LiveData<List<ShelfProduct>> {
        val uid = currentUserId ?: return MutableLiveData(emptyList())
        return dao.getAllShelfProducts(uid)
    }

    fun addProductToShelf(product: ShelfProduct) {
        val uid = currentUserId ?: return
        // Create a copy of the product with the current user ID
        val productWithUser = product.copy(userId = uid)
        viewModelScope.launch {
            dao.insertShelfProduct(productWithUser)
        }
    }

    fun removeProductFromShelf(product: ShelfProduct) {
        viewModelScope.launch {
            dao.deleteShelfProduct(product)
        }
    }

    fun updatePrivateNote(product: ShelfProduct, newNote: String) {
        viewModelScope.launch {
            product.privateNote = newNote
            dao.updateShelfProduct(product)
        }
    }
}