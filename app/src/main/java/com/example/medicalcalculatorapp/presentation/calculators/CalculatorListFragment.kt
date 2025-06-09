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
import com.example.medicalcalculatorapp.data.user.UserManager
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.navigation.fragment.findNavController

class CalculatorListFragment : Fragment() {

    private var _binding: FragmentCalculatorListBinding? = null
    private val binding get() = _binding!!

    private lateinit var calculatorAdapter: CalculatorAdapter
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var secureStorageManager: SecureStorageManager
    private lateinit var userManager: UserManager

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
        userManager = AppDependencies.provideUserManager(requireContext())
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
        if (!secureStorageManager.hasAcceptedDisclaimer()) {
            showPrivacyAndDisclaimerDialog()
        }

        setupRecyclerView()
        setupCategoryRecyclerView()
        setupFilterButtons()
        setupSearchView()
        setupGuestStatusBar() // NEW: Setup guest status management
        observeViewModel()

        // Show loading initially
        showLoading(true)
    }

    // NEW: Guest Status Bar Management
    private fun setupGuestStatusBar() {
        // Check if user is in guest mode and show status bar accordingly
        updateGuestStatusVisibility()

        // Setup guest status bar click listeners
        binding.guestStatusBar.btnCreateAccount.setOnClickListener {
            navigateToAccountCreation()
        }

        binding.guestStatusBar.btnContinueGuest.setOnClickListener {
            dismissGuestStatusBar()
        }

        binding.guestStatusBar.btnDismissGuestStatus.setOnClickListener {
            dismissGuestStatusBar()
        }
    }

    private fun updateGuestStatusVisibility() {
        if (userManager.isGuestMode()) {
            showGuestStatusBar()
        } else {
            hideGuestStatusBar()
        }
    }

    private fun showGuestStatusBar() {
        binding.guestStatusBar.guestStatusCard.visibility = View.VISIBLE

        // Update guest status information
        updateGuestSessionInfo()

        println("🔍 DEBUG: Showing guest status bar for guest user")
    }

    private fun hideGuestStatusBar() {
        binding.guestStatusBar.guestStatusCard.visibility = View.GONE
        println("🔍 DEBUG: Hiding guest status bar for authenticated user")
    }

    private fun updateGuestSessionInfo() {
        try {
            // Update title and subtitle for guest status
            binding.guestStatusBar.tvGuestStatusTitle.text = getString(R.string.guest_session_welcome)
            binding.guestStatusBar.tvGuestStatusSubtitle.text = getString(R.string.guest_session_reminder)

            // Optional: Show session stats if needed
            val calculationCount = secureStorageManager.getGuestCalculationCount()
            val sessionDuration = userManager.getGuestSessionDuration()

            if (calculationCount > 0 || sessionDuration > 0) {
                binding.guestStatusBar.layoutGuestStats.visibility = View.VISIBLE

                // Show calculation count
                binding.guestStatusBar.tvCalculationCount.text =
                    getString(R.string.guest_calculations_count, calculationCount)

                // Show session time (convert milliseconds to minutes)
                val sessionMinutes = sessionDuration / (60 * 1000)
                binding.guestStatusBar.tvSessionTime.text =
                    getString(R.string.guest_session_time, sessionMinutes)
            } else {
                binding.guestStatusBar.layoutGuestStats.visibility = View.GONE
            }

        } catch (e: Exception) {
            println("❌ Error updating guest session info: ${e.message}")
            // Don't crash the app, just log the error
        }
    }

    private fun dismissGuestStatusBar() {
        // Hide the guest status bar for this session
        hideGuestStatusBar()

        // Show a brief message
        Toast.makeText(
            requireContext(),
            getString(R.string.guest_status_dismissed),
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun navigateToAccountCreation() {
        try {
            // Show upgrade prompt first
            showAccountUpgradeDialog()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error navegando a creación de cuenta: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showAccountUpgradeDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Crear Cuenta")
            .setMessage("¿Deseas crear una cuenta para guardar tu progreso y acceder a funciones adicionales?")
            .setPositiveButton("Crear Cuenta") { _, _ ->
                // Navigate to registration/login screen
                try {
                    findNavController().navigate(
                        R.id.action_calculatorListFragment_to_loginFragment
                    )
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Error de navegación: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Continuar como Invitado") { dialog, _ ->
                dialog.dismiss()
                dismissGuestStatusBar()
            }
            .show()
    }

    // EXISTING METHODS (unchanged)
    private fun showPrivacyAndDisclaimerDialog() {
        val dialog = PrivacyAndDisclaimerDialogFragment.newInstance()
        dialog.setOnAcceptedListener {
            Toast.makeText(requireContext(), "Políticas aceptadas. Bienvenido a MediCálculos", Toast.LENGTH_SHORT).show()
        }

        try {
            dialog.show(parentFragmentManager, PrivacyAndDisclaimerDialogFragment.TAG)
        } catch (e: Exception) {
            println("❌ Error showing privacy dialog: ${e.message}")
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
                handleFavoriteClick(calculator.id)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = calculatorAdapter
            setPadding(0, 0, 0, 100)
            clipToPadding = false
        }
    }

    private fun handleFavoriteClick(calculatorId: String) {
        if (userManager.isGuestMode()) {
            // Show guest limitation message
            showGuestLimitationDialog("favoritos")
        } else {
            // Allow favorite toggle for authenticated users
            viewModel.toggleFavorite(calculatorId)
        }
    }

    private fun showGuestLimitationDialog(feature: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Función Limitada")
            .setMessage("Los usuarios invitados no pueden guardar $feature. ¿Deseas crear una cuenta para acceder a esta función?")
            .setPositiveButton("Crear Cuenta") { _, _ ->
                navigateToAccountCreation()
            }
            .setNegativeButton("Continuar como Invitado") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun setupCategoryRecyclerView() {
        println("🔍 DEBUG: Setting up category RecyclerView")

        categoryAdapter = CategoryAdapter { selectedCategory ->
            println("🔍 DEBUG: Category selected: ${selectedCategory?.category?.name}")
            viewModel.selectCategory(selectedCategory?.category?.id)
        }

        binding.recyclerViewCategories.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = categoryAdapter
            visibility = View.VISIBLE
            println("🔍 DEBUG: Category RecyclerView setup complete, visibility: $visibility")
        }
    }

    private fun setupFilterButtons() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnHome -> {
                    true
                }
                R.id.btnSettings -> {
                    try {
                        findNavController().navigate(R.id.action_calculatorListFragment_to_settingsFragment)
                    } catch (e: Exception) {
                        Toast.makeText(requireContext(), "Error navegando a configuración: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                    true
                }
                else -> false
            }
        }

        binding.btnAll.setOnClickListener {
            viewModel.setFilterMode(CalculatorListViewModel.FilterMode.ALL)
        }

        binding.btnFavorites.setOnClickListener {
            if (userManager.isGuestMode()) {
                showGuestLimitationDialog("favoritos")
            } else {
                viewModel.setFilterMode(CalculatorListViewModel.FilterMode.FAVORITES)
            }
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

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
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

                launch {
                    viewModel.categories.collectLatest { categories ->
                        println("🔍 DEBUG Fragment: Received ${categories.size} categories for adapter")
                        categoryAdapter.submitList(categories)
                    }
                }

                launch {
                    viewModel.selectedCategoryId.collectLatest { selectedId ->
                        println("🔍 DEBUG Fragment: Selected category ID: $selectedId")
                        categoryAdapter.setSelectedCategory(selectedId)
                    }
                }

                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        showLoading(isLoading)
                    }
                }

                launch {
                    viewModel.error.collectLatest { errorMessage ->
                        errorMessage?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                        }
                    }
                }

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

    override fun onResume() {
        super.onResume()
        // Update guest status when fragment resumes
        updateGuestStatusVisibility()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//package com.example.medicalcalculatorapp.presentation.calculators
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.lifecycle.Lifecycle
//import androidx.lifecycle.lifecycleScope
//import androidx.lifecycle.repeatOnLifecycle
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.medicalcalculatorapp.R
//import com.example.medicalcalculatorapp.databinding.FragmentCalculatorListBinding
//import com.example.medicalcalculatorapp.di.AppDependencies
//import com.example.medicalcalculatorapp.presentation.auth.PrivacyAndDisclaimerDialogFragment
//import com.example.medicalcalculatorapp.util.SecureStorageManager
//import kotlinx.coroutines.flow.collectLatest
//import kotlinx.coroutines.launch
//import androidx.navigation.fragment.findNavController
//
//class CalculatorListFragment : Fragment() {
//
//    private var _binding: FragmentCalculatorListBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var calculatorAdapter: CalculatorAdapter
//    private lateinit var categoryAdapter: CategoryAdapter
//    private lateinit var secureStorageManager: SecureStorageManager
//
//    // Create ViewModel using our Factory
//    private val viewModel: CalculatorListViewModel by viewModels {
//        CalculatorListViewModel.Factory(
//            AppDependencies.provideCalculatorRepository(requireContext()),
//            AppDependencies.provideUserManager(requireContext()),
//            AppDependencies.provideCategoryRepository(requireContext())
//        )
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        secureStorageManager = SecureStorageManager(requireContext())
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentCalculatorListBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Check if user has accepted privacy policy and disclaimer
//        if (!secureStorageManager.isDisclaimerAccepted()) {
//            showPrivacyAndDisclaimerDialog()
//        }
//
//        setupRecyclerView()
//        setupCategoryRecyclerView()
//        setupFilterButtons()
//        setupSearchView()
//        observeViewModel()
//
//        // Show loading initially
//        showLoading(true)
//    }
//
//    private fun showPrivacyAndDisclaimerDialog() {
//        val dialog = PrivacyAndDisclaimerDialogFragment.newInstance()
//        dialog.setOnAcceptedListener {
//            // User accepted both privacy policy and disclaimer
//            Toast.makeText(requireContext(), "Políticas aceptadas. Bienvenido a MediCálculos", Toast.LENGTH_SHORT).show()
//        }
//
//        try {
//            dialog.show(parentFragmentManager, PrivacyAndDisclaimerDialogFragment.TAG)
//        } catch (e: Exception) {
//            // If there's an issue with the dialog, log it but don't crash
//            println("❌ Error showing privacy dialog: ${e.message}")
//            // For development, you might want to just mark as accepted
//            // secureStorageManager.saveDisclaimerAccepted(true)
//        }
//    }
//
//    private fun setupRecyclerView() {
//        calculatorAdapter = CalculatorAdapter(
//            onItemClick = { calculator ->
//                try {
//                    val bundle = Bundle().apply {
//                        putString("calculatorId", calculator.id)
//                    }
//                    findNavController().navigate(
//                        R.id.action_calculatorListFragment_to_calculatorDetailFragment,
//                        bundle
//                    )
//                } catch (e: Exception) {
//                    Toast.makeText(requireContext(), "Error opening calculator: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//            },
//            onFavoriteClick = { calculator ->
//                viewModel.toggleFavorite(calculator.id)
//            }
//        )
//
//        binding.recyclerView.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = calculatorAdapter
//            setPadding(0, 0, 0, 100)
//            clipToPadding = false
//        }
//    }
//
//    private fun setupCategoryRecyclerView() {
//        println("🔍 DEBUG: Setting up category RecyclerView")
//
//        categoryAdapter = CategoryAdapter { selectedCategory ->
//            println("🔍 DEBUG: Category selected: ${selectedCategory?.category?.name}")
//            viewModel.selectCategory(selectedCategory?.category?.id)
//        }
//
//        binding.recyclerViewCategories.apply {
//            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
//            adapter = categoryAdapter
//            visibility = View.VISIBLE // Force visibility
//            println("🔍 DEBUG: Category RecyclerView setup complete, visibility: $visibility")
//        }
//    }
//
//    private fun setupFilterButtons() {
//        // ✅ FIXED: Proper navigation to settings
//        binding.bottomNavigation.setOnItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.btnHome -> {
//                    // Already on home, do nothing
//                    true
//                }
//                R.id.btnSettings -> {
//                    // ✅ Navigate to settings instead of showing toast
//                    try {
//                        findNavController().navigate(R.id.action_calculatorListFragment_to_settingsFragment)
//                    } catch (e: Exception) {
//                        Toast.makeText(requireContext(), "Error navegando a configuración: ${e.message}", Toast.LENGTH_SHORT).show()
//                    }
//                    true
//                }
//                else -> false
//            }
//        }
//
//        // Filter buttons (Todo A-Z and Favoritos)
//        binding.btnAll.setOnClickListener {
//            viewModel.setFilterMode(CalculatorListViewModel.FilterMode.ALL)
//        }
//
//        binding.btnFavorites.setOnClickListener {
//            viewModel.setFilterMode(CalculatorListViewModel.FilterMode.FAVORITES)
//        }
//    }
//
//    private fun setupSearchView() {
//        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                viewModel.searchCalculators(query)
//                return true
//            }
//
//            override fun onQueryTextChange(newText: String?): Boolean {
//                if (newText.isNullOrBlank()) {
//                    viewModel.searchCalculators(null)
//                }
//                return true
//            }
//        })
//    }
//
//    private fun updateFilterButtonsUI(filterMode: CalculatorListViewModel.FilterMode) {
//        binding.btnAll.alpha = if (filterMode == CalculatorListViewModel.FilterMode.ALL) 1.0f else 0.6f
//        binding.btnFavorites.alpha = if (filterMode == CalculatorListViewModel.FilterMode.FAVORITES) 1.0f else 0.6f
//    }
//
//    private fun showLoading(show: Boolean) {
//        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
//        binding.recyclerView.visibility = if (show) View.GONE else View.VISIBLE
//    }
//
//    private fun observeViewModel() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                // Observe calculators
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
//                // Observe categories
//                launch {
//                    viewModel.categories.collectLatest { categories ->
//                        println("🔍 DEBUG Fragment: Received ${categories.size} categories for adapter")
//                        categoryAdapter.submitList(categories)
//                    }
//                }
//
//                // Observe selected category
//                launch {
//                    viewModel.selectedCategoryId.collectLatest { selectedId ->
//                        println("🔍 DEBUG Fragment: Selected category ID: $selectedId")
//                        categoryAdapter.setSelectedCategory(selectedId)
//                    }
//                }
//
//                // Observe loading state
//                launch {
//                    viewModel.isLoading.collectLatest { isLoading ->
//                        showLoading(isLoading)
//                    }
//                }
//
//                // Observe errors
//                launch {
//                    viewModel.error.collectLatest { errorMessage ->
//                        errorMessage?.let {
//                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
//                        }
//                    }
//                }
//
//                // Observe filter mode
//                launch {
//                    viewModel.currentFilterMode.collectLatest { mode ->
//                        updateFilterButtonsUI(mode)
//                    }
//                }
//            }
//        }
//    }
//
//    private fun showEmptyState() {
//        Toast.makeText(requireContext(), "No se encontraron calculadoras. Verificando base de datos...", Toast.LENGTH_LONG).show()
//    }
//
//    private fun hideEmptyState() {
//        // Hide empty state view if you add one
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}