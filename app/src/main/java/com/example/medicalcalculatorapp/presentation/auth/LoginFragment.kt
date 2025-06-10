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
import com.example.medicalcalculatorapp.di.AppDependencies
import com.example.medicalcalculatorapp.data.auth.FirebaseAuthService
import com.example.medicalcalculatorapp.data.auth.AuthResult
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var secureStorageManager: SecureStorageManager
    private lateinit var userManager: UserManager
    private lateinit var firebaseAuthService: FirebaseAuthService  // ← ADD THIS LINE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureStorageManager = SecureStorageManager(requireContext())
        userManager = AppDependencies.provideUserManager(requireContext())
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

        // Load any saved credentials
        loadSavedCredentials()

        // Setup click listeners
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Regular login button
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        // Guest login button - simplified flow
        binding.btnContinueAsGuest.setOnClickListener {
            showGuestMedicalDisclaimer()
        }

        // Privacy policy
        binding.tvPrivacyPolicy.setOnClickListener {
            showTermsOfUseDialog()
        }

        // Register link
        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // Forgot password
        binding.tvForgotPassword.setOnClickListener {
            Toast.makeText(context, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }
    }

    // SIMPLIFIED GUEST FLOW - Professional but streamlined
    private fun showGuestMedicalDisclaimer() {
        AlertDialog.Builder(requireContext())
            .setTitle("Acceso como Invitado")
            .setMessage("""
                Esta aplicación está diseñada para profesionales de salud.
                
                Al continuar como invitado:
                • Confirmas que eres un profesional de salud licenciado
                • Aceptas que los cálculos son solo para referencia
                • Entiendes que no se guardará tu información
                
                ¿Deseas continuar?
            """.trimIndent())
            .setPositiveButton("Sí, Continuar") { _, _ ->
                startGuestSession()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun startGuestSession() {
        try {
            showLoading(true)

            println("🔍 DEBUG: Starting guest session...")

            // Start guest session through UserManager
            val guestId = userManager.startGuestSession()
            println("✅ DEBUG: Guest session started with ID: $guestId")

            // Save guest disclaimer acceptance
            secureStorageManager.saveGuestDisclaimerAccepted(true)
            secureStorageManager.saveGuestSessionStart(System.currentTimeMillis())

            // Track guest mode usage for analytics
            secureStorageManager.incrementGuestModeUsage()

            showLoading(false)

            // Show welcome message
            Toast.makeText(
                requireContext(),
                "Sesión de invitado iniciada. Bienvenido a MediCálculos",
                Toast.LENGTH_SHORT
            ).show()

            // Navigate to calculator list
            navigateToCalculatorList()

        } catch (e: Exception) {
            println("❌ ERROR: Guest session creation failed: ${e.message}")
            e.printStackTrace()

            showLoading(false)
            showGuestSessionError(e.message)
        }
    }

    private fun navigateToCalculatorList() {
        try {
            println("🚀 DEBUG: Navigating to calculator list from guest login")
            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
        } catch (e: Exception) {
            println("❌ ERROR: Navigation to calculator list failed: ${e.message}")
            Toast.makeText(
                requireContext(),
                "Error de navegación. Por favor, intenta nuevamente.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showGuestSessionError(errorMessage: String?) {
        val userFriendlyMessage = when {
            errorMessage?.contains("storage", ignoreCase = true) == true ->
                "Error de almacenamiento. Verifica el espacio disponible."
            errorMessage?.contains("permission", ignoreCase = true) == true ->
                "Error de permisos. Reinicia la aplicación."
            else ->
                "No se pudo iniciar la sesión de invitado. Intenta nuevamente."
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Error de Sesión")
            .setMessage(userFriendlyMessage)
            .setPositiveButton("Reintentar") { _, _ ->
                // Allow user to try guest mode again
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showTermsOfUseDialog() {
        val termsDialog = PrivacyAndDisclaimerDialogFragment.newInstance()
        termsDialog.setOnAcceptedListener {
            // User viewed terms - no action needed for login screen
        }
        termsDialog.show(parentFragmentManager, PrivacyAndDisclaimerDialogFragment.TAG)
    }

    // EXISTING LOGIN METHODS (unchanged)
    private fun validateInputs(): Boolean {
        var isValid = true

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

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

        // Password validation - simplified for login
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

//    private fun performLogin() {
//        showLoading(true)
//
//        // Save credentials if "Remember me" is checked
//        saveCredentialsIfNeeded()
//
//        // Simulate network delay for authenticated login
//        view?.postDelayed({
//            showLoading(false)
//
//            // For now, simulate successful login
//            Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()
//
//            // Navigate to main screen (calculator list)
//            navigateToCalculatorList()
//        }, 1500)
//    }

    private fun performLogin() {
        showLoading(true)
        binding.btnLogin.isEnabled = false
        binding.btnContinueAsGuest.isEnabled = false

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()

        // Use lifecycleScope for coroutines in Fragment
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = firebaseAuthService.signInWithEmailAndPassword(email, password)

                when (result) {
                    is AuthResult.Success -> {
                        // Save credentials if "Remember me" is checked
                        saveCredentialsIfNeeded()

                        showLoading(false)
                        Toast.makeText(requireContext(), "Login successful!", Toast.LENGTH_SHORT).show()

                        // Navigate to main screen
                        findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
                    }
                    is AuthResult.Error -> {
                        showLoading(false)
                        binding.btnLogin.isEnabled = true
                        binding.btnContinueAsGuest.isEnabled = true

                        // Show user-friendly error message
                        val errorMessage = when {
                            result.message.contains("password") -> "Invalid password. Please try again."
                            result.message.contains("email") -> "Invalid email address."
                            result.message.contains("user") -> "No account found with this email."
                            result.message.contains("network") -> "Network error. Check your connection."
                            else -> "Login failed. Please try again."
                        }

                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                showLoading(false)
                binding.btnLogin.isEnabled = true
                binding.btnContinueAsGuest.isEnabled = true
                Toast.makeText(requireContext(), "Unexpected error: ${e.message}", Toast.LENGTH_LONG).show()
            }
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
        binding.tvRegister.isEnabled = !show
        binding.tvForgotPassword.isEnabled = !show
        binding.tvPrivacyPolicy.isEnabled = !show

        // Visual feedback for disabled state
        val alpha = if (show) 0.5f else 1.0f
        binding.loginFormContainer.alpha = alpha
        binding.guestOptionCard.alpha = alpha
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}