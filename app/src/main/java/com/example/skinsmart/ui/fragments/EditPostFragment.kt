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
import com.example.skinsmart.databinding.FragmentEditPostBinding
import com.example.skinsmart.model.SocialPost
import com.example.skinsmart.ui.viewmodel.FeedViewModel
import com.squareup.picasso.Picasso

class EditPostFragment : Fragment() {

    private var _binding: FragmentEditPostBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedViewModel: FeedViewModel
    private var selectedImageUri: Uri? = null
    private lateinit var currentPost: SocialPost

    private val imagePickerLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                selectedImageUri = uri
                binding.ivEditPostPreview.setImageURI(uri)
                binding.ivEditPostPreview.visibility = View.VISIBLE
                binding.btnEditPickImage.text = "Change Photo Again"
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditPostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        feedViewModel = ViewModelProvider(requireActivity()).get(FeedViewModel::class.java)

        // Receive the post data via Bundle (Serializable)
        @Suppress("DEPRECATION")
        currentPost = requireArguments().getSerializable("post") as SocialPost

        // Pre-fill fields with existing post data
        binding.etEditProductName.setText(currentPost.productName)
        binding.etEditReviewText.setText(currentPost.reviewText)
        binding.rbEditRating.rating = currentPost.rating.toFloat()

        // Show existing image if any
        if (currentPost.imageUrl.isNotEmpty()) {
            binding.ivEditPostPreview.visibility = View.VISIBLE
            Picasso.get().load(currentPost.imageUrl).into(binding.ivEditPostPreview)
        }

        binding.toolbarEditPost.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditPickImage.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        binding.btnSaveChanges.setOnClickListener {
            val newProductName = binding.etEditProductName.text.toString().trim()
            val newReview = binding.etEditReviewText.text.toString().trim()
            val newRating = binding.rbEditRating.rating.toInt()

            if (newProductName.isEmpty() || newReview.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val updatedPost = currentPost.copy(
                productName = newProductName,
                reviewText = newReview,
                rating = newRating
            )

            feedViewModel.updatePost(updatedPost, selectedImageUri)
        }

        feedViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.pbEditLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.btnSaveChanges.isEnabled = !isLoading
        }

        feedViewModel.actionSuccess.observe(viewLifecycleOwner) { message ->
            if (message == "Post updated") {
                Toast.makeText(requireContext(), "Post updated!", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        feedViewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
