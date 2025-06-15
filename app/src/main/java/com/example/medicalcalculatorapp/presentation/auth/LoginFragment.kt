package com.example.medicalcalculatorapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.FragmentLoginBinding
import com.example.medicalcalculatorapp.util.SecureStorageManager
import com.example.medicalcalculatorapp.util.ValidationUtils
import com.example.medicalcalculatorapp.data.user.UserManager
import androidx.appcompat.app.AlertDialog
import com.example.medicalcalculatorapp.data.auth.FirebaseAuthService
import com.example.medicalcalculatorapp.data.auth.AuthResult
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.medicalcalculatorapp.domain.service.ComplianceManagerService
import com.example.medicalcalculatorapp.di.AppDependencies
// ✅ FIXED: Correct import for DisclaimerFlow from domain model
import com.example.medicalcalculatorapp.domain.model.DisclaimerFlow

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var secureStorageManager: SecureStorageManager
    private lateinit var userManager: UserManager
    private lateinit var complianceManagerService: ComplianceManagerService
    private lateinit var firebaseAuthService: FirebaseAuthService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureStorageManager = SecureStorageManager(requireContext())
        userManager = UserManager(requireContext())
        // Create all required dependencies first
        val secureStorageManager = SecureStorageManager(requireContext())
        val userManager = UserManager(requireContext())
        val userComplianceRepository = AppDependencies.provideUserComplianceRepository(requireContext())

        // Now create the service with all required parameters
        complianceManagerService = ComplianceManagerService(
            secureStorageManager,
            userManager,
            userComplianceRepository
        )

        firebaseAuthService = FirebaseAuthService()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load any saved credentials (existing functionality)
        loadSavedCredentials()

        // Setup click listeners with enhanced compliance
        setupClickListeners()

        // Show compliance information if helpful
        showComplianceStatusIfNeeded()
    }

    private fun setupClickListeners() {
        // ✅ EXISTING: Regular login button (unchanged)
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                performAuthenticatedLogin()
            }
        }

        // 🆕 ENHANCED: Guest login with progressive compliance
        binding.btnContinueAsGuest.setOnClickListener {
            handleGuestLoginWithCompliance()
        }

        // ✅ EXISTING: Privacy policy (enhanced to show appropriate version)
        binding.tvPrivacyPolicy.setOnClickListener {
            showAppropriateTermsDialog()
        }

        // ✅ EXISTING: Register link (unchanged)
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // ✅ EXISTING: Forgot password (unchanged)
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(context, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Enhanced guest login with streamlined compliance checking
     */
    private fun handleGuestLoginWithCompliance() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val complianceStatus = complianceManagerService.getComplianceStatus()
                val requiredFlow = complianceManagerService.getRequiredDisclaimerFlow()

                when (requiredFlow) {
                    DisclaimerFlow.BASIC_INTRODUCTION -> {
                        // Skip basic disclaimer - go directly to enhanced
                        showEnhancedMedicalDisclaimer()
                    }

                    DisclaimerFlow.ENHANCED_MEDICAL_REQUIRED,
                    DisclaimerFlow.PROFESSIONAL_VERIFICATION_REQUIRED -> {
                        // Show enhanced disclaimer directly
                        showEnhancedMedicalDisclaimer()
                    }

                    DisclaimerFlow.COMPLIANCE_UPDATE_REQUIRED -> {
                        // Show compliance update
                        showComplianceUpdateDialog()
                    }

                    DisclaimerFlow.FULLY_COMPLIANT -> {
                        // User is already compliant - direct guest access
                        startGuestSessionDirectly()
                    }
                }
            } catch (e: Exception) {
                println("❌ Error in guest compliance check: ${e.message}")
                // Fallback to showing enhanced disclaimer
                showEnhancedMedicalDisclaimer()
            }
        }
    }

    /**
     * Show enhanced medical disclaimer for professional users
     */
    private fun showEnhancedMedicalDisclaimer() {
        try {
            val enhancedDialog = EnhancedMedicalDisclaimerDialogFragment.newInstance()

            enhancedDialog.setOnAcceptedListener {
                // User accepted - record compliance using new service
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val success = complianceManagerService.recordCompleteCompliance(
                            professionalType = com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.DOCTOR,
                            licenseInfo = null,
                            method = com.example.medicalcalculatorapp.domain.service.SimpleConsentMethod.APP_DIALOG
                        )

                        if (success) {
                            startGuestSessionDirectly()
                        } else {
                            Toast.makeText(requireContext(), "Error saving compliance", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        println("❌ Error recording guest compliance: ${e.message}")
                        Toast.makeText(requireContext(), "Error saving compliance: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }

            enhancedDialog.setOnRejectedListener {
                showRejectionOptions()
            }

            enhancedDialog.show(parentFragmentManager, EnhancedMedicalDisclaimerDialogFragment.TAG)

        } catch (e: Exception) {
            println("❌ Error showing enhanced disclaimer: ${e.message}")
            Toast.makeText(
                requireContext(),
                getString(R.string.guest_session_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Show compliance update dialog
     */
    private fun showComplianceUpdateDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("📋 Actualización de Políticas")
            .setMessage(
                "Hemos actualizado nuestras políticas médicas para cumplir con los nuevos requisitos de Google Play Store 2024.\n\n" +
                        "Por favor, revise y acepte los términos actualizados."
            )
            .setPositiveButton("Revisar Términos") { _, _ ->
                showEnhancedMedicalDisclaimer()
            }
            .setNegativeButton("Más Tarde") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Start guest session directly (existing logic enhanced)
     */
    private fun startGuestSessionDirectly() {
        try {
            showLoading(true)

            // Start guest session (existing logic)
            val guestId = userManager.startGuestSession()

            // Save compliance status (enhanced)
            secureStorageManager.saveGuestSessionStart(System.currentTimeMillis())
            secureStorageManager.incrementGuestModeUsage()

            showLoading(false)

            // Show welcome message
            Toast.makeText(
                requireContext(),
                getString(R.string.guest_session_started),
                Toast.LENGTH_SHORT
            ).show()

            // Navigate to calculator list
            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)

        } catch (e: Exception) {
            showLoading(false)
            Toast.makeText(
                requireContext(),
                getString(R.string.guest_session_error),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    /**
     * Show appropriate terms dialog based on compliance status
     */
    private fun showAppropriateTermsDialog() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val complianceStatus = complianceManagerService.getComplianceStatus()

                if (complianceStatus.hasPrivacyPolicy || complianceStatus.isProfessionalVerified) {
                    // Show enhanced terms for professionals
                    val fullTermsDialog = FullTermsDialogFragment.newInstance()
                    fullTermsDialog.show(parentFragmentManager, FullTermsDialogFragment.TAG)
                } else {
                    // Show basic terms for general users
                    val basicDialog = PrivacyAndDisclaimerDialogFragment.newInstance()
                    basicDialog.setOnAcceptedListener {
                        // Optional: Mark basic terms viewed
                    }
                    basicDialog.show(parentFragmentManager, PrivacyAndDisclaimerDialogFragment.TAG)
                }
            } catch (e: Exception) {
                println("❌ Error showing terms dialog: ${e.message}")
                // Fallback to basic dialog
                val basicDialog = PrivacyAndDisclaimerDialogFragment.newInstance()
                basicDialog.show(parentFragmentManager, PrivacyAndDisclaimerDialogFragment.TAG)
            }
        }
    }

    /**
     * Show rejection options when user declines enhanced disclaimer
     */
    private fun showRejectionOptions() {
        AlertDialog.Builder(requireContext())
            .setTitle("Opciones Disponibles")
            .setMessage(
                "Entendemos que las políticas médicas pueden ser extensas. Tienes estas opciones:\n\n" +
                        "• Crear una cuenta para acceso completo\n" +
                        "• Revisar los términos nuevamente\n" +
                        "• Contactar soporte para preguntas"
            )
            .setPositiveButton("Crear Cuenta") { _, _ ->
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            }
            .setNeutralButton("Revisar Términos") { _, _ ->
                showEnhancedMedicalDisclaimer()
            }
            .setNegativeButton("Contactar Soporte") { _, _ ->
                // TODO: Implement support contact
                Toast.makeText(requireContext(), "Contacto: legal@medicalcalculatorapp.com", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    /**
     * Show compliance status if it helps user understand requirements
     */
    private fun showComplianceStatusIfNeeded() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val complianceStatus = complianceManagerService.getComplianceStatus()

                // Only show for users who have partial compliance (debugging/transparency)
                if (complianceStatus.hasBasicTerms && !complianceStatus.hasMedicalDisclaimer) {
                    // Could show a small info badge about compliance status
                    // For now, just log it for debugging
                    println("ℹ️ User has partial compliance: ${complianceStatus.getStatusSummary()}")
                }
            } catch (e: Exception) {
                // Silent fail for status check
                println("❌ Error checking compliance status: ${e.message}")
            }
        }
    }

    // ==== AUTHENTICATED USER LOGIN FLOW ====

    private fun performAuthenticatedLogin() {
        println("🔍 DEBUG: performAuthenticatedLogin() called")
        showLoading(true)
        binding.btnLogin.isEnabled = false
        binding.btnContinueAsGuest.isEnabled = false

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        println("🔍 DEBUG: About to call Firebase signInWithEmailAndPassword")

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = firebaseAuthService.signInWithEmailAndPassword(email, password)

                println("🔍 DEBUG: Firebase result: $result")

                when (result) {
                    is AuthResult.Success -> {
                        val firebaseUser = result.user
                        if (firebaseUser != null) {
                            // Save credentials if "Remember me" is checked
                            saveCredentialsIfNeeded()

                            // Check compliance status for this authenticated user
                            checkAuthenticatedUserCompliance(firebaseUser.uid)
                        } else {
                            handleAuthenticationError("Login failed - no user data")
                        }
                    }
                    is AuthResult.Error -> {
                        handleAuthenticationError(result.message)
                    }
                }
            } catch (e: Exception) {
                println("❌ DEBUG: Exception in performAuthenticatedLogin: ${e.message}")
                e.printStackTrace()
                handleAuthenticationError("Unexpected error: ${e.message}")
            }
        }
    }

    /**
     * Check compliance status for authenticated user and handle accordingly
     */
    private fun checkAuthenticatedUserCompliance(authenticatedUserId: String) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                println("🔍 DEBUG: Checking compliance for authenticated user: $authenticatedUserId")

                // ✅ SIMPLIFIED: Skip database check for now - assume first time user
                val hasComplianceRecord = false // Always treat as first time user for now

                println("🔍 DEBUG: User has compliance record: $hasComplianceRecord")

                if (!hasComplianceRecord) {
                    // First time login - show enhanced medical disclaimer
                    showFirstTimeUserCompliance(authenticatedUserId)
                } else {
                    // Check if compliance is complete and up-to-date
                    val complianceStatus = complianceManagerService.getComplianceStatus()
                    val requiredFlow = complianceManagerService.getRequiredDisclaimerFlow()

                    println("🔍 DEBUG: Compliance status: ${complianceStatus.getStatusSummary()}")
                    println("🔍 DEBUG: Required flow: $requiredFlow")

                    when (requiredFlow) {
                        DisclaimerFlow.FULLY_COMPLIANT -> {
                            // User is fully compliant - proceed to app
                            proceedToCalculatorList("Authenticated user fully compliant")
                        }
                        DisclaimerFlow.ENHANCED_MEDICAL_REQUIRED -> {
                            // User needs enhanced disclaimer
                            showEnhancedDisclaimerForAuthenticatedUser(authenticatedUserId)
                        }
                        DisclaimerFlow.COMPLIANCE_UPDATE_REQUIRED -> {
                            // User needs compliance update
                            showComplianceUpdateForAuthenticatedUser(authenticatedUserId)
                        }
                        else -> {
                            // Default to enhanced disclaimer
                            showEnhancedDisclaimerForAuthenticatedUser(authenticatedUserId)
                        }
                    }
                }
            } catch (e: Exception) {
                println("❌ DEBUG: Error checking compliance: ${e.message}")
                e.printStackTrace()
                handleAuthenticationError("Error checking compliance: ${e.message}")
            }
        }
    }

    /**
     * Show compliance flow for first-time authenticated users
     */
    private fun showFirstTimeUserCompliance(authenticatedUserId: String) {
        println("🔍 DEBUG: Showing first-time user compliance for: $authenticatedUserId")

        AlertDialog.Builder(requireContext())
            .setTitle("Bienvenido a MediCálculos")
            .setMessage(
                "Como usuario registrado, necesitas completar la verificación médica profesional.\n\n" +
                        "Este proceso solo se realiza una vez y tus preferencias se guardarán de forma segura."
            )
            .setPositiveButton("Continuar") { _, _ ->
                showEnhancedDisclaimerForAuthenticatedUser(authenticatedUserId)
            }
            .setNegativeButton("Más Tarde") { _, _ ->
                // User can postpone, but sign them out
                firebaseAuthService.signOut()
                userManager.signOut()
                showLoading(false)
                binding.btnLogin.isEnabled = true
                binding.btnContinueAsGuest.isEnabled = true
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show enhanced disclaimer specifically for authenticated users
     */
    private fun showEnhancedDisclaimerForAuthenticatedUser(authenticatedUserId: String) {
        try {
            val enhancedDialog = EnhancedMedicalDisclaimerDialogFragment.newInstance()

            enhancedDialog.setOnAcceptedListener {
                // User accepted - record compliance in database
                viewLifecycleOwner.lifecycleScope.launch {
                    try {
                        val success = complianceManagerService.recordCompleteCompliance(
                            professionalType = com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.DOCTOR, // Default, could be made selectable
                            licenseInfo = null,
                            method = com.example.medicalcalculatorapp.domain.service.SimpleConsentMethod.APP_DIALOG
                        )

                        if (success) {
                            println("✅ DEBUG: Compliance recorded successfully for authenticated user")
                            proceedToCalculatorList("Authenticated user compliance completed")
                        } else {
                            handleAuthenticationError("Failed to save compliance data")
                        }
                    } catch (e: Exception) {
                        println("❌ DEBUG: Error recording compliance: ${e.message}")
                        handleAuthenticationError("Error saving compliance: ${e.message}")
                    }
                }
            }

            enhancedDialog.setOnRejectedListener {
                // User rejected - sign them out
                firebaseAuthService.signOut()
                userManager.signOut()
                showLoading(false)
                binding.btnLogin.isEnabled = true
                binding.btnContinueAsGuest.isEnabled = true

                Toast.makeText(
                    requireContext(),
                    "La aceptación de los términos médicos es requerida para usar la aplicación",
                    Toast.LENGTH_LONG
                ).show()
            }

            enhancedDialog.show(parentFragmentManager, EnhancedMedicalDisclaimerDialogFragment.TAG)

        } catch (e: Exception) {
            println("❌ ERROR: Error showing enhanced disclaimer: ${e.message}")
            handleAuthenticationError("Error showing compliance dialog")
        }
    }

    /**
     * Show compliance update dialog for existing users
     */
    private fun showComplianceUpdateForAuthenticatedUser(authenticatedUserId: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("📋 Actualización de Políticas")
            .setMessage(
                "Hemos actualizado nuestras políticas médicas para cumplir con los nuevos requisitos.\n\n" +
                        "Por favor, revisa y acepta los términos actualizados para continuar."
            )
            .setPositiveButton("Revisar Términos") { _, _ ->
                showEnhancedDisclaimerForAuthenticatedUser(authenticatedUserId)
            }
            .setNegativeButton("Más Tarde") { _, _ ->
                // Allow postponing for existing users
                proceedToCalculatorList("Authenticated user postponed compliance update")
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Proceed to calculator list after successful authentication and compliance
     */
    private fun proceedToCalculatorList(reason: String) {
        println("🚀 DEBUG: Proceeding to calculator list: $reason")

        showLoading(false)
        binding.btnLogin.isEnabled = true
        binding.btnContinueAsGuest.isEnabled = true

        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()

        try {
            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
        } catch (e: Exception) {
            println("❌ DEBUG: Navigation error: ${e.message}")
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // ==== UTILITY METHODS ====

    private fun handleAuthenticationError(errorMessage: String) {
        showLoading(false)
        binding.btnLogin.isEnabled = true
        binding.btnContinueAsGuest.isEnabled = true

        println("❌ DEBUG: Authentication error: $errorMessage")

        // Show user-friendly error message
        val userFriendlyMessage = when {
            errorMessage.contains("password", ignoreCase = true) -> "Invalid password. Please try again."
            errorMessage.contains("email", ignoreCase = true) -> "Invalid email address."
            errorMessage.contains("user", ignoreCase = true) -> "No account found with this email."
            errorMessage.contains("network", ignoreCase = true) -> "Network error. Check your connection."
            else -> "Login failed. Please try again."
        }

        Toast.makeText(requireContext(), userFriendlyMessage, Toast.LENGTH_LONG).show()
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Email validation (existing logic)
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.email_required)
            isValid = false
        } else if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            isValid = false
        } else if (ValidationUtils.containsSuspiciousPatterns(email)) {
            binding.tilEmail.error = getString(R.string.invalid_input)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Password validation - simplified for login (existing logic)
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.password_required)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun loadSavedCredentials() {
        val rememberCredentials = secureStorageManager.getRememberMeFlag()
        if (rememberCredentials) {
            val savedEmail = secureStorageManager.getEmail()
            savedEmail?.let {
                binding.etEmail.setText(it)
                binding.cbRememberMe.isChecked = true
            }
        }
    }

    private fun saveCredentialsIfNeeded() {
        val email = binding.etEmail.text.toString().trim()
        val isChecked = binding.cbRememberMe.isChecked

        secureStorageManager.saveRememberMeFlag(isChecked)
        if (isChecked) {
            secureStorageManager.saveEmail(email)
        } else {
            secureStorageManager.clearCredentials()
        }
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE

        // Disable all interactive elements during loading
        binding.btnLogin.isEnabled = !show
        binding.btnContinueAsGuest.isEnabled = !show
        binding.etEmail.isEnabled = !show
        binding.etPassword.isEnabled = !show
        binding.cbRememberMe.isEnabled = !show

        // Optional: Visual feedback for disabled state
        val alpha = if (show) 0.5f else 1.0f
        binding.loginFormContainer?.alpha = alpha
        binding.guestOptionCard?.alpha = alpha
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}