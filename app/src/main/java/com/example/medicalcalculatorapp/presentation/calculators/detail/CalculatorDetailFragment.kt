package com.example.medicalcalculatorapp.presentation.calculators.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.medicalcalculatorapp.databinding.FragmentCalculatorDetailBinding

class CalculatorDetailFragment : Fragment() {

    private var _binding: FragmentCalculatorDetailBinding? = null
    private val binding get() = _binding!!

    // This will be set up in Step 2 when we create ViewModel
    // private val viewModel: CalculatorDetailViewModel by viewModels { ... }

    // Will be set up when we add navigation args
    // private val args: CalculatorDetailFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculatorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup toolbar
        setupToolbar()

        // We'll implement these in subsequent steps
        // setupObservers()
        // setupListeners()
    }

    private fun setupToolbar() {
        // Basic toolbar setup with back navigation
        activity?.title = "Calculator" // We'll replace with actual calculator name later
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}