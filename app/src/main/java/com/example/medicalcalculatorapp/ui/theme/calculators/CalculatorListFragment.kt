package com.example.medicalcalculatorapp.ui.theme.calculators

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medicalcalculatorapp.data.model.MedicalCalculator
import com.example.medicalcalculatorapp.data.repository.CalculatorRepository
import com.example.medicalcalculatorapp.databinding.FragmentCalculatorListBinding
import com.example.medicalcalculatorapp.R

class CalculatorListFragment : Fragment() {

    private var _binding: FragmentCalculatorListBinding? = null
    private val binding get() = _binding!!

    private lateinit var calculatorAdapter: CalculatorAdapter
    private val calculatorRepository = CalculatorRepository()

    // Track current filter mode
    private var currentFilterMode = FilterMode.ALL

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculatorListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFilterButtons()
        loadCalculators()
    }

    private fun setupRecyclerView() {
        calculatorAdapter = CalculatorAdapter(
            onItemClick = { calculator ->
                // Will implement calculator detail later
                Toast.makeText(requireContext(), "Selected: ${calculator.name}", Toast.LENGTH_SHORT).show()
            },
            onFavoriteClick = { calculator ->
                // Toggle favorite status
                val updatedCalculator = calculator.copy(isFavorite = !calculator.isFavorite)
                // In a real app, would update the database
                Toast.makeText(
                    requireContext(),
                    if (updatedCalculator.isFavorite) "Added to favorites" else "Removed from favorites",
                    Toast.LENGTH_SHORT
                ).show()

                // Refresh list with updated data
                loadCalculators()
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = calculatorAdapter
        }
    }

    private fun setupFilterButtons() {
        // Set up bottom navigation
//        binding.bottomNavigation.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                binding.btnHome.id -> {
//                    // Would navigate to home dashboard in a real app
//                    true
//                }
//                binding.btnSettings.id -> {
//                    // Would navigate to settings in a real app
//                    Toast.makeText(requireContext(), "Settings", Toast.LENGTH_SHORT).show()
//                    true
//                }
//                else -> false
//            }
//        }
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnHome -> {
                    // Would navigate to home dashboard in a real app
                    true
                }
                R.id.btnSettings -> {
                    // Would navigate to settings in a real app
                    Toast.makeText(requireContext(), "Settings", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
        // Set up top filter buttons
        binding.btnAll.setOnClickListener {
            currentFilterMode = FilterMode.ALL
            updateFilterButtonsUI()
            loadCalculators()
        }

        binding.btnFavorites.setOnClickListener {
            currentFilterMode = FilterMode.FAVORITES
            updateFilterButtonsUI()
            loadCalculators()
        }
    }

    private fun updateFilterButtonsUI() {
        // Update UI to show which filter is selected
        binding.btnAll.alpha = if (currentFilterMode == FilterMode.ALL) 1.0f else 0.5f
        binding.btnFavorites.alpha = if (currentFilterMode == FilterMode.FAVORITES) 1.0f else 0.5f
    }

    private fun loadCalculators() {
        val allCalculators = calculatorRepository.getCalculators()

        // Apply filter
        val filteredCalculators = when (currentFilterMode) {
            FilterMode.ALL -> allCalculators
            FilterMode.FAVORITES -> allCalculators.filter { it.isFavorite }
        }

        calculatorAdapter.submitList(filteredCalculators)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    enum class FilterMode {
        ALL, FAVORITES
    }
}