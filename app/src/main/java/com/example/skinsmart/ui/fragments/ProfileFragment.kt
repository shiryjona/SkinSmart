package com.example.skinsmart.ui.fragments

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
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
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private lateinit var feedViewModel: FeedViewModel
    private lateinit var postsAdapter: SocialPostAdapter

    private var profileBitmap: Bitmap? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            // Convert Uri to Bitmap
            val bitmap = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                val source = android.graphics.ImageDecoder.createSource(requireContext().contentResolver, it)
                android.graphics.ImageDecoder.decodeBitmap(source)
            } else {
                @Suppress("DEPRECATION")
                android.provider.MediaStore.Images.Media.getBitmap(requireContext().contentResolver, it)
            }
            profileBitmap = bitmap
            binding.ivProfileImage.setImageBitmap(bitmap)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)
        authViewModel = ViewModelProvider(requireActivity(), factory).get(AuthViewModel::class.java)
        feedViewModel = ViewModelProvider(requireActivity()).get(FeedViewModel::class.java)

        binding.rvMyPosts.layoutManager = LinearLayoutManager(requireContext())

        // Setup Skin Type Spinner
        val skinTypes = arrayOf("Oily", "Dry", "Combination", "Normal", "Sensitive")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, skinTypes)
        binding.spinnerSkinType.adapter = spinnerAdapter

        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.name.ifEmpty { "Guest User" }
                binding.tvUserSkinType.text = "Skin Type: ${user.skinType}"

                // Load Profile Image
                if (profileBitmap == null && user.avatarUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(user.avatarUrl)
                        .placeholder(R.drawable.ic_launcher_foreground) // Use a proper resource
                        .error(R.drawable.ic_launcher_foreground)
                        .into(binding.ivProfileImage)
                }

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
                authViewModel.refreshStats(user.id)
            } else {
                findNavController().navigate(R.id.loginFragment)
            }
        }

        authViewModel.cachedUser.observe(viewLifecycleOwner) { cached ->
            if (authViewModel.currentUser.value == null && cached != null) {
                binding.tvUserName.text = cached.name
                binding.tvUserSkinType.text = "Skin Type: ${cached.skinType} (Offline)"
                if (cached.avatarUrl.isNotEmpty()) {
                    Picasso.get().load(cached.avatarUrl).into(binding.ivProfileImage)
                }
            }
        }

        authViewModel.reviewCount.observe(viewLifecycleOwner) { count ->
            binding.tvReviewCount.text = count.toString()
        }

        authViewModel.shelfCount.observe(viewLifecycleOwner) { count ->
            binding.tvShelfCount.text = count.toString()
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
            toggleEditMode(true)
        }

        binding.btnCancelEdit.setOnClickListener {
            toggleEditMode(false)
            profileBitmap = null
            // Reset image from user data
            val user = authViewModel.currentUser.value
            if (user != null && user.avatarUrl.isNotEmpty()) {
                Picasso.get().load(user.avatarUrl).into(binding.ivProfileImage)
            } else {
                binding.ivProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
            }
        }

        binding.layoutProfileImage.setOnClickListener {
            if (binding.layoutEditMode.visibility == View.VISIBLE) {
                pickImageLauncher.launch("image/*")
            }
        }

        binding.btnSaveProfile.setOnClickListener {
            val name = binding.etEditName.text.toString().trim()
            val skinType = binding.spinnerSkinType.selectedItem.toString()

            if (name.isEmpty()) {
                binding.etEditName.error = "Name cannot be empty"
                return@setOnClickListener
            }

            authViewModel.updateUserProfile(name, skinType, profileBitmap)
            toggleEditMode(false)
            profileBitmap = null
        }

        authViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // You could add a progress bar here if needed
            binding.btnSaveProfile.isEnabled = !isLoading
        }

        authViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun toggleEditMode(isEditing: Boolean) {
        binding.layoutViewMode.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.layoutEditMode.visibility = if (isEditing) View.VISIBLE else View.GONE

        binding.btnEditProfile.visibility = if (isEditing) View.GONE else View.VISIBLE
        binding.btnLogout.visibility = if (isEditing) View.GONE else View.VISIBLE

        binding.btnSaveProfile.visibility = if (isEditing) View.VISIBLE else View.GONE
        binding.btnCancelEdit.visibility = if (isEditing) View.VISIBLE else View.GONE
        
        binding.viewImageOverlay.visibility = if (isEditing) View.VISIBLE else View.GONE
        binding.ivEditIcon.visibility = if (isEditing) View.VISIBLE else View.GONE

        if (isEditing) {
            val user = authViewModel.currentUser.value
            binding.etEditName.setText(user?.name)

            // Set spinner selection
            val skinTypes = arrayOf("Oily", "Dry", "Combination", "Normal", "Sensitive")
            val index = skinTypes.indexOf(user?.skinType).coerceAtLeast(0)
            binding.spinnerSkinType.setSelection(index)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
