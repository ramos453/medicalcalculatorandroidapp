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

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var secureStorageManager: SecureStorageManager
    private lateinit var userManager: UserManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureStorageManager = SecureStorageManager(requireContext())
        userManager = UserManager(requireContext())
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

        // ✅ NEW: Guest login button
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

    private fun showGuestMedicalDisclaimer() {
        // First verify they're a medical professional
        showProfessionalVerificationDialog { isProfessional ->
            if (isProfessional) {
                showGuestTermsDialog()
            } else {
                showNonProfessionalMessage()
            }
        }
    }

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

    private fun showNonProfessionalMessage() {
        AlertDialog.Builder(requireContext())
            .setTitle("Acceso Restringido")
            .setMessage("Esta aplicación está diseñada exclusivamente para profesionales de salud licenciados.\n\nSi eres un profesional médico, por favor verifica tu estatus. Si no lo eres, consulta con un profesional de salud calificado.")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showGuestTermsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.guest_medical_disclaimer_title))
            .setMessage(getString(R.string.guest_medical_disclaimer_text))
            .setPositiveButton("Acepto como Profesional de Salud") { _, _ ->
                startGuestSession()
            }
            .setNegativeButton("No Acepto") { dialog, _ ->
                dialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.guest_disclaimer_required),
                    Toast.LENGTH_LONG
                ).show()
            }
            .setCancelable(false)
            .show()
    }

    private fun startGuestSession() {
        try {
            showLoading(true)

            // Start guest session
            val guestId = userManager.startGuestSession()

            // Save guest disclaimer acceptance
            secureStorageManager.saveGuestDisclaimerAccepted(true)
            secureStorageManager.saveGuestSessionStart(System.currentTimeMillis())

            // Track guest mode usage
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

    private fun showTermsOfUseDialog() {
        val termsDialog = PrivacyAndDisclaimerDialogFragment.newInstance()
        termsDialog.setOnAcceptedListener {
            // User viewed terms - no action needed for login screen
        }
        termsDialog.show(parentFragmentManager, PrivacyAndDisclaimerDialogFragment.TAG)
    }

    // ✅ EXISTING LOGIN METHODS (unchanged)

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

    private fun performLogin() {
        showLoading(true)
        binding.btnLogin.isEnabled = false
        binding.btnContinueAsGuest.isEnabled = false

        // Save credentials if "Remember me" is checked
        saveCredentialsIfNeeded()

        // Simulate network delay
        view?.postDelayed({
            showLoading(false)
            binding.btnLogin.isEnabled = true
            binding.btnContinueAsGuest.isEnabled = true

            // For now, simulate successful login
            Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()

            // Navigate to main screen (calculator list)
            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
        }, 1500)
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