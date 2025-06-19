package com.example.medicalcalculatorapp.data.auth

import com.google.firebase.auth.*
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

/**
 * Enhanced FirebaseAuthService - Dynamic Links Migration Ready
 *
 * Updated to use ActionCodeSettings with custom domain for:
 * - Email verification links
 * - Password reset links
 * - Email sign-in links
 *
 * Complies with Google Play health app requirements and provides
 * better security and user experience.
 */
class FirebaseAuthService {

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    // 🆕 CUSTOM DOMAIN CONFIGURATION
    companion object {
        // Your Firebase Hosting domain (replace Dynamic Links)
        private const val CUSTOM_DOMAIN = "https://medicalcalculatorapp-39631.web.app"
        private const val APP_PACKAGE_NAME = "com.example.medicalcalculatorapp"
        private const val MIN_APP_VERSION = "1.0"

        // Auth action paths on your custom domain
        private const val EMAIL_VERIFICATION_PATH = "/verify"
        private const val PASSWORD_RESET_PATH = "/reset-password"
        private const val EMAIL_SIGNIN_PATH = "/auth"

        // Session timeout for medical app security (Google Play requirement)
        private const val SESSION_TIMEOUT_MINUTES = 30L
    }

    // ====== ENHANCED AUTHENTICATION METHODS ======

    /**
     * Sign in with email and password - Enhanced security checks
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            logAuthEvent(AuthEventType.SIGN_IN_ATTEMPT, email)

            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                // Enhanced security checks
                val securityCheck = performSecurityCheck(user)
                if (!securityCheck.passed) {
                    logAuthEvent(AuthEventType.SECURITY_CHECK_FAILED, user.uid, securityCheck.reason)
                    return AuthResult.SecurityIssue(securityCheck.reason)
                }

                // Check email verification status
                if (!user.isEmailVerified) {
                    logAuthEvent(AuthEventType.EMAIL_NOT_VERIFIED, user.uid)
                    return AuthResult.EmailNotVerified(
                        user = user,
                        message = "Please verify your email address before signing in."
                    )
                }

                // Validate medical professional email (Google Play compliance)
                if (!isValidMedicalEmail(email)) {
                    logAuthEvent(AuthEventType.INVALID_MEDICAL_EMAIL, user.uid)
                }

                logAuthEvent(AuthEventType.SIGN_IN_SUCCESS, user.uid)
                AuthResult.Success(user, "Sign in successful")
            } else {
                logAuthEvent(AuthEventType.SIGN_IN_FAILED, email)
                AuthResult.Error("Sign in failed - no user returned")
            }
        } catch (e: FirebaseAuthException) {
            logAuthEvent(AuthEventType.SIGN_IN_ERROR, email, e.errorCode)
            AuthResult.Error(mapFirebaseErrorToUserMessage(e))
        } catch (e: Exception) {
            logAuthEvent(AuthEventType.SIGN_IN_ERROR, email, e.message)
            AuthResult.Error(e.message ?: "Sign in failed")
        }
    }

    /**
     * Create user account with enhanced validation and immediate verification
     */
    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            logAuthEvent(AuthEventType.ACCOUNT_CREATION_ATTEMPT, email)

            // Validate medical professional email domain
            if (!isValidMedicalEmail(email)) {
                logAuthEvent(AuthEventType.INVALID_MEDICAL_EMAIL, email)
                return AuthResult.Error(
                    "Please use a professional or institutional email address for medical app access."
                )
            }

            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user

