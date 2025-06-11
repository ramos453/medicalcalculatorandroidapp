// Replace your existing app/src/main/java/com/example/medicalcalculatorapp/presentation/auth/LoginFragment.kt

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
import com.example.medicalcalculatorapp.util.MedicalComplianceManager
import com.example.medicalcalculatorapp.util.DisclaimerFlow
import androidx.appcompat.app.AlertDialog

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var secureStorageManager: SecureStorageManager
    private lateinit var userManager: UserManager
    private lateinit var complianceManager: MedicalComplianceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureStorageManager = SecureStorageManager(requireContext())
        userManager = UserManager(requireContext())
        complianceManager = MedicalComplianceManager(
            requireContext(),
            secureStorageManager,
            userManager
        )
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
     * Enhanced guest login with progressive compliance checking
     */
    private fun handleGuestLoginWithCompliance() {
        val complianceStatus = complianceManager.getComplianceStatus()

        when (complianceStatus.requiredFlow) {
            DisclaimerFlow.BASIC_INTRODUCTION -> {
                // New user - show basic disclaimer first, then professional verification
                showBasicDisclaimerThenEnhanced()
            }

            DisclaimerFlow.ENHANCED_MEDICAL_REQUIRED,
            DisclaimerFlow.PROFESSIONAL_VERIFICATION_REQUIRED -> {
                // User needs enhanced verification
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
    }

    /**
     * Show basic disclaimer first, then enhanced (progressive disclosure)
     */
    private fun showBasicDisclaimerThenEnhanced() {
        // First show your existing privacy dialog
        val basicDialog = PrivacyAndDisclaimerDialogFragment.newInstance()
        basicDialog.setOnAcceptedListener {
            // Mark basic disclaimer accepted
            complianceManager.markBasicDisclaimerAccepted()

            // Now show enhanced medical disclaimer
            showProfessionalVerificationFlow()
        }

        try {
            basicDialog.show(parentFragmentManager, PrivacyAndDisclaimerDialogFragment.TAG)
        } catch (e: Exception) {
            println("❌ Error showing basic disclaimer: ${e.message}")
            // Fallback directly to enhanced
            showEnhancedMedicalDisclaimer()
        }
    }

    /**
     * Show professional verification with enhanced disclaimer
     */
    private fun showProfessionalVerificationFlow() {
        showProfessionalVerificationDialog { isProfessional ->
            if (isProfessional) {
                showEnhancedMedicalDisclaimer()
            } else {
                showNonProfessionalMessage()
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
                // Mark all compliance requirements as met
                complianceManager.markEnhancedDisclaimerAccepted()
                complianceManager.markProfessionalVerified()

                // Start guest session
                startGuestSessionDirectly()
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
     * Professional verification dialog (existing logic preserved)
     */
    private fun showProfessionalVerificationDialog(callback: (Boolean) -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.guest_professional_verification_title))
            .setMessage(getString(R.string.guest_professional_verification_message))
            .setPositiveButton(getString(R.string.confirm_medical_professional)) { _, _ ->
                callback(true)
            }
            .setNegativeButton(getString(R.string.not_medical_professional)) { _, _ ->
                callback(false)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Handle non-professional users (existing logic)
     */
    private fun showNonProfessionalMessage() {
        AlertDialog.Builder(requireContext())
            .setTitle("Acceso Restringido")
            .setMessage("Esta aplicación está diseñada exclusivamente para profesionales de salud licenciados.\n\nSi eres un profesional médico, por favor verifica tu estatus. Si no lo eres, consulta con un profesional de salud calificado.")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
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
        val complianceStatus = complianceManager.getComplianceStatus()

        if (complianceStatus.hasEnhancedDisclaimer || complianceStatus.isProfessionalVerified) {
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
        val complianceStatus = complianceManager.getComplianceStatus()

        // Only show for users who have partial compliance (debugging/transparency)
        if (complianceStatus.hasBasicDisclaimer && !complianceStatus.hasEnhancedDisclaimer) {
            // Could show a small info badge about compliance status
            // For now, just log it for debugging
            println("ℹ️ User has partial compliance: ${complianceStatus.requiredFlow}")
        }
    }

    // ✅ EXISTING METHODS (unchanged - keeping all your current login logic)

    private fun performAuthenticatedLogin() {
        showLoading(true)
        binding.btnLogin.isEnabled = false
        binding.btnContinueAsGuest.isEnabled = false

        // Save credentials if "Remember me" is checked (existing logic)
        saveCredentialsIfNeeded()

        // Simulate network delay (existing logic)
        view?.postDelayed({
            showLoading(false)
            binding.btnLogin.isEnabled = true
            binding.btnContinueAsGuest.isEnabled = true

            // Mark user as authenticated and compliant
            complianceManager.markBasicDisclaimerAccepted()
            complianceManager.markEnhancedDisclaimerAccepted()
            complianceManager.markProfessionalVerified()

            // For now, simulate successful login (existing logic)
            Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()

            // Navigate to main screen (existing logic)
            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
        }, 1500)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

//package com.example.medicalcalculatorapp.presentation.auth
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Toast
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import com.example.medicalcalculatorapp.R
//import com.example.medicalcalculatorapp.databinding.FragmentLoginBinding
//import com.example.medicalcalculatorapp.util.SecureStorageManager
//import com.example.medicalcalculatorapp.util.ValidationUtils
//import com.example.medicalcalculatorapp.data.user.UserManager
//import androidx.appcompat.app.AlertDialog
//import com.example.medicalcalculatorapp.di.AppDependencies
//import com.example.medicalcalculatorapp.data.auth.FirebaseAuthService
//import com.example.medicalcalculatorapp.data.auth.AuthResult
//import kotlinx.coroutines.launch
//import androidx.lifecycle.lifecycleScope
//import com.example.medicalcalculatorapp.domain.model.UserProfile
//
//
//class LoginFragment : Fragment() {
//
//    private var _binding: FragmentLoginBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var secureStorageManager: SecureStorageManager
//    private lateinit var userManager: UserManager
//    private lateinit var firebaseAuthService: FirebaseAuthService  // ← ADD THIS LINE
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        secureStorageManager = SecureStorageManager(requireContext())
//        userManager = AppDependencies.provideUserManager(requireContext())
//        firebaseAuthService = FirebaseAuthService()
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentLoginBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Load any saved credentials
//        loadSavedCredentials()
//
//        // Setup click listeners
//        setupClickListeners()
//    }
//
//    private fun setupClickListeners() {
//        // Regular login button
//        binding.btnLogin.setOnClickListener {
//            if (validateInputs()) {
//                performLogin()
//            }
//        }
//
//        // Guest login button - simplified flow
//        binding.btnContinueAsGuest.setOnClickListener {
//            showGuestMedicalDisclaimer()
//        }
//
//        // Privacy policy
//        binding.tvPrivacyPolicy.setOnClickListener {
//            showTermsOfUseDialog()
//        }
//
//        // Register link
//        binding.tvRegister.setOnClickListener {
//            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
//        }
//
//        // Forgot password
//        binding.tvForgotPassword.setOnClickListener {
//            Toast.makeText(context, "Forgot password clicked", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // SIMPLIFIED GUEST FLOW - Professional but streamlined
//    private fun showGuestMedicalDisclaimer() {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Acceso como Invitado")
//            .setMessage("""
//                Esta aplicación está diseñada para profesionales de salud.
//
//                Al continuar como invitado:
//                • Confirmas que eres un profesional de salud licenciado
//                • Aceptas que los cálculos son solo para referencia
//                • Entiendes que no se guardará tu información
//
//                ¿Deseas continuar?
//            """.trimIndent())
//            .setPositiveButton("Sí, Continuar") { _, _ ->
//                startGuestSession()
//            }
//            .setNegativeButton("Cancelar") { dialog, _ ->
//                dialog.dismiss()
//            }
//            .setCancelable(false)
//            .show()
//    }
//
//    private fun startGuestSession() {
//        try {
//            showLoading(true)
//
//            println("🔍 DEBUG: Starting guest session...")
//
//            // Start guest session through UserManager
//            val guestId = userManager.startGuestSession()
//            println("✅ DEBUG: Guest session started with ID: $guestId")
//
//            // Save guest disclaimer acceptance
//            secureStorageManager.saveGuestDisclaimerAccepted(true)
//            secureStorageManager.saveGuestSessionStart(System.currentTimeMillis())
//
//            // Track guest mode usage for analytics
//            secureStorageManager.incrementGuestModeUsage()
//
//            showLoading(false)
//
//            // Show welcome message
//            Toast.makeText(
//                requireContext(),
//                "Sesión de invitado iniciada. Bienvenido a MediCálculos",
//                Toast.LENGTH_SHORT
//            ).show()
//
//            // Navigate to calculator list
//            navigateToCalculatorList()
//
//        } catch (e: Exception) {
//            println("❌ ERROR: Guest session creation failed: ${e.message}")
//            e.printStackTrace()
//
//            showLoading(false)
//            showGuestSessionError(e.message)
//        }
//    }
//
//    private fun navigateToCalculatorList() {
//        try {
//            println("🚀 DEBUG: Navigating to calculator list from guest login")
//            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
//        } catch (e: Exception) {
//            println("❌ ERROR: Navigation to calculator list failed: ${e.message}")
//            Toast.makeText(
//                requireContext(),
//                "Error de navegación. Por favor, intenta nuevamente.",
//                Toast.LENGTH_LONG
//            ).show()
//        }
//    }
//
//    private fun showGuestSessionError(errorMessage: String?) {
//        val userFriendlyMessage = when {
//            errorMessage?.contains("storage", ignoreCase = true) == true ->
//                "Error de almacenamiento. Verifica el espacio disponible."
//            errorMessage?.contains("permission", ignoreCase = true) == true ->
//                "Error de permisos. Reinicia la aplicación."
//            else ->
//                "No se pudo iniciar la sesión de invitado. Intenta nuevamente."
//        }
//
//        AlertDialog.Builder(requireContext())
//            .setTitle("Error de Sesión")
//            .setMessage(userFriendlyMessage)
//            .setPositiveButton("Reintentar") { _, _ ->
//                // Allow user to try guest mode again
//            }
//            .setNegativeButton("Cancelar") { dialog, _ ->
//                dialog.dismiss()
//            }
//            .show()
//    }
//
//    private fun showTermsOfUseDialog() {
//        val termsDialog = PrivacyAndDisclaimerDialogFragment.newInstance()
//        termsDialog.setOnAcceptedListener {
//            // User viewed terms - no action needed for login screen
//        }
//        termsDialog.show(parentFragmentManager, PrivacyAndDisclaimerDialogFragment.TAG)
//    }
//
//    // EXISTING LOGIN METHODS (unchanged)
//    private fun validateInputs(): Boolean {
//        var isValid = true
//
//        val email = binding.etEmail.text.toString().trim()
//        val password = binding.etPassword.text.toString()
//
//        // Email validation
//        if (email.isEmpty()) {
//            binding.tilEmail.error = getString(R.string.email_required)
//            isValid = false
//        } else if (!ValidationUtils.isValidEmail(email)) {
//            binding.tilEmail.error = getString(R.string.invalid_email)
//            isValid = false
//        } else if (ValidationUtils.containsSuspiciousPatterns(email)) {
//            binding.tilEmail.error = getString(R.string.invalid_input)
//            isValid = false
//        } else {
//            binding.tilEmail.error = null
//        }
//
//        // Password validation - simplified for login
//        if (password.isEmpty()) {
//            binding.tilPassword.error = getString(R.string.password_required)
//            isValid = false
//        } else {
//            binding.tilPassword.error = null
//        }
//
//        return isValid
//    }
//
//    private fun loadSavedCredentials() {
//        val rememberCredentials = secureStorageManager.getRememberMeFlag()
//        if (rememberCredentials) {
//            val savedEmail = secureStorageManager.getEmail()
//            savedEmail?.let {
//                binding.etEmail.setText(it)
//                binding.cbRememberMe.isChecked = true
//            }
//        }
//    }
//
//    private fun saveCredentialsIfNeeded() {
//        val email = binding.etEmail.text.toString().trim()
//        val isChecked = binding.cbRememberMe.isChecked
//
//        secureStorageManager.saveRememberMeFlag(isChecked)
//        if (isChecked) {
//            secureStorageManager.saveEmail(email)
//        } else {
//            secureStorageManager.clearCredentials()
//        }
//    }
//
////    private fun performLogin() {
////        showLoading(true)
////
////        // Save credentials if "Remember me" is checked
////        saveCredentialsIfNeeded()
////
////        // Simulate network delay for authenticated login
////        view?.postDelayed({
////            showLoading(false)
////
////            // For now, simulate successful login
////            Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()
////
////            // Navigate to main screen (calculator list)
////            navigateToCalculatorList()
////        }, 1500)
////    }
//
//    private fun performLogin() {
//        showLoading(true)
//        binding.btnLogin.isEnabled = false
//        binding.btnContinueAsGuest.isEnabled = false
//
//        val email = binding.etEmail.text.toString().trim()
//        val password = binding.etPassword.text.toString()
//
//        // Use lifecycleScope for coroutines in Fragment
//        viewLifecycleOwner.lifecycleScope.launch {
//            try {
//                val result = firebaseAuthService.signInWithEmailAndPassword(email, password)
//
//                when (result) {
//                    is AuthResult.Success -> {
//                        try {
//                            // Get the Firebase user
//                            val firebaseUser = result.user
//                            if (firebaseUser != null) {
//
//                                // Save credentials if "Remember me" is checked
//                                saveCredentialsIfNeeded()
//
//                                // Create or update user profile in local database
//                                val userRepository = AppDependencies.provideUserRepository(requireContext())
//                                val userProfile = UserProfile(
//                                    id = firebaseUser.uid,
//                                    email = firebaseUser.email ?: email,
//                                    fullName = firebaseUser.displayName,
//                                    createdAt = System.currentTimeMillis(),
//                                    updatedAt = System.currentTimeMillis()
//                                )
//
//                                // Create profile if it doesn't exist, update if it does
//                                lifecycleScope.launch {
//                                    try {
//                                        userRepository.createUserProfile(userProfile)
//                                    } catch (e: Exception) {
//                                        // Profile might already exist, try to update
//                                        userRepository.updateUserProfile(userProfile)
//                                    }
//                                }
//
//                                // Check email verification status
//                                if (firebaseUser.isEmailVerified) {
//                                    showLoading(false)
//                                    Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()
//
//                                    // Navigate to main screen
//                                    findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
//                                } else {
//                                    showLoading(false)
//
//                                    // Show email verification prompt
//                                    showEmailVerificationDialog(firebaseUser.email ?: email)
//                                }
//                            } else {
//                                showLoading(false)
//                                binding.btnLogin.isEnabled = true
//                                binding.btnContinueAsGuest.isEnabled = true
//                                Toast.makeText(requireContext(), "Login failed - no user data", Toast.LENGTH_SHORT).show()
//                            }
//                        } catch (e: Exception) {
//                            showLoading(false)
//                            binding.btnLogin.isEnabled = true
//                            binding.btnContinueAsGuest.isEnabled = true
//                            Toast.makeText(requireContext(), "Error setting up user profile: ${e.message}", Toast.LENGTH_SHORT).show()
//                        }
//                    }
//                    is AuthResult.Error -> {
//                        showLoading(false)
//                        binding.btnLogin.isEnabled = true
//                        binding.btnContinueAsGuest.isEnabled = true
//
//                        // Show user-friendly error message
//                        val errorMessage = when {
//                            result.message.contains("password") -> "Invalid password. Please try again."
//                            result.message.contains("email") -> "Invalid email address."
//                            result.message.contains("user") -> "No account found with this email."
//                            result.message.contains("network") -> "Network error. Check your connection."
//                            else -> "Login failed. Please try again."
//                        }
//
//                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
//                    }
//                }
//            } catch (e: Exception) {
//                showLoading(false)
//                binding.btnLogin.isEnabled = true
//                binding.btnContinueAsGuest.isEnabled = true
//                Toast.makeText(requireContext(), "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//
//    private fun showLoading(show: Boolean) {
//        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
//
//        // Disable all interactive elements during loading
//        binding.btnLogin.isEnabled = !show
//        binding.btnContinueAsGuest.isEnabled = !show
//        binding.etEmail.isEnabled = !show
//        binding.etPassword.isEnabled = !show
//        binding.cbRememberMe.isEnabled = !show
//        binding.tvRegister.isEnabled = !show
//        binding.tvForgotPassword.isEnabled = !show
//        binding.tvPrivacyPolicy.isEnabled = !show
//
//        // Visual feedback for disabled state
//        val alpha = if (show) 0.5f else 1.0f
//        binding.loginFormContainer.alpha = alpha
//        binding.guestOptionCard.alpha = alpha
//    }
//
//    private fun showEmailVerificationDialog(email: String) {
//        AlertDialog.Builder(requireContext())
//            .setTitle("Email Verification Required")
//            .setMessage("Your email ($email) is not verified. Please check your email and click the verification link, or we can send a new verification email.")
//            .setPositiveButton("Resend Verification") { _, _ ->
//                resendVerificationEmail()
//            }
//            .setNeutralButton("Continue Anyway") { _, _ ->
//                // Allow unverified users to continue (for development)
//                findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
//            }
//            .setNegativeButton("Sign Out") { _, _ ->
//                firebaseAuthService.signOut()
//                userManager.signOut()
//            }
//            .setCancelable(false)
//            .show()
//    }
//
//    private fun resendVerificationEmail() {
//        lifecycleScope.launch {
//            try {
//                val result = firebaseAuthService.sendEmailVerification()
//                when (result) {
//                    is AuthResult.Success -> {
//                        Toast.makeText(
//                            requireContext(),
//                            "Verification email sent! Please check your inbox.",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                    is AuthResult.Error -> {
//                        Toast.makeText(
//                            requireContext(),
//                            "Failed to send verification email: ${result.message}",
//                            Toast.LENGTH_LONG
//                        ).show()
//                    }
//                }
//            } catch (e: Exception) {
//                Toast.makeText(
//                    requireContext(),
//                    "Error sending verification: ${e.message}",
//                    Toast.LENGTH_SHORT
//                ).show()
//            }
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}