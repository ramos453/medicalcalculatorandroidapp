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
import com.example.medicalcalculatorapp.util.MedicalAccessController
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
    private lateinit var medicalAccessController: MedicalAccessController
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
        medicalAccessController = MedicalAccessController(requireContext(), userManager)
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

        // ✅ NEW: Setup guest status bar for guest users
        setupGuestStatusBar()

        setupRecyclerView()
        setupCategoryRecyclerView()
        setupFilterButtons()
        setupSearchView()
        observeViewModel()

        // Show loading initially
        showLoading(true)
    }

    /**
     * ✅ NEW: Setup guest status bar with upgrade prompts and session info
     */
    private fun setupGuestStatusBar() {
        if (userManager.isGuestMode()) {
            // Include the guest status bar layout
            val guestStatusCard = binding.root.findViewById<View>(R.id.guestStatusCard)
            guestStatusCard?.visibility = View.VISIBLE

            // Setup guest status info
            val sessionDuration = userManager.getGuestSessionDuration() / (60 * 1000) // minutes
            val calculationCount = secureStorageManager.getGuestCalculationCount()

            binding.root.findViewById<android.widget.TextView>(R.id.tvCalculationCount)?.text = "Cálculos: $calculationCount/50"
            binding.root.findViewById<android.widget.TextView>(R.id.tvSessionTime)?.text = "Sesión: ${sessionDuration} min"
            binding.root.findViewById<View>(R.id.layoutGuestStats)?.visibility = View.VISIBLE

            // Setup action buttons
            binding.root.findViewById<View>(R.id.btnCreateAccount)?.setOnClickListener {
                showGuestUpgradeDialog()
            }

            binding.root.findViewById<View>(R.id.btnContinueGuest)?.setOnClickListener {
                guestStatusCard?.visibility = View.GONE
            }

            binding.root.findViewById<View>(R.id.btnDismissGuestStatus)?.setOnClickListener {
                guestStatusCard?.visibility = View.GONE
            }
        } else {
            binding.root.findViewById<View>(R.id.guestStatusCard)?.visibility = View.GONE
        }
    }

    /**
     * ✅ NEW: Show upgrade dialog for guest users
     */
    private fun showGuestUpgradeDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Crear Cuenta Premium")
            .setMessage("""
                🎯 Beneficios de crear una cuenta:
                
                ✅ Historial ilimitado de cálculos
                ✅ Sincronización entre dispositivos  
                ✅ Calculadoras favoritas guardadas
                ✅ Sin límites de sesión
                ✅ Exportar resultados en PDF
                ✅ Acceso prioritario a nuevas funciones
                
                ¿Te gustaría crear una cuenta ahora?
            """.trimIndent())
            .setPositiveButton("Crear Cuenta") { _, _ ->
                try {
                    findNavController().navigate(R.id.action_calculatorListFragment_to_loginFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error de navegación: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Continuar como Invitado") { dialog, _ ->
                dialog.dismiss()
                binding.guestStatusBar.guestStatusCard.visibility = View.GONE
            }
            .show()
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
        }
    }

    private fun setupRecyclerView() {
        calculatorAdapter = CalculatorAdapter(
            onItemClick = { calculator ->
                // ✅ UPDATED: Use MedicalAccessController before opening calculator
                medicalAccessController.checkAccessAndExecute(
                    fragment = this,
                    calculatorName = calculator.name
                ) {
                    // This block executes only if access is granted
                    openCalculator(calculator.id)
                }
            },
            onFavoriteClick = { calculator ->
                // ✅ UPDATED: Check if user can save favorites (guest users cannot)
                if (userManager.canSaveUserData()) {
                    viewModel.toggleFavorite(calculator.id)
                } else {
                    showGuestFeatureLimitationDialog("favoritos")
                }
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = calculatorAdapter
            setPadding(0, 0, 0, 100)
            clipToPadding = false
        }
    }

    /**
     * ✅ NEW: Handle calculator opening with navigation
     */
    private fun openCalculator(calculatorId: String) {
        try {
            // Track guest calculation count
            if (userManager.isGuestMode()) {
                secureStorageManager.incrementGuestCalculationCount()

                // Check if guest has reached calculation limit
                val calculationCount = secureStorageManager.getGuestCalculationCount()
                if (calculationCount >= 45) { // Show warning at 45/50
                    showGuestCalculationLimitWarning(calculationCount)
                }
            }

            val bundle = Bundle().apply {
                putString("calculatorId", calculatorId)
            }
            findNavController().navigate(
                R.id.action_calculatorListFragment_to_calculatorDetailFragment,
                bundle
            )
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error opening calculator: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * ✅ NEW: Show limitation dialog for guest users
     */
    private fun showGuestFeatureLimitationDialog(feature: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Función Premium")
            .setMessage("La función de $feature requiere una cuenta registrada.\n\n¿Te gustaría crear una cuenta gratuita para acceder a esta función?")
            .setPositiveButton("Crear Cuenta") { _, _ ->
                showGuestUpgradeDialog()
            }
            .setNegativeButton("Continuar como Invitado") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * ✅ NEW: Warn guest users about approaching calculation limit
     */
    private fun showGuestCalculationLimitWarning(currentCount: Int) {
        val remaining = 50 - currentCount

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Límite de Cálculos")
            .setMessage("Te quedan $remaining cálculos en esta sesión de invitado.\n\nCrea una cuenta gratuita para obtener cálculos ilimitados.")
            .setPositiveButton("Crear Cuenta") { _, _ ->
                showGuestUpgradeDialog()
            }
            .setNegativeButton("Continuar") { dialog, _ ->
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
        // ✅ UPDATED: Enhanced navigation to settings with guest handling
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.btnHome -> {
                    // Already on home, do nothing
                    true
                }
                R.id.btnSettings -> {
                    // Check if guest user - show limited settings or upgrade prompt
                    if (userManager.isGuestMode()) {
                        showGuestSettingsDialog()
                    } else {
                        try {
                            findNavController().navigate(R.id.action_calculatorListFragment_to_settingsFragment)
                        } catch (e: Exception) {
                            Toast.makeText(requireContext(), "Error navegando a configuración: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
                else -> false
            }
        }

        // Filter buttons (Todo A-Z and Favoritos)
        binding.btnAll.setOnClickListener {
            viewModel.setFilterMode(CalculatorListViewModel.FilterMode.ALL)
        }

        binding.btnFavorites.setOnClickListener {
            // ✅ UPDATED: Check if user can access favorites
            if (userManager.canSaveUserData()) {
                viewModel.setFilterMode(CalculatorListViewModel.FilterMode.FAVORITES)
            } else {
                showGuestFeatureLimitationDialog("favoritos")
            }
        }
    }

    /**
     * ✅ NEW: Show limited settings for guest users
     */
    private fun showGuestSettingsDialog() {
        val options = arrayOf(
            "Crear Cuenta",
            "Política de Privacidad",
            "Términos de Uso",
            "Acerca de la App",
            "Cerrar Sesión de Invitado"
        )

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Configuración de Invitado")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> showGuestUpgradeDialog()
                    1 -> showPrivacyAndDisclaimerDialog()
                    2 -> showPrivacyAndDisclaimerDialog()
                    3 -> showAboutDialog()
                    4 -> showGuestSignOutDialog()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * ✅ NEW: Show about dialog
     */
    private fun showAboutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("MediCálculos")
            .setMessage("""
                Versión: 1.0.0
                
                Calculadoras médicas para profesionales de salud.
                
                ⚠️ Solo para uso educativo y de referencia
                ⚠️ No reemplaza el juicio clínico profesional
                ⚠️ Siempre verificar resultados independientemente
                
                Desarrollado con los más altos estándares de seguridad médica.
            """.trimIndent())
            .setPositiveButton("Entendido", null)
            .show()
    }

    /**
     * ✅ NEW: Handle guest sign out
     */
    private fun showGuestSignOutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cerrar Sesión de Invitado")
            .setMessage("¿Estás seguro de que quieres cerrar la sesión?\n\nSe perderán todos los datos de la sesión actual.")
            .setPositiveButton("Cerrar Sesión") { _, _ ->
                userManager.endGuestSession()
                secureStorageManager.clearGuestSession()

                try {
                    findNavController().navigate(R.id.action_calculatorListFragment_to_loginFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error de navegación: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
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

                // Observe categories
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
        if (userManager.isGuestMode()) {
            Toast.makeText(requireContext(), "No se encontraron calculadoras. Asegúrate de tener conexión a internet.", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "No se encontraron calculadoras. Verificando base de datos...", Toast.LENGTH_LONG).show()
        }
    }

    private fun hideEmptyState() {
        // Hide empty state view if you add one
    }

    override fun onResume() {
        super.onResume()

        // ✅ NEW: Update guest session info when returning to fragment
        if (userManager.isGuestMode()) {
            setupGuestStatusBar()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}