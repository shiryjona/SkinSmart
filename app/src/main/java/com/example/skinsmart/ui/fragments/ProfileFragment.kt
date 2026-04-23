package com.example.skinsmart.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.skinsmart.R
import com.example.skinsmart.databinding.FragmentProfileBinding
import com.example.skinsmart.ui.viewmodel.AuthViewModel

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var authViewModel: AuthViewModel

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

        authViewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvUserName.text = user.name.ifEmpty { "Guest User" }
                binding.tvUserSkinType.text = "Skin Type: ${user.skinType}"
            } else {
                // If logged out, redirect to Login
                findNavController().navigate(R.id.loginFragment)
            }
        }

        binding.btnLogout.setOnClickListener {
            authViewModel.logout()
            // The observer above will catch the null user and redirect
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
