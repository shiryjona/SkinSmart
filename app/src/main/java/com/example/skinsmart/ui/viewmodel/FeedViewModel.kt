package com.example.skinsmart.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinsmart.data.network.MakeupProduct
import com.example.skinsmart.data.repository.ProductRepository
import kotlinx.coroutines.launch

class FeedViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _searchResults = MutableLiveData<List<MakeupProduct>>()
    val searchResults: LiveData<List<MakeupProduct>> = _searchResults

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Executes a search query via Retrofit on the external API.
     */
    fun searchMakeupProducts(type: String? = null, brand: String? = null) {
        _isSearching.value = true
        viewModelScope.launch {
            val result = repository.searchProducts(type, brand)
            if (result.isSuccess) {
                _searchResults.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Search failed"
            }
            _isSearching.value = false
        }
    }
}
