package com.example.medicalcalculatorapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.FragmentLoginBinding
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.util.SecureStorageManager
import com.example.medicalcalculatorapp.domain.service.ComplianceManagerService
import com.example.medicalcalculatorapp.di.AppDependencies
import com.example.medicalcalculatorapp.data.auth.FirebaseAuthService
import com.example.medicalcalculatorapp.data.auth.AuthResult
import com.example.medicalcalculatorapp.util.ValidationUtils
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Enhanced LoginFragment - Firebase Integration with Medical App Compliance
 *
 * Provides three clear paths with proper Firebase authentication:
 * 1. Sign In - Existing users with email verification
 * 2. Register - New professional accounts with compliance
 * 3. Guest Mode - Immediate access with compliance
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var userManager: UserManager
    private lateinit var secureStorageManager: SecureStorageManager
    private lateinit var complianceManagerService: ComplianceManagerService
    private lateinit var firebaseAuthService: FirebaseAuthService

    // Track email verification retry attempts
    private var verificationRetryCount = 0
    private val maxVerificationRetries = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize dependencies
        secureStorageManager = SecureStorageManager(requireContext())
        userManager = UserManager(requireContext())
        firebaseAuthService = FirebaseAuthService()

        val userComplianceRepository = AppDependencies.provideUserComplianceRepository(requireContext())
        complianceManagerService = ComplianceManagerService(
            secureStorageManager,
            userManager,
            userComplianceRepository
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

        // Check for existing session
        checkExistingSession()

        setupClickListeners()
        loadSavedCredentials()
    }

    // ====== SESSION MANAGEMENT ======

    private fun checkExistingSession() {
        lifecycleScope.launch {
            try {
                // Check if user is already logged in
                if (firebaseAuthService.isUserLoggedIn()) {
                    val userId = firebaseAuthService.getCurrentUserId()

                    // Check session timeout for security
                    if (firebaseAuthService.isSessionExpired()) {
                        showSessionExpiredDialog()
                        return@launch
                    }

                    // Check compliance status
                    val complianceStatus = complianceManagerService.getComplianceStatus()
                    if (complianceStatus.hasBasicTerms && complianceStatus.hasMedicalDisclaimer) {
                        // User is logged in and compliant - go to calculator list
                        navigateToCalculatorList("Existing session restored")
                        return@launch
                    }
                }

                // No valid session - stay on login screen
                println("🔐 No valid session found - showing login options")

            } catch (e: Exception) {
                println("❌ Error checking existing session: ${e.message}")
            }
        }
    }

    private fun showSessionExpiredDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Sesión Expirada")
            .setMessage("Su sesión ha expirado por seguridad. Por favor, inicie sesión nuevamente.")
            .setPositiveButton("Iniciar Sesión") { _, _ ->
                firebaseAuthService.signOut()
                // Stay on login screen
            }
            .setCancelable(false)
            .show()
    }

    private fun loadSavedCredentials() {
        // Load remember me credentials if available
        if (secureStorageManager.getRememberMeFlag()) {
            val savedEmail = secureStorageManager.getEmail()
            if (!savedEmail.isNullOrEmpty()) {
                binding.etEmail.setText(savedEmail)
            }
        }
    }

    private fun setupClickListeners() {
        // Sign In Button (enhanced Firebase authentication)
        binding.btnLogin.setOnClickListener {
            performLogin()
        }

        // Register Button (new professional accounts)
        binding.btnRegister.setOnClickListener {
            startRegistrationFlow()
        }

        // Guest Mode Button (immediate access)
        binding.btnGuestMode.setOnClickListener {
            startGuestFlow()
        }

        // Forgot Password
        binding.tvForgotPassword?.setOnClickListener {
            showForgotPasswordDialog()
        }
    }

    // ====== ENHANCED FIREBASE LOGIN ======

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Validate inputs
        if (!validateLoginInputs(email, password)) {
            return
        }

        lifecycleScope.launch {
            try {
                showLoading(true, "Iniciando sesión...")

                val result = firebaseAuthService.signInWithEmailAndPassword(email, password)

                handleAuthResult(result, email)

            } catch (e: Exception) {
                showLoading(false)
                println("❌ Login error: ${e.message}")
                showError("Error de conexión: ${e.message}")
            }
        }
    }

    private fun handleAuthResult(result: AuthResult, email: String) {
        lifecycleScope.launch {
            when (result) {
                is AuthResult.Success -> {
                    println("✅ Firebase login successful")

                    // Save credentials if user wants
                    saveCredentialsIfRequested(email)

                    // Sync compliance status
                    val userId = result.user?.uid ?: ""
                    syncUserCompliance(userId)

                    showLoading(false)
                    showSuccess("Inicio de sesión exitoso")

                    // Small delay for user feedback
                    delay(1000)

                    navigateToCalculatorList("Firebase authentication successful")
                }

                is AuthResult.EmailNotVerified -> {
                    showLoading(false)
                    handleEmailNotVerified(result)
                }

                is AuthResult.SecurityIssue -> {
                    showLoading(false)
                    handleSecurityIssue(result)
                }

                is AuthResult.Error -> {
                    showLoading(false)
                    showError(result.message)
                }

                else -> {
                    showLoading(false)
                    showError("Tipo de resultado no manejado")
                }
            }
        }
    }

    private fun handleEmailNotVerified(result: AuthResult.EmailNotVerified) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📧 Verificación de Email Requerida")
            .setMessage(
                "Su email no ha sido verificado. Las aplicaciones médicas requieren verificación de email por seguridad.\n\n" +
                        "¿Desea que le enviemos un nuevo email de verificación?"
            )
            .setPositiveButton("Enviar Verificación") { _, _ ->
                sendEmailVerification()
            }
            .setNegativeButton("Usar Modo Invitado") { _, _ ->
                startGuestFlow()
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun sendEmailVerification() {
        if (verificationRetryCount >= maxVerificationRetries) {
            showError("Máximo número de intentos de verificación alcanzado. Contacte soporte.")
            return
        }

        lifecycleScope.launch {
            try {
                showLoading(true, "Enviando verificación...")
                verificationRetryCount++

                val result = firebaseAuthService.sendEmailVerification()
                showLoading(false)

                when (result) {
                    is AuthResult.Success -> {
                        showVerificationSentDialog()
                    }
                    is AuthResult.Error -> {
                        showError("Error enviando verificación: ${result.message}")
                    }
                    else -> {
                        showError("Error inesperado al enviar verificación")
                    }
                }
            } catch (e: Exception) {
                showLoading(false)
                showError("Error de conexión: ${e.message}")
            }
        }
    }

    private fun showVerificationSentDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("✅ Verificación Enviada")
            .setMessage(
                "Email de verificación enviado exitosamente.\n\n" +
                        "Por favor:\n" +
                        "1. Revise su bandeja de entrada\n" +
                        "2. Haga clic en el enlace de verificación\n" +
                        "3. Regrese aquí e intente iniciar sesión nuevamente"
            )
            .setPositiveButton("Entendido") { _, _ ->
                // Sign out current user so they can sign in again after verification
                firebaseAuthService.signOut()
            }
            .setCancelable(false)
            .show()
    }

    private fun handleSecurityIssue(result: AuthResult.SecurityIssue) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔒 Problema de Seguridad")
            .setMessage(
                "Se detectó un problema de seguridad con su cuenta:\n\n${result.reason}\n\n" +
                        "Por seguridad, no podemos permitir el acceso en este momento. " +
                        "¿Desea contactar soporte o usar modo invitado?"
            )
            .setPositiveButton("Contactar Soporte") { _, _ ->
                // TODO: Implement support contact
                showError("Función de soporte próximamente disponible")
            }
            .setNegativeButton("Modo Invitado") { _, _ ->
                startGuestFlow()
            }
            .setNeutralButton("Cancelar", null)
            .show()
    }

    private fun validateLoginInputs(email: String, password: String): Boolean {
        var isValid = true

        // Email validation
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email requerido"
            isValid = false
        } else if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.error = "Email inválido"
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Password validation
        if (password.isEmpty()) {
            binding.tilPassword.error = "Contraseña requerida"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

    private fun saveCredentialsIfRequested(email: String) {
        // For now, always save for convenience
        // In production, you might want a "Remember me" checkbox
        secureStorageManager.saveEmail(email)
        secureStorageManager.saveRememberMeFlag(true)
    }

    private suspend fun syncUserCompliance(userId: String) {
        try {
            // Check if user has compliance record
            val userComplianceRepo = AppDependencies.provideUserComplianceRepository(requireContext())
            val hasRecord = userComplianceRepo.hasComplianceRecord(userId)

            if (!hasRecord) {
                // Create initial compliance record for new authenticated user
                userComplianceRepo.createUserCompliance(userId)
                println("✅ Created initial compliance record for user: $userId")
            }

        } catch (e: Exception) {
            println("⚠️ Error syncing compliance: ${e.message}")
            // Don't block login for compliance sync errors
        }
    }

    // ====== FORGOT PASSWORD FLOW ======

    private fun showForgotPasswordDialog() {
        val emailInput = android.widget.EditText(requireContext()).apply {
            hint = "Ingrese su email"
            inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS

            // Pre-fill with current email if available
            val currentEmail = binding.etEmail.text.toString().trim()
            if (currentEmail.isNotEmpty()) {
                setText(currentEmail)
            }
        }

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🔑 Recuperar Contraseña")
            .setMessage("Ingrese su email para recibir instrucciones de recuperación:")
            .setView(emailInput)
            .setPositiveButton("Enviar") { _, _ ->
                val email = emailInput.text.toString().trim()
                if (ValidationUtils.isValidEmail(email)) {
                    sendPasswordReset(email)
                } else {
                    showError("Por favor ingrese un email válido")
                }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun sendPasswordReset(email: String) {
        lifecycleScope.launch {
            try {
                showLoading(true, "Enviando recuperación...")

                val result = firebaseAuthService.sendPasswordResetEmail(email)
                showLoading(false)

                when (result) {
                    is AuthResult.Success -> {
                        showSuccess("Email de recuperación enviado a $email")
                    }
                    is AuthResult.Error -> {
                        showError("Error enviando recuperación: ${result.message}")
                    }
                    else -> {
                        showError("Error inesperado")
                    }
                }
            } catch (e: Exception) {
                showLoading(false)
                showError("Error de conexión: ${e.message}")
            }
        }
    }

    // ====== EXISTING FLOWS (unchanged from previous implementation) ======

    private fun startRegistrationFlow() {
        println("🆕 Starting registration flow with compliance")
        showRegistrationCompliance()
    }

    private fun showRegistrationCompliance() {
        val enhancedDisclaimer = EnhancedMedicalDisclaimerDialogFragment.newInstance()

        enhancedDisclaimer.setOnAcceptedListener {
            showProfessionalVerificationForRegistration()
        }

        enhancedDisclaimer.setOnRejectedListener {
            println("❌ Registration disclaimer rejected")
            Toast.makeText(requireContext(), "Medical disclaimer must be accepted to register", Toast.LENGTH_SHORT).show()
        }

        enhancedDisclaimer.show(parentFragmentManager, EnhancedMedicalDisclaimerDialogFragment.TAG)
    }

    private fun showProfessionalVerificationForRegistration() {
        val professionalDialog = ProfessionalVerificationDialogFragment.newInstance()

        professionalDialog.setOnVerifiedListener { professionalType, licenseInfo ->
            println("✅ Professional verification for registration: $professionalType")
            navigateToFirebaseRegistration(professionalType, licenseInfo, true)
        }

        professionalDialog.setOnSkippedListener {
            println("✅ General user registration")
            navigateToFirebaseRegistration("General User", null, false)
        }

        professionalDialog.show(parentFragmentManager, ProfessionalVerificationDialogFragment.TAG)
    }

    private fun startGuestFlow() {
        println("👤 Starting guest flow with compliance")
        showGuestCompliance()
    }

    private fun showGuestCompliance() {
        val enhancedDisclaimer = EnhancedMedicalDisclaimerDialogFragment.newInstance()

        enhancedDisclaimer.setOnAcceptedListener {
            showProfessionalVerificationForGuest()
        }

        enhancedDisclaimer.setOnRejectedListener {
            println("❌ Guest disclaimer rejected")
            Toast.makeText(requireContext(), "Medical disclaimer must be accepted for guest access", Toast.LENGTH_SHORT).show()
        }

        enhancedDisclaimer.show(parentFragmentManager, EnhancedMedicalDisclaimerDialogFragment.TAG)
    }

    private fun showProfessionalVerificationForGuest() {
        val professionalDialog = ProfessionalVerificationDialogFragment.newInstance()

        professionalDialog.setOnVerifiedListener { professionalType, licenseInfo ->
            println("✅ Professional guest verification: $professionalType")
            startGuestSession(true, professionalType, licenseInfo)
        }

        professionalDialog.setOnSkippedListener {
            println("✅ General guest access")
            startGuestSession(false, "General User", null)
        }

        professionalDialog.show(parentFragmentManager, ProfessionalVerificationDialogFragment.TAG)
    }

    // ====== NAVIGATION METHODS ======

    private fun navigateToFirebaseRegistration(professionalType: String, licenseInfo: String?, isProfessional: Boolean) {
        lifecycleScope.launch {
            try {
                println("📝 Navigating to registration screen for: $professionalType")
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            } catch (e: Exception) {
                println("❌ Navigation error: ${e.message}")
                showError("Error de navegación")
            }
        }
    }

    private fun startGuestSession(isProfessional: Boolean, professionalType: String, licenseInfo: String?) {
        lifecycleScope.launch {
            try {
                println("👤 Starting guest session - Professional: $isProfessional, Type: $professionalType")

                val success = complianceManagerService.recordCompleteCompliance(
                    professionalType = if (isProfessional) mapToProfessionalType(professionalType) else com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.OTHER,
                    licenseInfo = licenseInfo
                )

                if (success) {
                    showSuccess("Acceso de invitado otorgado")
                    delay(1000)
                    navigateToCalculatorList("Guest session started")
                } else {
                    showError("Error iniciando sesión de invitado")
                }

            } catch (e: Exception) {
                println("❌ Guest session error: ${e.message}")
                showError("Error iniciando sesión de invitado: ${e.message}")
            }
        }
    }

    private fun navigateToCalculatorList(reason: String) {
        try {
            println("🧮 Navigating to calculator list: $reason")
            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
        } catch (e: Exception) {
            println("❌ Navigation error: ${e.message}")
            showError("Error de navegación")
        }
    }

    // ====== UI HELPER METHODS ======

    private fun showLoading(show: Boolean, message: String = "Cargando...") {
        if (show) {
            binding.btnLogin.isEnabled = false
            binding.btnRegister.isEnabled = false
            binding.btnGuestMode.isEnabled = false
            // You could add a progress bar here
            binding.btnLogin.text = message
        } else {
            binding.btnLogin.isEnabled = true
            binding.btnRegister.isEnabled = true
            binding.btnGuestMode.isEnabled = true
            binding.btnLogin.text = "Iniciar Sesión"
        }
    }

    private fun showError(message: String) {
        Toast.makeText(requireContext(), "❌ $message", Toast.LENGTH_LONG).show()
    }

    private fun showSuccess(message: String) {
        Toast.makeText(requireContext(), "✅ $message", Toast.LENGTH_SHORT).show()
    }

    private fun mapToProfessionalType(professionalTypeString: String): com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType {
        return when (professionalTypeString) {
            "Médico" -> com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.DOCTOR
            "Enfermero/a" -> com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.NURSE
            "Farmacéutico/a" -> com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.PHARMACIST
            "Estudiante de Medicina", "Estudiante de Enfermería" -> com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.STUDENT
            else -> com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.OTHER
        }
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
//import androidx.lifecycle.lifecycleScope
//import androidx.navigation.fragment.findNavController
//import com.example.medicalcalculatorapp.R
//import com.example.medicalcalculatorapp.databinding.FragmentLoginBinding
//import com.example.medicalcalculatorapp.data.user.UserManager
//import com.example.medicalcalculatorapp.util.SecureStorageManager
//import com.example.medicalcalculatorapp.domain.service.ComplianceManagerService
//import com.example.medicalcalculatorapp.di.AppDependencies
//import kotlinx.coroutines.launch
//
///**
// * LoginFragment - Entry point for user authentication and onboarding
// *
// * Provides three clear paths:
// * 1. Sign In - Existing users
// * 2. Register - New professional accounts with compliance
// * 3. Guest Mode - Immediate access with compliance
// */
//class LoginFragment : Fragment() {
//
//    private var _binding: FragmentLoginBinding? = null
//    private val binding get() = _binding!!
//
//    private lateinit var userManager: UserManager
//    private lateinit var secureStorageManager: SecureStorageManager
//    private lateinit var complianceManagerService: ComplianceManagerService
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        // Initialize dependencies
//        secureStorageManager = SecureStorageManager(requireContext())
//        userManager = UserManager(requireContext())
//        val userComplianceRepository = AppDependencies.provideUserComplianceRepository(requireContext())
//
//        complianceManagerService = ComplianceManagerService(
//            secureStorageManager,
//            userManager,
//            userComplianceRepository
//        )
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
//        setupClickListeners()
//    }
//
//    private fun setupClickListeners() {
//        // Sign In Button (existing users)
//        binding.btnLogin.setOnClickListener {
//            performLogin()
//        }
//
//        // Register Button (new professional accounts)
//        binding.btnRegister.setOnClickListener {
//            startRegistrationFlow()
//        }
//
//        // Guest Mode Button (immediate access)
//        binding.btnGuestMode.setOnClickListener {
//            startGuestFlow()
//        }
//
//        // Optional: Forgot Password
//        binding.tvForgotPassword?.setOnClickListener {
//            // TODO: Implement forgot password flow
//            Toast.makeText(requireContext(), "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // ====== EXISTING USER LOGIN ======
//
//    private fun performLogin() {
//        val email = binding.etEmail.text.toString().trim()
//        val password = binding.etPassword.text.toString().trim()
//
//        if (email.isEmpty() || password.isEmpty()) {
//            Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        lifecycleScope.launch {
//            try {
//                // TODO: Implement Firebase login
//                println("📧 Attempting login for: $email")
//
//                // For now, simulate successful login
//                Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
//                navigateToCalculatorList("Existing user login successful")
//
//            } catch (e: Exception) {
//                println("❌ Login error: ${e.message}")
//                Toast.makeText(requireContext(), "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    // ====== NEW USER REGISTRATION FLOW ======
//
//    private fun startRegistrationFlow() {
//        println("🆕 Starting registration flow with compliance")
//        showRegistrationCompliance()
//    }
//
//    private fun showRegistrationCompliance() {
//        // Show medical disclaimer first for registration
//        val enhancedDisclaimer = EnhancedMedicalDisclaimerDialogFragment.newInstance()
//
//        enhancedDisclaimer.setOnAcceptedListener {
//            // After disclaimer accepted, show professional verification for registration
//            showProfessionalVerificationForRegistration()
//        }
//
//        enhancedDisclaimer.setOnRejectedListener {
//            println("❌ Registration disclaimer rejected")
//            Toast.makeText(requireContext(), "Medical disclaimer must be accepted to register", Toast.LENGTH_SHORT).show()
//        }
//
//        enhancedDisclaimer.show(parentFragmentManager, EnhancedMedicalDisclaimerDialogFragment.TAG)
//    }
//
//    private fun showProfessionalVerificationForRegistration() {
//        val professionalDialog = ProfessionalVerificationDialogFragment.newInstance()
//
//        professionalDialog.setOnVerifiedListener { professionalType, licenseInfo ->
//            println("✅ Professional verification for registration: $professionalType")
//            // Navigate to Firebase registration with professional data
//            navigateToFirebaseRegistration(professionalType, licenseInfo, true)
//        }
//
//        professionalDialog.setOnSkippedListener {
//            println("✅ General user registration")
//            // Navigate to Firebase registration as general user
//            navigateToFirebaseRegistration("General User", null, false)
//        }
//
//        professionalDialog.show(parentFragmentManager, ProfessionalVerificationDialogFragment.TAG)
//    }
//
//    // ====== GUEST MODE FLOW ======
//
//    private fun startGuestFlow() {
//        println("👤 Starting guest flow with compliance")
//        showGuestCompliance()
//    }
//
//    private fun showGuestCompliance() {
//        // Show medical disclaimer first for guest mode
//        val enhancedDisclaimer = EnhancedMedicalDisclaimerDialogFragment.newInstance()
//
//        enhancedDisclaimer.setOnAcceptedListener {
//            // After disclaimer accepted, show professional verification for guest
//            showProfessionalVerificationForGuest()
//        }
//
//        enhancedDisclaimer.setOnRejectedListener {
//            println("❌ Guest disclaimer rejected")
//            Toast.makeText(requireContext(), "Medical disclaimer must be accepted for guest access", Toast.LENGTH_SHORT).show()
//        }
//
//        enhancedDisclaimer.show(parentFragmentManager, EnhancedMedicalDisclaimerDialogFragment.TAG)
//    }
//
//    private fun showProfessionalVerificationForGuest() {
//        val professionalDialog = ProfessionalVerificationDialogFragment.newInstance()
//
//        professionalDialog.setOnVerifiedListener { professionalType, licenseInfo ->
//            println("✅ Professional guest verification: $professionalType")
//            // Start guest session with professional status
//            startGuestSession(true, professionalType, licenseInfo)
//        }
//
//        professionalDialog.setOnSkippedListener {
//            println("✅ General guest access")
//            // Start guest session with general status
//            startGuestSession(false, "General User", null)
//        }
//
//        professionalDialog.show(parentFragmentManager, ProfessionalVerificationDialogFragment.TAG)
//    }
//
//    // ====== NAVIGATION METHODS ====== (Updated)
//
//    private fun navigateToFirebaseRegistration(professionalType: String, licenseInfo: String?, isProfessional: Boolean) {
//        lifecycleScope.launch {
//            try {
//                println("📝 Navigating to registration screen for: $professionalType")
//
//                // Store the professional verification data to pass to RegisterFragment
//                // You can use SharedPreferences, Bundle, or ViewModel to pass this data
//                //storeRegistrationData(professionalType, licenseInfo, isProfessional)
//
//                // Navigate to the actual RegisterFragment
//                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
//
//            } catch (e: Exception) {
//                println("❌ Navigation error: ${e.message}")
//                Toast.makeText(requireContext(), "Navigation error occurred", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    // Helper method to store registration data for RegisterFragment
////    private fun storeRegistrationData(professionalType: String, licenseInfo: String?, isProfessional: Boolean) {
////        val sharedPrefs = requireContext().getSharedPreferences("registration_temp", Context.MODE_PRIVATE)
////        sharedPrefs.edit().apply {
////            putString("professional_type", professionalType)
////            putString("license_info", licenseInfo)
////            putBoolean("is_professional", isProfessional)
////            apply()
////        }
////    }
//
//
//    private fun startGuestSession(isProfessional: Boolean, professionalType: String, licenseInfo: String?) {
//        lifecycleScope.launch {
//            try {
//                println("👤 Starting guest session - Professional: $isProfessional, Type: $professionalType")
//
//                // Record compliance for guest user
//                val success = complianceManagerService.recordCompleteCompliance(
//                    professionalType = if (isProfessional) mapToProfessionalType(professionalType) else com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.OTHER,
//                    licenseInfo = licenseInfo
//                )
//
//                if (success) {
//                    Toast.makeText(requireContext(), "Guest access granted", Toast.LENGTH_SHORT).show()
//                    navigateToCalculatorList("Guest session started")
//                } else {
//                    Toast.makeText(requireContext(), "Failed to start guest session", Toast.LENGTH_SHORT).show()
//                }
//
//            } catch (e: Exception) {
//                println("❌ Guest session error: ${e.message}")
//                Toast.makeText(requireContext(), "Failed to start guest session: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    private fun navigateToCalculatorList(reason: String) {
//        try {
//            println("🧮 Navigating to calculator list: $reason")
//            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
//        } catch (e: Exception) {
//            println("❌ Navigation error: ${e.message}")
//            Toast.makeText(requireContext(), "Navigation error occurred", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    // ====== HELPER METHODS ======
//
//    private fun mapToProfessionalType(professionalTypeString: String): com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType {
//        return when (professionalTypeString) {
//            "Médico" -> com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.DOCTOR
//            "Enfermero/a" -> com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.NURSE
//            "Farmacéutico/a" -> com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.PHARMACIST
//            "Estudiante de Medicina", "Estudiante de Enfermería" -> com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.STUDENT
//            else -> com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.OTHER
//        }
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
//
//
