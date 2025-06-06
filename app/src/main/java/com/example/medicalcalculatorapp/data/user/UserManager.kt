package com.example.medicalcalculatorapp.data.user

import android.content.Context
import com.example.medicalcalculatorapp.util.SecureStorageManager
import java.util.UUID

/**
 * Enhanced UserManager that supports both authenticated users and guest sessions.
 * Guest users have limited functionality and temporary sessions.
 */
class UserManager(context: Context) {

    private val secureStorageManager = SecureStorageManager(context)

    // In-memory guest session storage (cleared on app restart)
    private var currentGuestSession: GuestSession? = null

    /**
     * Get the current user ID - could be authenticated user or guest session
     */
    fun getCurrentUserId(): String {
        return when {
            isGuestMode() -> currentGuestSession?.guestId ?: createGuestSession()
            else -> getAuthenticatedUserId()
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
        val email = secureStorageManager.getEmail()
        return !email.isNullOrBlank()
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
        secureStorageManager.clearCredentials()
        endGuestSession()
    }

    /**
     * Get user type for UI/feature decisions
     */
    fun getUserType(): UserType {
        return when {
            isGuestMode() -> UserType.GUEST
            hasAuthenticatedUser() -> UserType.AUTHENTICATED
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
            UserType.GUEST -> "Usuario Invitado"
            UserType.AUTHENTICATED -> {
                val email = secureStorageManager.getEmail()
                email?.substringBefore("@") ?: "Usuario"
            }
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

    companion object {
        const val DEFAULT_USER_ID = "default_user"
        const val GUEST_PREFIX = "guest_"

        // Guest session limits (optional - for rate limiting)
        const val MAX_GUEST_CALCULATIONS_PER_SESSION = 50
        const val GUEST_SESSION_TIMEOUT_HOURS = 24
    }
}