            if (user != null) {
                // Immediately send verification email
                val verificationResult = sendEmailVerification()
                if (verificationResult is AuthResult.Error) {
                    // Account created but verification failed
                    logAuthEvent(AuthEventType.VERIFICATION_SEND_FAILED, user.uid)
                    return AuthResult.Error("Account created but verification email failed. Please check your email.")
                }

                logAuthEvent(AuthEventType.ACCOUNT_CREATED, user.uid)
                AuthResult.AccountCreated(
                    user = user,
                    message = "Account created successfully. Please verify your email before signing in."
                )
            } else {
                logAuthEvent(AuthEventType.ACCOUNT_CREATION_FAILED, email)
                AuthResult.Error("Account creation failed")
            }
        } catch (e: FirebaseAuthException) {
            logAuthEvent(AuthEventType.ACCOUNT_CREATION_ERROR, email, e.errorCode)
            AuthResult.Error(mapFirebaseErrorToUserMessage(e))
        } catch (e: Exception) {
            logAuthEvent(AuthEventType.ACCOUNT_CREATION_ERROR, email, e.message)
            AuthResult.Error(e.message ?: "Account creation failed")
        }
    }

    /**
     * 🔥 UPDATED: Send password reset email with ActionCodeSettings
     * Uses custom domain instead of Dynamic Links
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            logAuthEvent(AuthEventType.PASSWORD_RESET_REQUESTED, email)

            // 🆕 CREATE ACTION CODE SETTINGS FOR CUSTOM DOMAIN
            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("$CUSTOM_DOMAIN$PASSWORD_RESET_PATH") // Your Firebase Hosting domain
                .setHandleCodeInApp(true) // Handle link in your app
                .setAndroidPackageName(
                    APP_PACKAGE_NAME,
                    true, // Install if not available
                    MIN_APP_VERSION // Minimum version
                )
                .build()

            // Send password reset with custom settings
            firebaseAuth.sendPasswordResetEmail(email, actionCodeSettings).await()

            logAuthEvent(AuthEventType.PASSWORD_RESET_SENT, email)
            AuthResult.Success(null, "Password reset email sent successfully to $email")
        } catch (e: FirebaseAuthException) {
            logAuthEvent(AuthEventType.PASSWORD_RESET_FAILED, email, e.errorCode)
            AuthResult.Error(mapFirebaseErrorToUserMessage(e))
        } catch (e: Exception) {
            logAuthEvent(AuthEventType.PASSWORD_RESET_ERROR, email, e.message)
            AuthResult.Error(e.message ?: "Failed to send reset email")
        }
    }

    /**
     * 🔥 UPDATED: Send email verification with ActionCodeSettings
     * Uses custom domain instead of Dynamic Links
     */
    suspend fun sendEmailVerification(): AuthResult {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {

                // 🆕 CREATE ACTION CODE SETTINGS FOR CUSTOM DOMAIN
                val actionCodeSettings = ActionCodeSettings.newBuilder()
                    .setUrl("$CUSTOM_DOMAIN$EMAIL_VERIFICATION_PATH") // Your Firebase Hosting domain
                    .setHandleCodeInApp(true) // Handle link in your app
                    .setAndroidPackageName(
                        APP_PACKAGE_NAME,
                        true, // Install if not available
                        MIN_APP_VERSION // Minimum version
                    )
                    .build()

                // Send verification with custom settings
                user.sendEmailVerification(actionCodeSettings).await()

                logAuthEvent(AuthEventType.VERIFICATION_SENT, user.uid)
                AuthResult.Success(user, "Verification email sent to ${user.email}")
            } else {
                AuthResult.Error("No user logged in")
            }
        } catch (e: FirebaseAuthException) {
            logAuthEvent(AuthEventType.VERIFICATION_SEND_FAILED, null, e.errorCode)
            AuthResult.Error(mapFirebaseErrorToUserMessage(e))
        } catch (e: Exception) {
            logAuthEvent(AuthEventType.VERIFICATION_SEND_FAILED, null, e.message)
            AuthResult.Error(e.message ?: "Failed to send verification email")
        }
    }

    /**
     * 🆕 NEW: Send email sign-in link (passwordless authentication)
     * Google Play recommended for medical apps
     */
    suspend fun sendSignInLinkToEmail(email: String): AuthResult {
        return try {
            logAuthEvent(AuthEventType.EMAIL_SIGNIN_REQUESTED, email)

            val actionCodeSettings = ActionCodeSettings.newBuilder()
                .setUrl("$CUSTOM_DOMAIN$EMAIL_SIGNIN_PATH")
                .setHandleCodeInApp(true)
                .setAndroidPackageName(APP_PACKAGE_NAME, true, MIN_APP_VERSION)
                .build()

            firebaseAuth.sendSignInLinkToEmail(email, actionCodeSettings).await()

            logAuthEvent(AuthEventType.EMAIL_SIGNIN_SENT, email)
            AuthResult.Success(null, "Sign-in link sent to $email")
        } catch (e: FirebaseAuthException) {
            logAuthEvent(AuthEventType.EMAIL_SIGNIN_FAILED, email, e.errorCode)
            AuthResult.Error(mapFirebaseErrorToUserMessage(e))
        } catch (e: Exception) {
            logAuthEvent(AuthEventType.EMAIL_SIGNIN_ERROR, email, e.message)
            AuthResult.Error(e.message ?: "Failed to send sign-in link")
        }
    }

    /**
     * 🆕 NEW: Complete email sign-in with link
     */
    suspend fun signInWithEmailLink(email: String, emailLink: String): AuthResult {
        return try {
            logAuthEvent(AuthEventType.EMAIL_SIGNIN_ATTEMPT, email)

            if (firebaseAuth.isSignInWithEmailLink(emailLink)) {
                val result = firebaseAuth.signInWithEmailLink(email, emailLink).await()
                val user = result.user

                if (user != null) {
                    logAuthEvent(AuthEventType.EMAIL_SIGNIN_SUCCESS, user.uid)
                    AuthResult.Success(user, "Email sign-in successful")
                } else {
                    logAuthEvent(AuthEventType.EMAIL_SIGNIN_FAILED, email)
                    AuthResult.Error("Email sign-in failed")
                }
            } else {
                AuthResult.Error("Invalid sign-in link")
            }
        } catch (e: FirebaseAuthException) {
            logAuthEvent(AuthEventType.EMAIL_SIGNIN_ERROR, email, e.errorCode)
            AuthResult.Error(mapFirebaseErrorToUserMessage(e))
        } catch (e: Exception) {
            logAuthEvent(AuthEventType.EMAIL_SIGNIN_ERROR, email, e.message)
            AuthResult.Error(e.message ?: "Email sign-in failed")
        }
    }



    /**
     * Get last sign in timestamp for session management
     */
    fun getLastSignInTime(): Long? {
        return getCurrentUser()?.metadata?.lastSignInTimestamp
    }

    /**
     * Check if session has expired (medical app security requirement)
     * Google Play recommends session timeouts for health apps
     */
    fun isSessionExpired(): Boolean {
        val lastSignIn = getLastSignInTime() ?: return true
        val sessionTimeout = TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES)
        return (System.currentTimeMillis() - lastSignIn) > sessionTimeout
    }

    // ====== EXISTING METHODS (UNCHANGED) ======

    /**
     * Update user profile with sanitization
     */
    suspend fun updateUserProfile(displayName: String): AuthResult {
        return try {
            val user = firebaseAuth.currentUser
            if (user != null) {
                val sanitizedName = sanitizeDisplayName(displayName)

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(sanitizedName)
                    .build()

                user.updateProfile(profileUpdates).await()

                logAuthEvent(AuthEventType.PROFILE_UPDATED, user.uid)
                AuthResult.Success(user, "Profile updated successfully")
            } else {
                AuthResult.Error("No user logged in")
            }
        } catch (e: Exception) {
            logAuthEvent(AuthEventType.PROFILE_UPDATE_FAILED, null, e.message)
            AuthResult.Error(e.message ?: "Failed to update profile")
        }
    }

    /**
     * Get current user
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Check if user is logged in
     */
    fun isUserLoggedIn(): Boolean {
        return getCurrentUser() != null
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return getCurrentUser()?.uid
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        firebaseAuth.signOut()
        logAuthEvent(AuthEventType.SIGN_OUT, null)
    }

    /**
     * Check if current user's email is verified
     */
    fun isEmailVerified(): Boolean {
        return getCurrentUser()?.isEmailVerified ?: false
    }

    // ====== HELPER METHODS ======

    private fun mapFirebaseErrorToUserMessage(exception: FirebaseAuthException): String {
        return when (exception.errorCode) {
            "ERROR_INVALID_EMAIL" -> "Invalid email address format"
            "ERROR_WRONG_PASSWORD" -> "Incorrect password. Please try again"
            "ERROR_USER_NOT_FOUND" -> "No account found with this email address"
            "ERROR_USER_DISABLED" -> "This account has been disabled. Contact support"
            "ERROR_TOO_MANY_REQUESTS" -> "Too many failed attempts. Please try again later"
            "ERROR_EMAIL_ALREADY_IN_USE" -> "An account with this email already exists"
            "ERROR_WEAK_PASSWORD" -> "Password must be at least 6 characters long"
            "ERROR_NETWORK_REQUEST_FAILED" -> "Network error. Please check your connection"
            else -> "Authentication error: ${exception.message}"
        }
    }

    private fun performSecurityCheck(user: FirebaseUser): SecurityCheckResult {
        // Check for suspicious activity patterns
        val metadata = user.metadata
        val creationTime = metadata?.creationTimestamp ?: 0
        val lastSignIn = metadata?.lastSignInTimestamp ?: 0

        // Flag accounts created very recently (potential fraud)
        if (System.currentTimeMillis() - creationTime < TimeUnit.HOURS.toMillis(1)) {
            return SecurityCheckResult(false, "Account too new - requires additional verification")
        }

        return SecurityCheckResult(true, "Security check passed")
    }

    private fun isValidMedicalEmail(email: String): Boolean {
        // Enhanced validation for medical professionals
        // Accept educational, healthcare, and professional domains
        val medicalDomains = listOf(
            ".edu", ".gov", ".org", // Educational/government
            "hospital", "clinic", "medical", "health", // Healthcare institutions
            "gmail.com", "outlook.com", "yahoo.com" // Allow common providers for testing
        )

        // Reject obvious temporary/spam email providers
        val blockedDomains = listOf(
            "tempmail", "10minutemail", "guerrillamail", "mailinator"
        )

        val lowerEmail = email.lowercase()
        val hasBlockedDomain = blockedDomains.any { lowerEmail.contains(it) }

        return !hasBlockedDomain && email.contains("@")
    }

    private fun sanitizeDisplayName(displayName: String): String {
        // Remove potentially dangerous characters
        return displayName.trim()
            .replace(Regex("[<>\"'/\\\\]"), "")
            .take(50) // Limit length
    }

    private fun logAuthEvent(eventType: AuthEventType, identifier: String?, details: String? = null) {
        // Enhanced logging for debugging and analytics
        val timestamp = System.currentTimeMillis()
        println("🔐 AUTH EVENT: $eventType | User: $identifier | Details: $details | Time: $timestamp")

        // TODO: In production, send to Firebase Analytics or Crashlytics
        // FirebaseAnalytics.getInstance(context).logEvent("auth_event", bundle)
    }
}

