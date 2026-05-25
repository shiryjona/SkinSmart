package com.example.skinsmart.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.skinsmart.databinding.FragmentHomeBinding
import com.example.skinsmart.ui.adapters.SocialPostAdapter
import com.example.skinsmart.ui.viewmodel.AuthViewModel
import com.example.skinsmart.ui.viewmodel.FeedViewModel
import com.google.android.material.chip.Chip
import android.content.res.ColorStateList
import android.graphics.Color
import com.example.skinsmart.model.SkinType
import com.example.skinsmart.model.SocialPost

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedViewModel: FeedViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var adapter: SocialPostAdapter
    private var originalPosts: List<SocialPost> = emptyList()
    private val selectedSkinTypes = mutableSetOf<SkinType>()

    private fun setupSkinTypeChips() {
        binding.cgSkinTypeFilter.removeAllViews()

        SkinType.entries.filter { it != SkinType.UNKNOWN }.forEach { skinType ->
            val chip = Chip(requireContext()).apply {
                text = skinType.label
                isCheckable = true
                isClickable = true

                val states = arrayOf(
                    intArrayOf(android.R.attr.state_checked),
                    intArrayOf(-android.R.attr.state_checked)
                )

                // If the skin type is already selected, set the chip to checked
                isChecked = selectedSkinTypes.contains(skinType)

                // Listener to handle chip selection
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        selectedSkinTypes.add(skinType) // Was clicked add to list
                    } else {
                        selectedSkinTypes.remove(skinType) // Unclicked remove from list
                    }
                    // Active local filter
                    applyLocalFilter()
                }

                val backgroundColors = intArrayOf(
                    skinType.bgColorInt,
                    Color.parseColor("#F3F4F6")
                )

                chipBackgroundColor = ColorStateList(states, backgroundColors)

                val textColors = intArrayOf(
                    skinType.textColorInt,
                    Color.parseColor("#4B5563")
                )
                setTextColor(ColorStateList(states, textColors))

                isChipIconVisible = false
                isCheckedIconVisible = false
            }

            binding.cgSkinTypeFilter.addView(chip)
        }
    }

    private fun applyLocalFilter() {
        if (selectedSkinTypes.isEmpty()) {
            // When nothing is selected, show all posts
            adapter.submitList(originalPosts)
        } else {
            // When skin types are selected, filter the posts
            val filteredList = originalPosts.filter { post ->
                val postSkinType = SkinType.fromString(post.authorSkinType)

                // Check if the post's skin type is in the selected types
                selectedSkinTypes.contains(postSkinType)
            }
            adapter.submitList(filteredList)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        feedViewModel = ViewModelProvider(requireActivity()).get(FeedViewModel::class.java)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        setupSkinTypeChips()

        // Recreate the adapter with the new list
        val hasActiveFilters = selectedSkinTypes.isNotEmpty()
        if (hasActiveFilters) {
            binding.scrollSkinFilters.visibility = View.VISIBLE
            binding.btnToggleFilter.isSelected = true
        } else {
            binding.scrollSkinFilters.visibility = View.GONE
            binding.btnToggleFilter.isSelected = false
        }

        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.setHasFixedSize(false)

        // Create the adapter with empty list and null currentUserId
        adapter = SocialPostAdapter(
            posts = emptyList(),
            currentUserId = null,
            onEditClicked = { post ->
                val action = HomeFragmentDirections.actionHomeFragmentToEditPostFragment(post)
                findNavController().navigate(action)
            },
            onDeleteClicked = { post ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Delete Post")
                    .setMessage("Are you sure you want to delete this review?")
                    .setPositiveButton("Delete") { _, _ ->
                        // Use user's UID to delete the post
                        adapter.currentUserId?.let { uid ->
                            feedViewModel.deletePost(post.postId, uid)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvFeed.adapter = adapter

        // Update the adapter with the current user's UID
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                adapter.currentUserId = user.id
                // Refresh the adapter with the new currentUserId
                adapter.notifyDataSetChanged()
            }
        }

        // When posts are loaded, submit them to the adapter
        feedViewModel.posts.observe(viewLifecycleOwner) { posts ->
            originalPosts = posts
            applyLocalFilter()
        }

        binding.btnToggleFilter.setOnClickListener {
            val isVisible = binding.scrollSkinFilters.visibility == View.VISIBLE

            if (isVisible) {
                binding.scrollSkinFilters.visibility = View.GONE
                binding.btnToggleFilter.isSelected = false
            } else {
                binding.scrollSkinFilters.visibility = View.VISIBLE
                binding.btnToggleFilter.isSelected = true
            }
        }

        feedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        feedViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        feedViewModel.actionSuccess.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
