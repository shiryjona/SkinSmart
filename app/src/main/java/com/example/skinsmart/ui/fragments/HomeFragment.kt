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

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var feedViewModel: FeedViewModel
    private lateinit var authViewModel: AuthViewModel
    private lateinit var adapter: SocialPostAdapter

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

                val backgroundColors = intArrayOf(
                    skinType.bgColorInt,
                    Color.parseColor("#F3F4F6")
                )

                // השורה שהייתה חסרה:
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

        binding.rvFeed.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFeed.setHasFixedSize(false)

        // 1. יצירת האדפטר באופן מיידי ללא ה-ID של המשתמש (כדי שהפיד יוכל להיטען מיד)
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
                        // נשתמש ב-ID מהאדפטר, כי הוא כבר יעודכן עד כאן
                        adapter.currentUserId?.let { uid ->
                            feedViewModel.deletePost(post.postId, uid)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        )
        binding.rvFeed.adapter = adapter

        // 2. כשהמשתמש מגיע, אנחנו רק מעדכנים את האדפטר הקיים
        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                adapter.currentUserId = user.id
                // מרעננים את התצוגה כדי שהכפתורי עריכה יופיעו במידת הצורך
                adapter.notifyDataSetChanged()
            }
        }

        // 3. כשהפוסטים מגיעים, מכניסים אותם לאדפטר שתמיד קיים
        feedViewModel.posts.observe(viewLifecycleOwner) { posts ->
            adapter.submitList(posts)
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