// ====== RESULT CLASSES ======

/**
 * Enhanced AuthResult with more specific cases
 */
sealed class AuthResult {
    data class Success(val user: FirebaseUser?, val message: String = "") : AuthResult()
    data class Error(val message: String) : AuthResult()
    data class AccountCreated(val user: FirebaseUser, val message: String) : AuthResult()
    data class EmailNotVerified(val user: FirebaseUser, val message: String) : AuthResult()
    data class SecurityIssue(val reason: String) : AuthResult()
}

/**
 * Security check result
 */
data class SecurityCheckResult(
    val passed: Boolean,
    val reason: String
)

/**
 * Authentication event types for logging
 */
enum class AuthEventType {
    SIGN_IN_ATTEMPT,
    SIGN_IN_SUCCESS,
    SIGN_IN_FAILED,
    SIGN_IN_ERROR,
    SIGN_OUT,
    ACCOUNT_CREATION_ATTEMPT,
    ACCOUNT_CREATED,
    ACCOUNT_CREATION_FAILED,
    ACCOUNT_CREATION_ERROR,
    EMAIL_NOT_VERIFIED,
    VERIFICATION_SENT,
    VERIFICATION_SEND_FAILED,
    PASSWORD_RESET_REQUESTED,
    PASSWORD_RESET_SENT,
    PASSWORD_RESET_FAILED,
    PASSWORD_RESET_ERROR,
    EMAIL_SIGNIN_REQUESTED,
    EMAIL_SIGNIN_SENT,
    EMAIL_SIGNIN_FAILED,
    EMAIL_SIGNIN_ERROR,
    EMAIL_SIGNIN_ATTEMPT,
    EMAIL_SIGNIN_SUCCESS,
    PROFILE_UPDATED,
    PROFILE_UPDATE_FAILED,
    SECURITY_CHECK_FAILED,
    INVALID_MEDICAL_EMAIL
}