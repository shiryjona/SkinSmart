package com.example.skinsmart.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.example.skinsmart.data.local.ShelfProduct
import com.example.skinsmart.data.local.SkinSmartDatabase
import kotlinx.coroutines.launch

/**
 * AndroidViewModel is used here instead of regular ViewModel because Room Database requires Context.
 */
class ShelfViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = SkinSmartDatabase.getDatabase(application).skinSmartDao()

    // Room automatically updates LiveData without needing MutableLiveData internally!
    val allProducts: LiveData<List<ShelfProduct>> = dao.getAllShelfProducts()

    fun addProductToShelf(product: ShelfProduct) {
        viewModelScope.launch {
            dao.insertShelfProduct(product)
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
