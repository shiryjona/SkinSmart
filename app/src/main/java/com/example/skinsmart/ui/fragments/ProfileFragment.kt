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
import com.example.skinsmart.R
import com.example.skinsmart.databinding.FragmentProfileBinding
import com.example.skinsmart.ui.adapters.SocialPostAdapter
import com.example.skinsmart.ui.viewmodel.AuthViewModel
import com.example.skinsmart.ui.viewmodel.FeedViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var postsAdapter: SocialPostAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        feedViewModel = ViewModelProvider(requireActivity()).get(FeedViewModel::class.java)

        binding.rvMyPosts.layoutManager = LinearLayoutManager(requireContext())

        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.name.ifEmpty { "Guest User" }
                binding.tvUserSkinType.text = "Skin Type: ${user.skinType}"

                // Build adapter with the user's ID so Edit/Delete show for all posts in this list
                postsAdapter = SocialPostAdapter(
                    posts = emptyList(),
                    currentUserId = user.id,   // ← all posts here belong to this user
                    onEditClicked = { post ->
                        val action = ProfileFragmentDirections.actionProfileFragmentToEditPostFragment(post)
                        findNavController().navigate(action)
                    },
                    onDeleteClicked = { post ->
                        AlertDialog.Builder(requireContext())
                            .setTitle("Delete Post")
                            .setMessage("Are you sure you want to delete this review?")
                            .setPositiveButton("Delete") { _, _ ->
                                feedViewModel.deletePost(post.postId, user.id)
                            }
                            .setNegativeButton("Cancel", null)
                            .show()
                    }
                )
                binding.rvMyPosts.adapter = postsAdapter

                // Load the user's posts
                feedViewModel.loadUserPosts(user.id)
            } else {
                findNavController().navigate(R.id.loginFragment)
            }
        }

        // Deliver loaded posts to the adapter whenever they change
        feedViewModel.userPosts.observe(viewLifecycleOwner) { posts ->
            if (::postsAdapter.isInitialized) {
                postsAdapter.submitList(posts)
            }
            binding.tvNoPostsYet.visibility = if (posts.isEmpty()) View.VISIBLE else View.GONE
        }

        feedViewModel.actionSuccess.observe(viewLifecycleOwner) { msg ->
            if (!msg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
        }

        binding.btnEditProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Edit profile coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
