package com.example.medicalcalculatorapp.data.user

import android.content.Context
import com.example.medicalcalculatorapp.util.SecureStorageManager
import java.util.UUID
import com.example.medicalcalculatorapp.data.auth.FirebaseAuthService
import com.google.firebase.auth.FirebaseUser

/**
 * Enhanced UserManager that supports both authenticated users and guest sessions.
 * Guest users have limited functionality and temporary sessions.
 */
class UserManager(context: Context) {

    private val secureStorageManager = SecureStorageManager(context)
    private val firebaseAuthService = FirebaseAuthService()
    // In-memory guest session storage (cleared on app restart)
    private var currentGuestSession: GuestSession? = null

    /**
     * Get the current user ID - could be authenticated user or guest session
     */
//    fun getCurrentUserId(): String {
//        return when {
//            isGuestMode() -> currentGuestSession?.guestId ?: createGuestSession()
//            else -> getAuthenticatedUserId()
//        }
//    }

    /**
     * Get the current user ID - Firebase user ID for authenticated users, guest ID for guests
     */
    fun getCurrentUserId(): String {
        return when {
            // Priority 1: Firebase authenticated user
            firebaseAuthService.isUserLoggedIn() -> {
                firebaseAuthService.getCurrentUserId() ?: createGuestSession()
            }
            // Priority 2: Guest session
            isGuestMode() -> currentGuestSession?.guestId ?: createGuestSession()
            // Fallback: Create new guest session
            else -> createGuestSession()
        }
    }

    /**
     * Get authenticated user ID from secure storage
     */
    private fun getAuthenticatedUserId(): String {
        return secureStorageManager.getEmail() ?: DEFAULT_USER_ID
    }

    /**
     * Check if currently in guest mode
     */
    fun isGuestMode(): Boolean {
        return currentGuestSession != null && !hasAuthenticatedUser()
    }

    /**
     * Check if user is authenticated (has valid credentials)
     */
    fun hasAuthenticatedUser(): Boolean {
        return firebaseAuthService.isUserLoggedIn()
    }

    /**
     * Start a guest session - creates temporary guest ID
     */
    fun startGuestSession(): String {
        if (currentGuestSession == null) {
            currentGuestSession = createGuestSessionObject()
        }
        return currentGuestSession!!.guestId
    }

    /**
     * End guest session and clear temporary data
     */
    fun endGuestSession() {
        currentGuestSession = null
    }

    /**
     * Convert guest session to authenticated user
     * @param email User's email for account creation
     * @return true if conversion successful
     */
    fun convertGuestToAuthenticatedUser(email: String): Boolean {
        return try {
            if (isGuestMode()) {
                // Save user credentials
                secureStorageManager.saveEmail(email)

                // Keep guest session temporarily for data migration
                // (The calling code should handle migrating favorites/history)

                true
            } else {
                false // Already authenticated
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get guest session info (for debugging/analytics)
     */
    fun getGuestSessionInfo(): GuestSession? {
        return currentGuestSession
    }

    /**
     * Clear all user data (for sign out)
     */
    fun signOut() {
        // Sign out from Firebase
        firebaseAuthService.signOut()

        // Clear local storage
        secureStorageManager.clearCredentials()

        // End guest session
        endGuestSession()
    }

    /**
     * Get user type for UI/feature decisions
     */
    fun getUserType(): UserType {
        return when {
            firebaseAuthService.isUserLoggedIn() -> UserType.AUTHENTICATED
            isGuestMode() -> UserType.GUEST
            else -> UserType.ANONYMOUS
        }
    }

    /**
     * Check if user can access premium features
     */
    fun canAccessPremiumFeatures(): Boolean {
        return getUserType() == UserType.AUTHENTICATED
    }

    /**
     * Check if user can save data (history, favorites)
     */
    fun canSaveUserData(): Boolean {
        return getUserType() == UserType.AUTHENTICATED
    }

    /**
     * Get display name for current user
     */
    fun getUserDisplayName(): String {
        return when (getUserType()) {
            UserType.AUTHENTICATED -> {
                val firebaseUser = firebaseAuthService.getCurrentUser()
                firebaseUser?.displayName
                    ?: firebaseUser?.email?.substringBefore("@")
                    ?: "Usuario"
            }
            UserType.GUEST -> "Usuario Invitado"
            UserType.ANONYMOUS -> "Usuario"
        }
    }

    /**
     * Get session duration for guest users (for analytics)
     */
    fun getGuestSessionDuration(): Long {
        return currentGuestSession?.let {
            System.currentTimeMillis() - it.startTime
        } ?: 0L
    }

    // Private helper methods

    private fun createGuestSession(): String {
        currentGuestSession = createGuestSessionObject()
        return currentGuestSession!!.guestId
    }

    private fun createGuestSessionObject(): GuestSession {
        return GuestSession(
            guestId = "guest_${UUID.randomUUID().toString().take(8)}",
            startTime = System.currentTimeMillis(),
            sessionId = UUID.randomUUID().toString()
        )
    }

    /**
     * Data class for guest session management
     */
    data class GuestSession(
        val guestId: String,
        val startTime: Long,
        val sessionId: String,
        val calculationsPerformed: Int = 0
    )

    /**
     * Enum for different user types
     */
    enum class UserType {
        ANONYMOUS,      // No session started
        GUEST,          // Guest session active
        AUTHENTICATED   // Logged in user
    }

    /**
     * Get Firebase user object (for authenticated users only)
     */
    fun getFirebaseUser(): FirebaseUser? {
        return if (hasAuthenticatedUser()) {
            firebaseAuthService.getCurrentUser()
        } else {
            null
        }
    }

    /**
     * Check if current user's email is verified
     */
    fun isEmailVerified(): Boolean {
        return firebaseAuthService.isEmailVerified()
    }

    companion object {
        const val DEFAULT_USER_ID = "default_user"
        const val GUEST_PREFIX = "guest_"

        // Guest session limits (optional - for rate limiting)
        const val MAX_GUEST_CALCULATIONS_PER_SESSION = 50
        const val GUEST_SESSION_TIMEOUT_HOURS = 24
    }
}