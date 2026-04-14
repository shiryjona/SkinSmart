package com.example.skinsmart.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.skinsmart.R
import com.example.skinsmart.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Setup Skin Type Spinner
        val skinTypes = arrayOf("Oily Skin", "Dry Skin", "Combination", "Normal", "Sensitive")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, skinTypes)
        binding.spinnerSkinType.adapter = adapter

        binding.tvBackToLogin.setOnClickListener {
            // Navigate back to Login
            findNavController().popBackStack()
        }

        binding.btnRegister.setOnClickListener {
            // Trigger AuthViewModel Registration here.
            // On success, navigate to Feed/Home
            findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
