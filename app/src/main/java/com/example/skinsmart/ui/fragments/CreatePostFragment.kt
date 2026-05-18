package com.example.skinsmart.ui.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.skinsmart.databinding.FragmentCreatePostBinding
import com.example.skinsmart.model.SocialPost
import com.example.skinsmart.ui.viewmodel.AuthViewModel
import com.example.skinsmart.ui.viewmodel.FeedViewModel

class CreatePostFragment : Fragment() {

    private var _binding: FragmentCreatePostBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private lateinit var feedViewModel: FeedViewModel

    // Holds the URI of the image the user picked from gallery
    private var selectedImageUri: Uri? = null

    // Launches the system image picker and handles the result
    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivPostPreview.setImageURI(uri)
                binding.ivPostPreview.visibility = View.VISIBLE
                binding.btnPickImage.text = "Change Photo"
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        feedViewModel = ViewModelProvider(requireActivity()).get(FeedViewModel::class.java)

        binding.toolbarCreatePost.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val args = CreatePostFragmentArgs.fromBundle(requireArguments())
        if (args.productName.isNotEmpty()) {
            binding.etProductName.setText(args.productName)
        }

        // Reset state so we don't immediately trigger success logic from a previous post
        feedViewModel.resetPostState()

        // Open gallery when user taps "Add Photo"
        binding.btnPickImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSubmitReview.setOnClickListener {
            val productName = binding.etProductName.text.toString().trim()
            val text = binding.etReviewText.text.toString().trim()
            val rating = binding.rbNewRating.rating.toInt()

            if (productName.isEmpty() || text.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentUser = authViewModel.currentUser.value
            if (currentUser != null) {
                val newPost = SocialPost(
                    userId = currentUser.id,
                    authorName = currentUser.name,
                    authorSkinType = currentUser.skinType,
                    productName = productName,
                    reviewText = text,
                    rating = rating
                )
                // Pass the selected image URI (null = no image, text-only post)
                feedViewModel.publishPost(newPost, selectedImageUri)
            } else {
                Toast.makeText(requireContext(), "Must be logged in to post", Toast.LENGTH_SHORT).show()
            }
        }

        feedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSubmitReview.isEnabled = !isLoading
        }

        feedViewModel.postPublished.observe(viewLifecycleOwner) { success ->
            if (success == true) {
                Toast.makeText(requireContext(), "Post published!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        feedViewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
