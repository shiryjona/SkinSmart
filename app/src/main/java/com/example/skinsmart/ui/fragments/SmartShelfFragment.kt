package com.example.skinsmart.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.skinsmart.databinding.FragmentSmartShelfBinding

class SmartShelfFragment : Fragment() {

    private var _binding: FragmentSmartShelfBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSmartShelfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // ShelfViewModel logic to load local Room DB items would go here
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
