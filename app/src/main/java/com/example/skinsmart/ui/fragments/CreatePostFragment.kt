package com.example.skinsmart.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreatePostBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Use Activity scope so we share data with the entire app
        authViewModel = ViewModelProvider(requireActivity()).get(AuthViewModel::class.java)
        feedViewModel = ViewModelProvider(requireActivity()).get(FeedViewModel::class.java)

        binding.toolbarCreatePost.setNavigationOnClickListener {
            findNavController().navigateUp()
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
                feedViewModel.publishPost(newPost)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
