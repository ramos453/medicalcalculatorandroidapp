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

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private lateinit var userManager: UserManager
    private lateinit var secureStorageManager: SecureStorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userManager = UserManager(requireContext())
        secureStorageManager = SecureStorageManager(requireContext())
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

        // Start the authentication/session check after splash delay
        Handler(Looper.getMainLooper()).postDelayed({
            checkUserSessionAndNavigate()
        }, 2000) // 2 seconds splash
    }

    private fun checkUserSessionAndNavigate() {
        try {
            println("🔍 SplashFragment: Starting session check...")

            when {
                // Case 1: User has authenticated account and disclaimer accepted
                userManager.hasAuthenticatedUser() && secureStorageManager.isDisclaimerAccepted() -> {
                    println("✅ Found authenticated user with disclaimer")
                    navigateToCalculatorList("Authenticated user session restored")
                }

                // Case 2: User has valid guest session and disclaimer accepted
                hasValidGuestSession() && secureStorageManager.isGuestDisclaimerAccepted() -> {
                    println("✅ Found valid guest session with disclaimer")
                    restoreGuestSession()
                    navigateToCalculatorList("Guest session restored")
                }

                // Case 3: User was in guest mode but session expired
                hadGuestSession() && !hasValidGuestSession() -> {
                    println("⚠️ Guest session expired")
                    clearExpiredGuestSession()
                    navigateToLogin("Guest session expired")
                }

                // Case 4: First time user or need to login
                else -> {
                    println("🆕 New session required")
                    navigateToLogin("New session required")
                }
            }
        } catch (e: Exception) {
            println("❌ Error during session check: ${e.message}")
            // On any error, go to login screen safely
            navigateToLogin("Error during session check: ${e.message}")
        }
    }

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

    private fun hadGuestSession(): Boolean {
        val hadSession = secureStorageManager.getGuestSessionStart() > 0L ||
                secureStorageManager.getGuestModeUsageCount() > 0
        println("🔍 Had previous guest session: $hadSession")
        return hadSession
    }

    private fun restoreGuestSession() {
        try {
            println("🔄 Restoring guest session...")

            // Restore guest session in UserManager
            userManager.startGuestSession()

            // Update session analytics
            secureStorageManager.incrementGuestModeUsage()

            println("✅ Guest session restored successfully")
        } catch (e: Exception) {
            println("❌ Error restoring guest session: ${e.message}")
            // If restoration fails, clear guest data and go to login
            clearExpiredGuestSession()
            throw e // Re-throw to trigger login navigation
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
            // Fallback to login if navigation fails
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
            // This should not happen, but handle gracefully
            requireActivity().finish()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}