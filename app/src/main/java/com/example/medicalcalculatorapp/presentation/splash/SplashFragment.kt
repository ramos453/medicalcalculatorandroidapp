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
import com.example.medicalcalculatorapp.di.AppDependencies

class SplashFragment : Fragment() {

    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!

    private lateinit var userManager: UserManager
    private lateinit var secureStorageManager: SecureStorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userManager = AppDependencies.provideUserManager(requireContext())
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
                hasValidAuthenticatedSession() -> {
                    println("✅ Found authenticated user with disclaimer")
                    navigateToCalculatorList("Authenticated user session restored")
                }

                // Case 2: User has valid guest session and disclaimer accepted
                hasValidGuestSession() -> {
                    println("✅ Found valid guest session with disclaimer")
                    restoreGuestSession()
                    navigateToCalculatorList("Guest session restored")
                }

                // Case 3: User had guest session but it expired
                hadPreviousGuestSession() -> {
                    println("⚠️ Previous guest session expired")
                    clearExpiredGuestSession()
                    navigateToLogin("Guest session expired - need new session")
                }

                // Case 4: First time user or clean state
                else -> {
                    println("🆕 New user or clean state")
                    navigateToLogin("New session required")
                }
            }
        } catch (e: Exception) {
            println("❌ Error during session check: ${e.message}")
            e.printStackTrace()
            // On any error, safely navigate to login
            navigateToLogin("Error during session check - safe fallback")
        }
    }

    // AUTHENTICATED USER SESSION VALIDATION
    private fun hasValidAuthenticatedSession(): Boolean {
        val hasAuthenticatedUser = userManager.hasAuthenticatedUser()
        val hasAcceptedDisclaimer = secureStorageManager.isDisclaimerAccepted()

        println("🔍 Authenticated check: hasUser=$hasAuthenticatedUser, hasDisclaimer=$hasAcceptedDisclaimer")
        return hasAuthenticatedUser && hasAcceptedDisclaimer
    }

    // GUEST SESSION VALIDATION
    private fun hasValidGuestSession(): Boolean {
        // Check if guest disclaimer was accepted
        val hasGuestDisclaimer = secureStorageManager.isGuestDisclaimerAccepted()
        if (!hasGuestDisclaimer) {
            println("🔍 No guest disclaimer found")
            return false
        }

        // Check if session start time exists
        val sessionStart = secureStorageManager.getGuestSessionStart()
        if (sessionStart == 0L) {
            println("🔍 No guest session start time found")
            return false
        }

        // Check if session is within timeout period (24 hours)
        val sessionAge = System.currentTimeMillis() - sessionStart
        val maxSessionAge = 24 * 60 * 60 * 1000L // 24 hours in milliseconds
        val isWithinTimeout = sessionAge < maxSessionAge

        // Check if we're approaching timeout (warn if less than 2 hours remaining)
        val timeRemaining = maxSessionAge - sessionAge
        val isNearTimeout = timeRemaining < (2 * 60 * 60 * 1000L) // Less than 2 hours

        println("🔍 Guest session: age=${sessionAge / (60 * 1000)}min, valid=$isWithinTimeout, nearTimeout=$isNearTimeout")

        if (isWithinTimeout && isNearTimeout) {
            println("⚠️ Guest session is near expiry")
            // Could add warning logic here in the future
        }

        return isWithinTimeout
    }

    private fun hadPreviousGuestSession(): Boolean {
        val hadSessionStart = secureStorageManager.getGuestSessionStart() > 0L
        val hadGuestUsage = secureStorageManager.getGuestModeUsageCount() > 0
        val hadGuestDisclaimer = secureStorageManager.isGuestDisclaimerAccepted()

        val hadPreviousSession = hadSessionStart || hadGuestUsage || hadGuestDisclaimer
        println("🔍 Previous guest session check: start=$hadSessionStart, usage=$hadGuestUsage, disclaimer=$hadGuestDisclaimer, result=$hadPreviousSession")

        return hadPreviousSession
    }

    // GUEST SESSION RESTORATION
    private fun restoreGuestSession() {
        try {
            println("🔄 Restoring guest session...")

            // Restore guest session in UserManager
            val guestId = userManager.startGuestSession()

            // Increment usage count for analytics
            secureStorageManager.incrementGuestModeUsage()

            println("✅ Guest session restored successfully with ID: $guestId")
        } catch (e: Exception) {
            println("❌ Error restoring guest session: ${e.message}")
            e.printStackTrace()

            // If restoration fails, clear guest data and go to login
            clearExpiredGuestSession()
            throw IllegalStateException("Failed to restore guest session", e)
        }
    }

    private fun clearExpiredGuestSession() {
        try {
            println("🧹 Clearing expired guest session...")

            // End current guest session if any
            userManager.endGuestSession()

            // Clear all guest-related storage
            secureStorageManager.clearGuestSession()

            println("✅ Expired guest session cleared successfully")
        } catch (e: Exception) {
            println("❌ Error clearing guest session: ${e.message}")
            e.printStackTrace()
            // Don't throw here - we want to continue to login even if cleanup fails
        }
    }

    // NAVIGATION METHODS
    private fun navigateToCalculatorList(reason: String) {
        println("🚀 Navigating to Calculator List: $reason")
        try {
            findNavController().navigate(
                R.id.action_splashFragment_to_calculatorListFragment
            )
        } catch (e: Exception) {
            println("❌ Navigation error to calculator list: ${e.message}")
            e.printStackTrace()
            // Fallback to login if navigation fails
            navigateToLogin("Navigation error to calculator list")
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
            e.printStackTrace()
            // This should not happen, but handle gracefully
            try {
                requireActivity().finish()
            } catch (activityError: Exception) {
                println("❌ Critical error - cannot even finish activity: ${activityError.message}")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}