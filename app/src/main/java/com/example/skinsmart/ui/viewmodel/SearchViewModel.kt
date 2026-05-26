package com.example.skinsmart.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skinsmart.data.network.MakeupProduct
import com.example.skinsmart.data.repository.ProductRepository
import kotlinx.coroutines.launch

class SearchViewModel : ViewModel() {

    private val repository = ProductRepository()

    private val _searchResults = MutableLiveData<List<MakeupProduct>>()
    val searchResults: LiveData<List<MakeupProduct>> = _searchResults

    private val _isSearching = MutableLiveData<Boolean>()
    val isSearching: LiveData<Boolean> = _isSearching

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Fetches beauty/skincare products matching the given keyword.
     * Call with null or empty to load default "skincare" results.
     */
    fun searchProducts(query: String? = null) {
        _isSearching.value = true
        viewModelScope.launch {
            val result = repository.searchProducts(query)
            if (result.isSuccess) {
                _searchResults.value = result.getOrNull() ?: emptyList()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Search failed"
            }
            _isSearching.value = false
        }
    }
}
