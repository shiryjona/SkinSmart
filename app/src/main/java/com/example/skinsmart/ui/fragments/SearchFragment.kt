package com.example.skinsmart.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skinsmart.R
import com.example.skinsmart.data.local.ShelfProduct
import com.example.skinsmart.data.network.MakeupProduct
import com.example.skinsmart.databinding.FragmentSearchBinding
import com.example.skinsmart.ui.adapters.ProductAdapter
import com.example.skinsmart.ui.viewmodel.SearchViewModel
import com.example.skinsmart.ui.viewmodel.ShelfViewModel

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var searchViewModel: SearchViewModel
    private lateinit var shelfViewModel: ShelfViewModel
    private lateinit var adapter: ProductAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        shelfViewModel = ViewModelProvider(this).get(ShelfViewModel::class.java)

        // Setup RecyclerView
        adapter = ProductAdapter(
            products = emptyList(),
            onReviewClicked = { navigateToCreatePost() },
            onSaveClicked = { product -> saveProductToShelf(product) }
        )
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = adapter

        // Observe results
        searchViewModel.searchResults.observe(viewLifecycleOwner) { products ->
            adapter.updateData(products)
            binding.tvEmptyState.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe loading
        searchViewModel.isSearching.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        searchViewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }

        // Default load — skincare products
        searchViewModel.searchProducts()

        // Handle search input
        binding.etSearchBox.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchViewModel.searchProducts(query)
                binding.etSearchBox.clearFocus()
                return true
            }
            override fun onQueryTextChange(newText: String?) = false
        })
    }

    private fun saveProductToShelf(product: MakeupProduct) {
        val shelfProduct = ShelfProduct(
            id = product.id.ifEmpty { product.name },
            name = product.name,
            brand = product.brand ?: "",
            imageUrl = product.imageUrl ?: "",
            price = ""
        )
        shelfViewModel.addProductToShelf(shelfProduct)
        Toast.makeText(requireContext(), "\"${product.name}\" saved to shelf!", Toast.LENGTH_SHORT).show()
    }

    private fun navigateToCreatePost() {
        findNavController().navigate(R.id.action_searchFragment_to_createPostFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
