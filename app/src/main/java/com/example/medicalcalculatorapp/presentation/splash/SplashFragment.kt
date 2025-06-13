// Replace your existing app/src/main/java/com/example/medicalcalculatorapp/presentation/splash/SplashFragment.kt

package com.example.medicalcalculatorapp.presentation.splash

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.FragmentSplashBinding
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.util.SecureStorageManager
import com.example.medicalcalculatorapp.util.MedicalComplianceManager
import com.example.medicalcalculatorapp.domain.model.DisclaimerFlow  // ✅ Correct location

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private lateinit var userManager: UserManager
    private lateinit var secureStorageManager: SecureStorageManager
    private lateinit var complianceManager: MedicalComplianceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userManager = UserManager(requireContext())
        secureStorageManager = SecureStorageManager(requireContext())
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
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Start the enhanced authentication/session check after splash delay
        Handler(Looper.getMainLooper()).postDelayed({
            checkComplianceAndNavigate()
        }, 2000) // 2 seconds splash
    }

    private fun checkComplianceAndNavigate() {
        try {
            println("🔍 SplashFragment: Starting enhanced compliance check...")

            // Get current compliance status
            val complianceStatus = complianceManager.getComplianceStatus()
            val requiredFlow = complianceStatus.requiredFlow

            println("📊 Compliance Status: ${complianceStatus.generateComplianceReport()}")

            // Navigate based on compliance requirements and user session
            when {
                // Case 1: User is fully compliant and has valid session
                requiredFlow == DisclaimerFlow.FULLY_COMPLIANT && hasValidUserSession() -> {
                    println("✅ User fully compliant with valid session")
                    navigateToCalculatorList("User fully compliant - direct access granted")
                }

                // Case 2: User needs basic introduction (new user)
                requiredFlow == DisclaimerFlow.BASIC_INTRODUCTION -> {
                    println("🆕 New user - showing basic introduction")
                    navigateToLogin("New user needs basic introduction")
                }

                // Case 3: User has basic disclaimer but needs enhanced for medical access
                requiredFlow == DisclaimerFlow.ENHANCED_MEDICAL_REQUIRED -> {
                    println("🏥 Enhanced medical disclaimer required")
                    showEnhancedDisclaimerForMedicalAccess()
                }

                // Case 4: Professional verification required
                requiredFlow == DisclaimerFlow.PROFESSIONAL_VERIFICATION_REQUIRED -> {
                    println("👨‍⚕️ Professional verification required")
                    showProfessionalVerificationFlow()
                }

                // Case 5: Compliance update required
                requiredFlow == DisclaimerFlow.COMPLIANCE_UPDATE_REQUIRED -> {
                    println("📋 Compliance update required")
                    showComplianceUpdateDialog()
                }

                // Case 6: User has some compliance but session issues
                hasComplianceIssues() -> {
                    println("⚠️ Compliance issues detected")
                    handleComplianceIssues()
                }

                // Default: Go to login for safe fallback
                else -> {
                    println("🔐 Default fallback to login")
                    navigateToLogin("Default fallback - compliance check")
                }
            }
        } catch (e: Exception) {
            println("❌ Error during compliance check: ${e.message}")
            e.printStackTrace()
            // Safe fallback to login on any error
            navigateToLogin("Error during compliance check: ${e.message}")
        }
    }

    /**
     * Check if user has a valid session (authenticated or guest)
     */
    private fun hasValidUserSession(): Boolean {
        return when {
            // Case 1: Authenticated user with valid credentials
            userManager.hasAuthenticatedUser() -> {
                println("✅ Found authenticated user")
                true
            }

            // Case 2: Valid guest session
            hasValidGuestSession() -> {
                println("✅ Found valid guest session")
                restoreGuestSession()
                true
            }

            // Case 3: No valid session
            else -> {
                println("❌ No valid user session found")
                false
            }
        }
    }

    /**
     * Show enhanced medical disclaimer for users who need medical calculator access
     */
    private fun showEnhancedDisclaimerForMedicalAccess() {
        try {
            val enhancedDisclaimer = com.example.medicalcalculatorapp.presentation.auth.EnhancedMedicalDisclaimerDialogFragment.newInstance()

            enhancedDisclaimer.setOnAcceptedListener {
                // User accepted enhanced disclaimer
                complianceManager.markEnhancedDisclaimerAccepted()
                complianceManager.markProfessionalVerified() // Combined verification

                println("✅ Enhanced disclaimer accepted")
                navigateToCalculatorList("Enhanced disclaimer accepted")
            }

            enhancedDisclaimer.setOnRejectedListener {
                // User rejected - go to login for alternative options
                println("❌ Enhanced disclaimer rejected")
                navigateToLogin("Enhanced disclaimer rejected")
            }

            enhancedDisclaimer.show(parentFragmentManager, com.example.medicalcalculatorapp.presentation.auth.EnhancedMedicalDisclaimerDialogFragment.TAG)

        } catch (e: Exception) {
            println("❌ Error showing enhanced disclaimer: ${e.message}")
            navigateToLogin("Error showing enhanced disclaimer")
        }
    }

    /**
     * Show professional verification flow
     */
    private fun showProfessionalVerificationFlow() {
        // For now, use the enhanced disclaimer as professional verification
        // In the future, this could be a separate professional credential check
        showEnhancedDisclaimerForMedicalAccess()
    }

    /**
     * Show compliance update dialog for policy changes
     */
    private fun showComplianceUpdateDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("📋 Actualización de Políticas")
            .setMessage(
                "Hemos actualizado nuestras políticas médicas para cumplir con los nuevos requisitos de Google Play Store.\n\n" +
                        "Por favor, revise y acepte los términos actualizados para continuar usando la aplicación."
            )
            .setPositiveButton("Revisar Términos") { _, _ ->
                showEnhancedDisclaimerForMedicalAccess()
            }
            .setNegativeButton("Más Tarde") { _, _ ->
                navigateToLogin("Compliance update postponed")
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Check for various compliance issues
     */
    private fun hasComplianceIssues(): Boolean {
        val status = complianceManager.getComplianceStatus()

        // Check for partial compliance scenarios
        return when {
            // User has basic but no enhanced disclaimer and trying to access medical features
            status.hasBasicDisclaimer && !status.hasEnhancedDisclaimer -> true

            // Professional verification missing
            status.hasEnhancedDisclaimer && !status.isProfessionalVerified -> true

            // Compliance version mismatch
            status.complianceVersion.isNotEmpty() &&
                    status.complianceVersion != "2024.1" -> true

            else -> false
        }
    }

    /**
     * Handle various compliance issues
     */
    private fun handleComplianceIssues() {
        val status = complianceManager.getComplianceStatus()

        when {
            !status.hasEnhancedDisclaimer -> {
                println("🏥 Missing enhanced disclaimer")
                showEnhancedDisclaimerForMedicalAccess()
            }

            !status.isProfessionalVerified -> {
                println("👨‍⚕️ Missing professional verification")
                showProfessionalVerificationFlow()
            }

            else -> {
                println("📋 General compliance issue")
                showComplianceUpdateDialog()
            }
        }
    }

    // Existing methods (keep these unchanged)

    private fun hasValidGuestSession(): Boolean {
        val sessionStart = secureStorageManager.getGuestSessionStart()
        if (sessionStart == 0L) {
            println("🔍 No guest session start time found")
            return false
        }

        // Check if session is within timeout period (24 hours)
        val sessionAge = System.currentTimeMillis() - sessionStart
        val maxSessionAge = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
        val isValid = sessionAge < maxSessionAge

        println("🔍 Guest session age: ${sessionAge / (60 * 1000)} minutes, valid: $isValid")
        return isValid
    }

    private fun restoreGuestSession() {
        try {
            println("🔄 Restoring guest session...")
            userManager.startGuestSession()
            secureStorageManager.incrementGuestModeUsage()
            println("✅ Guest session restored successfully")
        } catch (e: Exception) {
            println("❌ Error restoring guest session: ${e.message}")
            clearExpiredGuestSession()
            throw e
        }
    }

    private fun clearExpiredGuestSession() {
        try {
            println("🧹 Clearing expired guest session...")
            userManager.endGuestSession()
            secureStorageManager.clearGuestSession()
            println("✅ Expired guest session cleared")
        } catch (e: Exception) {
            println("❌ Error clearing guest session: ${e.message}")
        }
    }

    private fun navigateToCalculatorList(reason: String) {
        println("🚀 Navigating to Calculator List: $reason")
        try {
            findNavController().navigate(
                R.id.action_splashFragment_to_calculatorListFragment
            )
        } catch (e: Exception) {
            println("❌ Navigation error to calculator list: ${e.message}")
            navigateToLogin("Navigation error occurred")
        }
    }

    private fun navigateToLogin(reason: String) {
        println("🔐 Navigating to Login: $reason")
        try {
            findNavController().navigate(
                R.id.action_splashFragment_to_loginFragment
            )
        } catch (e: Exception) {
            println("❌ Critical navigation error to login: ${e.message}")
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
