package com.example.medicalcalculatorapp.presentation.calculators

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.FragmentCalculatorListBinding
import com.example.medicalcalculatorapp.di.AppDependencies
import com.example.medicalcalculatorapp.presentation.auth.PrivacyAndDisclaimerDialogFragment
import com.example.medicalcalculatorapp.util.SecureStorageManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

class CalculatorListFragment : Fragment() {

    private var _binding: FragmentCalculatorListBinding? = null
    private val binding get() = _binding!!

    private lateinit var calculatorAdapter: CalculatorAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var secureStorageManager: SecureStorageManager

    // Create ViewModel using our Factory
    private val viewModel: CalculatorListViewModel by viewModels {
        CalculatorListViewModel.Factory(
            AppDependencies.provideCalculatorRepository(requireContext()),
            AppDependencies.provideUserManager(requireContext()),
            AppDependencies.provideCategoryRepository(requireContext())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureStorageManager = SecureStorageManager(requireContext())
    }

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

        // Check if user has accepted privacy policy and disclaimer
        if (!secureStorageManager.isDisclaimerAccepted()) {
            showPrivacyAndDisclaimerDialog()
        }

        setupRecyclerView()
        setupCategoryRecyclerView()
        setupFilterButtons()
        setupSearchView()
        observeViewModel()

        // Show loading initially
        showLoading(true)
    }

    private fun showPrivacyAndDisclaimerDialog() {
        val dialog = PrivacyAndDisclaimerDialogFragment.newInstance()
        dialog.setOnAcceptedListener {
            // User accepted both privacy policy and disclaimer
            Toast.makeText(requireContext(), "Políticas aceptadas. Bienvenido a MediCálculos", Toast.LENGTH_SHORT).show()
        }

        try {
            dialog.show(parentFragmentManager, PrivacyAndDisclaimerDialogFragment.TAG)
        } catch (e: Exception) {
            // If there's an issue with the dialog, log it but don't crash
            println("❌ Error showing privacy dialog: ${e.message}")
            // For development, you might want to just mark as accepted
            // secureStorageManager.saveDisclaimerAccepted(true)
        }
    }

    private fun setupRecyclerView() {
        calculatorAdapter = CalculatorAdapter(
            onItemClick = { calculator ->
                try {
                    val bundle = Bundle().apply {
                        putString("calculatorId", calculator.id)
                    }
                    findNavController().navigate(
                        R.id.action_calculatorListFragment_to_calculatorDetailFragment,
                        bundle
                    )
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error opening calculator: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            onFavoriteClick = { calculator ->
                viewModel.toggleFavorite(calculator.id)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = calculatorAdapter
            setPadding(0, 0, 0, 100)
            clipToPadding = false
        }
    }

//    private fun setupCategoryRecyclerView() {
//        categoryAdapter = CategoryAdapter { selectedCategory ->
//            // Handle category selection (null means "All Categories")
//            viewModel.selectCategory(selectedCategory?.category?.id)
//        }
//
//        binding.recyclerViewCategories.apply {
//            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            adapter = categoryAdapter
//        }
//    }

    private fun setupCategoryRecyclerView() {
        println("🔍 DEBUG: Setting up category RecyclerView")

        categoryAdapter = CategoryAdapter { selectedCategory ->
            println("🔍 DEBUG: Category selected: ${selectedCategory?.category?.name}")
            viewModel.selectCategory(selectedCategory?.category?.id)
        }

        binding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            visibility = View.VISIBLE // Force visibility
            println("🔍 DEBUG: Category RecyclerView setup complete, visibility: $visibility")
        }
    }

    private fun setupFilterButtons() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnHome -> true
                R.id.btnSettings -> {
                    Toast.makeText(requireContext(), "Configuración - Próximamente", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        binding.btnAll.setOnClickListener {
            viewModel.setFilterMode(CalculatorListViewModel.FilterMode.ALL)
        }

        binding.btnFavorites.setOnClickListener {
            viewModel.setFilterMode(CalculatorListViewModel.FilterMode.FAVORITES)
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.searchCalculators(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrBlank()) {
                    viewModel.searchCalculators(null)
                }
                return true
            }
        })
    }

    private fun updateFilterButtonsUI(filterMode: CalculatorListViewModel.FilterMode) {
        binding.btnAll.alpha = if (filterMode == CalculatorListViewModel.FilterMode.ALL) 1.0f else 0.6f
        binding.btnFavorites.alpha = if (filterMode == CalculatorListViewModel.FilterMode.FAVORITES) 1.0f else 0.6f
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE
    }

//    private fun observeViewModel() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                launch {
//                    viewModel.calculators.collectLatest { calculators ->
//                        calculatorAdapter.submitList(calculators)
//
//                        if (calculators.isEmpty() && !viewModel.isLoading.value) {
//                            showEmptyState()
//                        } else {
//                            hideEmptyState()
//                        }
//                    }
//                }
//
//                launch {
//                    viewModel.isLoading.collectLatest { isLoading ->
//                        showLoading(isLoading)
//                    }
//                }
//
//                launch {
//                    viewModel.error.collectLatest { errorMessage ->
//                        errorMessage?.let {
//                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//
//                launch {
//                    viewModel.currentFilterMode.collectLatest { mode ->
//                        updateFilterButtonsUI(mode)
//                    }
//
////                    launch {
////                        viewModel.categories.collectLatest { categories ->
////                            categoryAdapter.submitList(categories)
////                        }
////                    }
//                    launch {
//                        viewModel.categories.collectLatest { categories ->
//                            println("🔍 DEBUG Fragment: Received ${categories.size} categories for adapter")
//                            categoryAdapter.submitList(categories)
//                        }
//                    }
//
//                    launch {
//                        viewModel.selectedCategoryId.collectLatest { selectedId ->
//                            categoryAdapter.setSelectedCategory(selectedId)
//                        }
//                    }
//
//
//                }
//            }
//        }
//    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe calculators
                launch {
                    viewModel.calculators.collectLatest { calculators ->
                        calculatorAdapter.submitList(calculators)

                        if (calculators.isEmpty() && !viewModel.isLoading.value) {
                            showEmptyState()
                        } else {
                            hideEmptyState()
                        }
                    }
                }

                // Observe categories - FIX HERE
                launch {
                    viewModel.categories.collectLatest { categories ->
                        println("🔍 DEBUG Fragment: Received ${categories.size} categories for adapter")
                        categoryAdapter.submitList(categories)
                    }
                }

                // Observe selected category
                launch {
                    viewModel.selectedCategoryId.collectLatest { selectedId ->
                        println("🔍 DEBUG Fragment: Selected category ID: $selectedId")
                        categoryAdapter.setSelectedCategory(selectedId)
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        showLoading(isLoading)
                    }
                }

                // Observe errors
                launch {
                    viewModel.error.collectLatest { errorMessage ->
                        errorMessage?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                // Observe filter mode
                launch {
                    viewModel.currentFilterMode.collectLatest { mode ->
                        updateFilterButtonsUI(mode)
                    }
                }
            }
        }
    }

    private fun showEmptyState() {
        Toast.makeText(requireContext(), "No se encontraron calculadoras. Verificando base de datos...", Toast.LENGTH_LONG).show()
    }

    private fun hideEmptyState() {
        // Hide empty state view if you add one
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

