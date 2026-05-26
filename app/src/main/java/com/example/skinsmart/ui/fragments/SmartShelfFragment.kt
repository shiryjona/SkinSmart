package com.example.skinsmart.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skinsmart.databinding.FragmentSmartShelfBinding
import com.example.skinsmart.ui.adapters.ShelfAdapter
import com.example.skinsmart.ui.viewmodel.ShelfViewModel

class SmartShelfFragment : Fragment() {

    private var _binding: FragmentSmartShelfBinding? = null
    private val binding get() = _binding!!

    private lateinit var shelfViewModel: ShelfViewModel
    private lateinit var adapter: ShelfAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmartShelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        shelfViewModel = ViewModelProvider(this).get(ShelfViewModel::class.java)

        adapter = ShelfAdapter(
            shelfItems = emptyList(),
            onDeleteClicked = { product ->
                shelfViewModel.removeProductFromShelf(product)
            },
            onEditNoteClicked = { product ->
                // Show a dialog with an EditText so the user can write/edit their private note
                val input = EditText(requireContext()).apply {
                    setText(product.privateNote)
                    hint = "Write your private note here..."
                    setPadding(32, 16, 32, 16)
                }
                AlertDialog.Builder(requireContext())
                    .setTitle("Edit Private Note")
                    .setMessage("\"${product.name}\"")
                    .setView(input)
                    .setPositiveButton("Save") { _, _ ->
                        val newNote = input.text.toString().trim()
                        shelfViewModel.updatePrivateNote(product, newNote)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvShelf.layoutManager = LinearLayoutManager(requireContext())
        binding.rvShelf.adapter = adapter

        // Room LiveData — updates automatically on any DB change
        shelfViewModel.getUserShelfProducts().observe(viewLifecycleOwner) { products ->
            adapter.updateData(products)
            binding.tvEmptyShelf.visibility = if (products.isEmpty()) View.VISIBLE else View.GONE
            binding.rvShelf.visibility = if (products.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
