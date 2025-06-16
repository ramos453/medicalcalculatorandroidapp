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
import kotlinx.coroutines.launch

/**
 * LoginFragment - Entry point for user authentication and onboarding
 *
 * Provides three clear paths:
 * 1. Sign In - Existing users
 * 2. Register - New professional accounts with compliance
 * 3. Guest Mode - Immediate access with compliance
 */
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var userManager: UserManager
    private lateinit var secureStorageManager: SecureStorageManager
    private lateinit var complianceManagerService: ComplianceManagerService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize dependencies
        secureStorageManager = SecureStorageManager(requireContext())
        userManager = UserManager(requireContext())
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
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Sign In Button (existing users)
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

        // Optional: Forgot Password
        binding.tvForgotPassword?.setOnClickListener {
            // TODO: Implement forgot password flow
            Toast.makeText(requireContext(), "Forgot password feature coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    // ====== EXISTING USER LOGIN ======

    private fun performLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter email and password", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                // TODO: Implement Firebase login
                println("📧 Attempting login for: $email")

                // For now, simulate successful login
                Toast.makeText(requireContext(), "Login successful", Toast.LENGTH_SHORT).show()
                navigateToCalculatorList("Existing user login successful")

            } catch (e: Exception) {
                println("❌ Login error: ${e.message}")
                Toast.makeText(requireContext(), "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ====== NEW USER REGISTRATION FLOW ======

    private fun startRegistrationFlow() {
        println("🆕 Starting registration flow with compliance")
        showRegistrationCompliance()
    }

    private fun showRegistrationCompliance() {
        // Show medical disclaimer first for registration
        val enhancedDisclaimer = EnhancedMedicalDisclaimerDialogFragment.newInstance()

        enhancedDisclaimer.setOnAcceptedListener {
            // After disclaimer accepted, show professional verification for registration
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
            // Navigate to Firebase registration with professional data
            navigateToFirebaseRegistration(professionalType, licenseInfo, true)
        }

        professionalDialog.setOnSkippedListener {
            println("✅ General user registration")
            // Navigate to Firebase registration as general user
            navigateToFirebaseRegistration("General User", null, false)
        }

        professionalDialog.show(parentFragmentManager, ProfessionalVerificationDialogFragment.TAG)
    }

    // ====== GUEST MODE FLOW ======

    private fun startGuestFlow() {
        println("👤 Starting guest flow with compliance")
        showGuestCompliance()
    }

    private fun showGuestCompliance() {
        // Show medical disclaimer first for guest mode
        val enhancedDisclaimer = EnhancedMedicalDisclaimerDialogFragment.newInstance()

        enhancedDisclaimer.setOnAcceptedListener {
            // After disclaimer accepted, show professional verification for guest
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
            // Start guest session with professional status
            startGuestSession(true, professionalType, licenseInfo)
        }

        professionalDialog.setOnSkippedListener {
            println("✅ General guest access")
            // Start guest session with general status
            startGuestSession(false, "General User", null)
        }

        professionalDialog.show(parentFragmentManager, ProfessionalVerificationDialogFragment.TAG)
    }

    // ====== NAVIGATION METHODS ====== (Updated)

    private fun navigateToFirebaseRegistration(professionalType: String, licenseInfo: String?, isProfessional: Boolean) {
        lifecycleScope.launch {
            try {
                println("📝 Navigating to registration screen for: $professionalType")

                // Store the professional verification data to pass to RegisterFragment
                // You can use SharedPreferences, Bundle, or ViewModel to pass this data
                //storeRegistrationData(professionalType, licenseInfo, isProfessional)

                // Navigate to the actual RegisterFragment
                findNavController().navigate(R.id.action_loginFragment_to_registerFragment)

            } catch (e: Exception) {
                println("❌ Navigation error: ${e.message}")
                Toast.makeText(requireContext(), "Navigation error occurred", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Helper method to store registration data for RegisterFragment
//    private fun storeRegistrationData(professionalType: String, licenseInfo: String?, isProfessional: Boolean) {
//        val sharedPrefs = requireContext().getSharedPreferences("registration_temp", Context.MODE_PRIVATE)
//        sharedPrefs.edit().apply {
//            putString("professional_type", professionalType)
//            putString("license_info", licenseInfo)
//            putBoolean("is_professional", isProfessional)
//            apply()
//        }
//    }


    private fun startGuestSession(isProfessional: Boolean, professionalType: String, licenseInfo: String?) {
        lifecycleScope.launch {
            try {
                println("👤 Starting guest session - Professional: $isProfessional, Type: $professionalType")

                // Record compliance for guest user
                val success = complianceManagerService.recordCompleteCompliance(
                    professionalType = if (isProfessional) mapToProfessionalType(professionalType) else com.example.medicalcalculatorapp.domain.service.SimpleProfessionalType.OTHER,
                    licenseInfo = licenseInfo
                )

                if (success) {
                    Toast.makeText(requireContext(), "Guest access granted", Toast.LENGTH_SHORT).show()
                    navigateToCalculatorList("Guest session started")
                } else {
                    Toast.makeText(requireContext(), "Failed to start guest session", Toast.LENGTH_SHORT).show()
                }

            } catch (e: Exception) {
                println("❌ Guest session error: ${e.message}")
                Toast.makeText(requireContext(), "Failed to start guest session: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun navigateToCalculatorList(reason: String) {
        try {
            println("🧮 Navigating to calculator list: $reason")
            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
        } catch (e: Exception) {
            println("❌ Navigation error: ${e.message}")
            Toast.makeText(requireContext(), "Navigation error occurred", Toast.LENGTH_SHORT).show()
        }
    }

    // ====== HELPER METHODS ======

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


