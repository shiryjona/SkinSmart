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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedViewModel: FeedViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var adapter: SocialPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use Activity scope so FeedViewModel is shared with CreatePostFragment
        feedViewModel = ViewModelProvider(requireActivity()).get(FeedViewModel::class.java)
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)

        // Observe current user to build adapter with the right userId
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            val currentUserId = user?.id

            // Build adapter with callbacks for the current user's own posts
            adapter = SocialPostAdapter(
                posts = feedViewModel.posts.value ?: emptyList(),
                currentUserId = currentUserId,
                onEditClicked = { post ->
                    val action = HomeFragmentDirections.actionHomeFragmentToEditPostFragment(post)
                    findNavController().navigate(action)
                },
                onDeleteClicked = { post ->
                    AlertDialog.Builder(requireContext())
                        .setTitle("Delete Post")
                        .setMessage("Are you sure you want to delete this review?")
                        .setPositiveButton("Delete") { _, _ ->
                            if (currentUserId != null) {
                                feedViewModel.deletePost(post.postId, currentUserId)
                            }
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            )
            binding.rvFeed.adapter = adapter

            // Deliver current posts to the new adapter
            feedViewModel.posts.value?.let { adapter.submitList(it) }
        }

        // Setup RecyclerView layout manager
        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.setHasFixedSize(false)

        binding.btnToggleFilter.setOnClickListener {
            val isVisible = binding.scrollSkinFilters.visibility == View.VISIBLE

            if (isVisible) {
                // If its opened -> close
                binding.scrollSkinFilters.visibility = View.GONE
                binding.btnToggleFilter.isSelected = false
            } else {
                // If its closed -> open
                binding.scrollSkinFilters.visibility = View.VISIBLE
                binding.btnToggleFilter.isSelected = true
            }
        }

        // Observe feed posts
        feedViewModel.posts.observe(viewLifecycleOwner) { posts ->
            if (::adapter.isInitialized) {
                adapter.submitList(posts)
            }
        }

        // Observe loading
        feedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe errors
        feedViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe delete success
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
