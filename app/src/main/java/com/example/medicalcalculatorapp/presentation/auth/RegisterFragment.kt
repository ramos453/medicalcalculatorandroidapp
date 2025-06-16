package com.example.medicalcalculatorapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.FragmentRegisterBinding
import com.example.medicalcalculatorapp.util.ValidationUtils
import com.example.medicalcalculatorapp.data.auth.FirebaseAuthService
import com.example.medicalcalculatorapp.data.auth.AuthResult
import com.example.medicalcalculatorapp.di.AppDependencies
import com.example.medicalcalculatorapp.domain.service.ComplianceManagerService
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.util.SecureStorageManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

/**
 * Enhanced RegisterFragment - Firebase Integration with Medical App Compliance
 *
 * Creates new professional accounts with:
 * - Firebase Authentication with email verification
 * - Professional profile creation
 * - Compliance record initialization
 * - Enhanced security validation
 */
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuthService: FirebaseAuthService
    private lateinit var userManager: UserManager
    private lateinit var secureStorageManager: SecureStorageManager
    private lateinit var complianceManagerService: ComplianceManagerService

    // Professional verification data (passed from login flow)
    private var professionalType: String? = null
    private var licenseInfo: String? = null
    private var isProfessional: Boolean = false

    // Email verification tracking
    private var registeredEmail: String? = null
    private var verificationRetryCount = 0
    private val maxVerificationRetries = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize services
        firebaseAuthService = FirebaseAuthService()
        userManager = UserManager(requireContext())
        secureStorageManager = SecureStorageManager(requireContext())

        val userComplianceRepository = AppDependencies.provideUserComplianceRepository(requireContext())
        complianceManagerService = ComplianceManagerService(
            secureStorageManager,
            userManager,
            userComplianceRepository
        )

        // Load professional verification data from previous flow
        loadProfessionalVerificationData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupClickListeners()
        displayProfessionalInfo()
    }

    // ====== SETUP METHODS ======

    private fun loadProfessionalVerificationData() {
        // In a real implementation, you might pass this data through navigation arguments
        // For now, we'll use SharedPreferences as a temporary solution
        val sharedPrefs = requireContext().getSharedPreferences("registration_temp", android.content.Context.MODE_PRIVATE)

        professionalType = sharedPrefs.getString("professional_type", null)
        licenseInfo = sharedPrefs.getString("license_info", null)
        isProfessional = sharedPrefs.getBoolean("is_professional", false)

        println("🔍 Loaded professional data - Type: $professionalType, Professional: $isProfessional")
    }

    private fun setupUI() {
        // Pre-fill professional information if available
        if (isProfessional && !professionalType.isNullOrEmpty()) {
            // You could add a professional info section here
            binding.etName.hint = "Nombre Profesional (${professionalType})"
        }
    }

    private fun displayProfessionalInfo() {
        if (isProfessional && !professionalType.isNullOrEmpty()) {
            // Add a visual indicator of professional status
            Toast.makeText(
                requireContext(),
                "✅ Registrando como: $professionalType",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                performRegistration()
            }
        }

        binding.tvLogin.setOnClickListener {
            navigateBackToLogin()
        }

        binding.ivBack.setOnClickListener {
            navigateBackToLogin()
        }
    }

    // ====== ENHANCED FIREBASE REGISTRATION ======

    private fun performRegistration() {
        val name = ValidationUtils.sanitizeInput(binding.etName.text.toString().trim())
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        lifecycleScope.launch {
            try {
                showLoading(true, "Creando cuenta...")

                // Store email for verification flow
                registeredEmail = email

                val result = firebaseAuthService.createUserWithEmailAndPassword(email, password)

                handleRegistrationResult(result, name, email)

            } catch (e: Exception) {
                showLoading(false)
                println("❌ Registration error: ${e.message}")
                showError("Error de conexión: ${e.message}")
            }
        }
    }

    private fun handleRegistrationResult(result: AuthResult, name: String, email: String) {
        lifecycleScope.launch {
            when (result) {
                is AuthResult.AccountCreated -> {
                    println("✅ Firebase account created successfully")

                    // Update user profile with name
                    updateUserProfile(result.user.uid, name)
                }

                is AuthResult.Success -> {
                    // Fallback case - account created and user returned
                    println("✅ Account created (success fallback)")

                    if (result.user != null) {
                        updateUserProfile(result.user.uid, name)
                    } else {
                        showAccountCreatedDialog(email)
                    }
                }

                is AuthResult.Error -> {
                    showLoading(false)
                    handleRegistrationError(result.message)
                }

                else -> {
                    showLoading(false)
                    showError("Tipo de resultado inesperado durante el registro")
                }
            }
        }
    }

    private suspend fun updateUserProfile(userId: String, name: String) {
        try {
            // Update Firebase profile
            val profileResult = firebaseAuthService.updateUserProfile(name)

            when (profileResult) {
                is AuthResult.Success -> {
                    println("✅ Profile updated successfully")

                    // Create compliance record
                    createComplianceRecord(userId, name)
                }

                is AuthResult.Error -> {
                    println("⚠️ Profile update failed: ${profileResult.message}")
                    // Continue with compliance creation even if profile update fails
                    createComplianceRecord(userId, name)
                }

                else -> {
                    println("⚠️ Unexpected profile update result")
                    createComplianceRecord(userId, name)
                }
            }

        } catch (e: Exception) {
            println("❌ Error updating profile: ${e.message}")
            // Continue with compliance creation
            createComplianceRecord(userId, name)
        }
    }

    private suspend fun createComplianceRecord(userId: String, name: String) {
        try {
            // Create initial compliance record
            val userComplianceRepo = AppDependencies.provideUserComplianceRepository(requireContext())
            userComplianceRepo.createUserCompliance(userId)

            // Record compliance based on professional verification
            val success = complianceManagerService.recordCompleteCompliance(
                professionalType = if (isProfessional) mapToProfessionalType(professionalType ?: "OTHER") else com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.OTHER,
                licenseInfo = licenseInfo
            )

            if (success) {
                println("✅ Compliance record created for new user")
            } else {
                println("⚠️ Failed to create compliance record")
            }

            // Create user profile record
            createUserProfileRecord(userId, name)

        } catch (e: Exception) {
            println("❌ Error creating compliance record: ${e.message}")
            // Continue to show success dialog even if compliance fails
            showAccountCreatedDialog(registeredEmail ?: "")
        }
    }

    private suspend fun createUserProfileRecord(userId: String, name: String) {
        try {
            val userRepo = AppDependencies.provideUserRepository(requireContext())

            val userProfile = com.example.medicalcalculatorapp.domain.model.UserProfile(
                id = userId,
                email = registeredEmail ?: "",
                fullName = name,
                profession = if (isProfessional) professionalType else null,
                licenseNumber = licenseInfo,
                language = "es"
            )

            val success = userRepo.createUserProfile(userProfile)

            if (success) {
                println("✅ User profile created successfully")
            } else {
                println("⚠️ Failed to create user profile")
            }

        } catch (e: Exception) {
            println("❌ Error creating user profile: ${e.message}")
        } finally {
            showLoading(false)
            showAccountCreatedDialog(registeredEmail ?: "")
        }
    }

    private fun showAccountCreatedDialog(email: String) {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("🎉 Cuenta Creada Exitosamente")
            .setMessage(
                "Su cuenta profesional ha sido creada.\n\n" +
                        "📧 Email: $email\n" +
                        "👨‍⚕️ Tipo: ${professionalType ?: "Usuario General"}\n\n" +
                        "IMPORTANTE: Debe verificar su email antes de poder iniciar sesión.\n\n" +
                        "Por favor:\n" +
                        "1. Revise su bandeja de entrada\n" +
                        "2. Haga clic en el enlace de verificación\n" +
                        "3. Regrese a la pantalla de login"
            )
            .setPositiveButton("Ir a Login") { _, _ ->
                // Clear professional data and navigate to login
                clearProfessionalData()
                navigateToLoginWithMessage("Cuenta creada - Verifique su email")
            }
            .setNegativeButton("Reenviar Verificación") { _, _ ->
                sendEmailVerification()
            }
            .setCancelable(false)
            .show()
    }

    private fun sendEmailVerification() {
        if (verificationRetryCount >= maxVerificationRetries) {
            showError("Máximo número de intentos alcanzado. Vaya a login para intentar nuevamente.")
            navigateToLoginWithMessage("Límite de verificación alcanzado")
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
                        showSuccess("Verificación enviada a $registeredEmail")

                        // Show final dialog
                        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                            .setTitle("✅ Verificación Enviada")
                            .setMessage("Email de verificación enviado exitosamente. Por favor revise su bandeja de entrada.")
                            .setPositiveButton("Ir a Login") { _, _ ->
                                clearProfessionalData()
                                navigateToLoginWithMessage("Verificación enviada")
                            }
                            .setCancelable(false)
                            .show()
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

    // ====== VALIDATION ======

    private fun validateInputs(): Boolean {
        var isValid = true

        val name = ValidationUtils.sanitizeInput(binding.etName.text.toString().trim())
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Update the name field with sanitized input
        if (binding.etName.text.toString() != name) {
            binding.etName.setText(name)
        }

        // Name validation
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.name_required)
            isValid = false
        } else if (name.length < 2) {
            binding.tilName.error = "El nombre debe tener al menos 2 caracteres"
            isValid = false
        } else {
            binding.tilName.error = null
        }

        // Email validation
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

        // Password validation - Enhanced for medical app security
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.password_required)
            isValid = false
        } else if (!isStrongPassword(password)) {
            binding.tilPassword.error = "La contraseña debe tener al menos 8 caracteres, incluir mayúsculas, minúsculas, números y símbolos"
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        // Confirm password validation
        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.passwords_not_matching)
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }

    private fun isStrongPassword(password: String): Boolean {
        // Enhanced password requirements for medical apps
        if (password.length < 8) return false

        val hasUppercase = password.any { it.isUpperCase() }
        val hasLowercase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }

        return hasUppercase && hasLowercase && hasDigit && hasSpecialChar
    }

    // ====== ERROR HANDLING ======

    private fun handleRegistrationError(message: String) {
        val userFriendlyMessage = when {
            message.contains("email-already-in-use", ignoreCase = true) ->
                "Este email ya está registrado. ¿Desea ir a la pantalla de login?"
            message.contains("weak-password", ignoreCase = true) ->
                "La contraseña es muy débil. Use al menos 8 caracteres con mayúsculas, minúsculas, números y símbolos."
            message.contains("invalid-email", ignoreCase = true) ->
                "Por favor ingrese un email válido."
            message.contains("network", ignoreCase = true) ->
                "Error de conexión. Verifique su internet e intente nuevamente."
            else -> "Error de registro: $message"
        }

        if (message.contains("email-already-in-use", ignoreCase = true)) {
            showEmailAlreadyExistsDialog()
        } else {
            showError(userFriendlyMessage)
        }
    }

    private fun showEmailAlreadyExistsDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Email Ya Registrado")
            .setMessage(
                "Ya existe una cuenta con este email.\n\n" +
                        "¿Desea ir a la pantalla de login para iniciar sesión?"
            )
            .setPositiveButton("Ir a Login") { _, _ ->
                // Pre-fill email in login screen
                val email = binding.etEmail.text.toString()
                navigateToLoginWithEmail(email)
            }
            .setNegativeButton("Cambiar Email") { _, _ ->
                // Focus on email field to change it
                binding.etEmail.requestFocus()
                binding.etEmail.selectAll()
            }
            .show()
    }

    // ====== NAVIGATION METHODS ======

    private fun navigateBackToLogin() {
        clearProfessionalData()
        findNavController().popBackStack()
    }

    private fun navigateToLoginWithMessage(message: String) {
        clearProfessionalData()
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        findNavController().popBackStack()
    }

    private fun navigateToLoginWithEmail(email: String) {
        clearProfessionalData()

        // Store email for login screen
        secureStorageManager.saveEmail(email)

        Toast.makeText(requireContext(), "Email guardado para login", Toast.LENGTH_SHORT).show()
        findNavController().popBackStack()
    }

    private fun clearProfessionalData() {
        // Clear temporary professional verification data
        val sharedPrefs = requireContext().getSharedPreferences("registration_temp", android.content.Context.MODE_PRIVATE)
        sharedPrefs.edit().clear().apply()
    }

    // ====== UI HELPER METHODS ======

    private fun showLoading(show: Boolean, message: String = "Procesando...") {
        binding.btnRegister.isEnabled = !show
        binding.btnRegister.text = if (show) message else "Registrarse"
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
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
//import androidx.navigation.fragment.findNavController
//import com.example.medicalcalculatorapp.R
//import com.example.medicalcalculatorapp.databinding.FragmentRegisterBinding
//import com.example.medicalcalculatorapp.util.ValidationUtils
//import com.example.medicalcalculatorapp.data.auth.FirebaseAuthService
//import com.example.medicalcalculatorapp.data.auth.AuthResult
//import androidx.lifecycle.lifecycleScope
//import kotlinx.coroutines.launch
//
//
//class RegisterFragment : Fragment() {
//
//    private var _binding: FragmentRegisterBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var firebaseAuthService: FirebaseAuthService
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
//        firebaseAuthService = FirebaseAuthService()
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        // Setup click listeners
//        binding.btnRegister.setOnClickListener {
//            if (validateInputs()) {
//                performRegistration()
//            }
//        }
//
//        binding.tvLogin.setOnClickListener {
//            findNavController().popBackStack()
//        }
//
//        binding.ivBack.setOnClickListener {
//            findNavController().popBackStack()
//        }
//    }
//
////    private fun validateInputs(): Boolean {
////        var isValid = true
////
////        val name = binding.etName.text.toString().trim()
////        val email = binding.etEmail.text.toString().trim()
////        val password = binding.etPassword.text.toString().trim()
////        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
////
////        // Name validation
////        if (name.isEmpty()) {
////            binding.tilName.error = getString(R.string.name_required)
////            isValid = false
////        } else {
////            binding.tilName.error = null
////        }
////
////        // Email validation
////        if (email.isEmpty()) {
////            binding.tilEmail.error = getString(R.string.email_required)
////            isValid = false
////        } else if (!isValidEmail(email)) {
////            binding.tilEmail.error = getString(R.string.invalid_email)
////            isValid = false
////        } else {
////            binding.tilEmail.error = null
////        }
////
////        // Password validation
////        if (password.isEmpty()) {
////            binding.tilPassword.error = getString(R.string.password_required)
////            isValid = false
////        } else if (password.length < 6) {
////            binding.tilPassword.error = getString(R.string.password_too_short)
////            isValid = false
////        } else {
////            binding.tilPassword.error = null
////        }
////
////        // Confirm password validation
////        if (password != confirmPassword) {
////            binding.tilConfirmPassword.error = getString(R.string.passwords_not_matching)
////            isValid = false
////        } else {
////            binding.tilConfirmPassword.error = null
////        }
////
////        return isValid
////    }
//
//    private fun validateInputs(): Boolean {
//        var isValid = true
//
//        val name = ValidationUtils.sanitizeInput(binding.etName.text.toString().trim())
//        val email = binding.etEmail.text.toString().trim()
//        val password = binding.etPassword.text.toString()
//        val confirmPassword = binding.etConfirmPassword.text.toString()
//
//        // Update the name field with sanitized input
//        if (binding.etName.text.toString() != name) {
//            binding.etName.setText(name)
//        }
//
//        // Name validation
//        if (name.isEmpty()) {
//            binding.tilName.error = getString(R.string.name_required)
//            isValid = false
//        } else {
//            binding.tilName.error = null
//        }
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
//        // Password validation
//        if (password.isEmpty()) {
//            binding.tilPassword.error = getString(R.string.password_required)
//            isValid = false
//        } else if (!ValidationUtils.isValidPassword(password)) {
//            binding.tilPassword.error = getString(R.string.password_requirements)
//            isValid = false
//        } else {
//            binding.tilPassword.error = null
//        }
//
//        // Confirm password validation
//        if (password != confirmPassword) {
//            binding.tilConfirmPassword.error = getString(R.string.passwords_not_matching)
//            isValid = false
//        } else {
//            binding.tilConfirmPassword.error = null
//        }
//
//        return isValid
//    }
//
//    private fun isValidEmail(email: String): Boolean {
//        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
//    }
//
////    private fun performRegistration() {
////        binding.progressBar.visibility = View.VISIBLE
////        binding.btnRegister.isEnabled = false
////
////        // Simulate network delay
////        view?.postDelayed({
////            binding.progressBar.visibility = View.GONE
////            binding.btnRegister.isEnabled = true
////
////            // For now, simulate successful registration
////            Toast.makeText(requireContext(), R.string.register_success, Toast.LENGTH_SHORT).show()
////
////            // Navigate back to login screen
////            findNavController().popBackStack()
////        }, 1500)
////    }
//
//    private fun performRegistration() {
//        binding.progressBar.visibility = View.VISIBLE
//        binding.btnRegister.isEnabled = false
//
//        val name = ValidationUtils.sanitizeInput(binding.etName.text.toString().trim())
//        val email = binding.etEmail.text.toString().trim()
//        val password = binding.etPassword.text.toString()
//
//        // Use lifecycleScope for coroutines in Fragment
//        lifecycleScope.launch {
//            try {
//                // Create the user account with Firebase
//                val result = firebaseAuthService.createUserWithEmailAndPassword(email, password)
//
//                when (result) {
//                    is AuthResult.Success -> {
//                        // Update user profile with display name
//                        val profileResult = firebaseAuthService.updateUserProfile(name)
//
//                        when (profileResult) {
//                            is AuthResult.Success -> {
//                                // Send email verification
//                                val verificationResult = firebaseAuthService.sendEmailVerification()
//
//                                binding.progressBar.visibility = View.GONE
//                                binding.btnRegister.isEnabled = true
//
//                                when (verificationResult) {
//                                    is AuthResult.Success -> {
//                                        Toast.makeText(
//                                            requireContext(),
//                                            "Account created! Please check your email to verify your account.",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//                                    }
//                                    is AuthResult.Error -> {
//                                        Toast.makeText(
//                                            requireContext(),
//                                            "Account created, but verification email failed. You can request it later.",
//                                            Toast.LENGTH_LONG
//                                        ).show()
//                                    }
//                                }
//
//                                // Navigate back to login screen
//                                findNavController().popBackStack()
//                            }
//                            is AuthResult.Error -> {
//                                binding.progressBar.visibility = View.GONE
//                                binding.btnRegister.isEnabled = true
//                                Toast.makeText(
//                                    requireContext(),
//                                    "Account created but profile update failed: ${profileResult.message}",
//                                    Toast.LENGTH_LONG
//                                ).show()
//                            }
//                        }
//                    }
//                    is AuthResult.Error -> {
//                        binding.progressBar.visibility = View.GONE
//                        binding.btnRegister.isEnabled = true
//
//                        // Show user-friendly error message
//                        val errorMessage = when {
//                            result.message.contains("email-already-in-use") -> "This email is already registered. Try logging in instead."
//                            result.message.contains("weak-password") -> "Password is too weak. Please use at least 6 characters."
//                            result.message.contains("invalid-email") -> "Please enter a valid email address."
//                            result.message.contains("network") -> "Network error. Check your connection."
//                            else -> "Registration failed: ${result.message}"
//                        }
//
//                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
//                    }
//                }
//            } catch (e: Exception) {
//                binding.progressBar.visibility = View.GONE
//                binding.btnRegister.isEnabled = true
//                Toast.makeText(requireContext(), "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//        }
//    }
//
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
//
