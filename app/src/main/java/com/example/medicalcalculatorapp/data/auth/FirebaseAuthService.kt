package com.example.medicalcalculatorapp.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class FirebaseAuthService {

    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Get current authenticated user
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }

    /**
     * Check if user is currently logged in
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
     * Sign in with email and password
     */
    suspend fun signInWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                AuthResult.Success(result.user!!)
            } else {
                AuthResult.Error("Login failed - no user returned")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Login failed")
        }
    }

    /**
     * Create account with email and password
     */
    suspend fun createUserWithEmailAndPassword(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            if (result.user != null) {
                AuthResult.Success(result.user!!)
            } else {
                AuthResult.Error("Account creation failed")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Account creation failed")
        }
    }

    /**
     * Send password reset email
     */
    suspend fun sendPasswordResetEmail(email: String): AuthResult {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            AuthResult.Success(null)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send reset email")
        }
    }

    /**
     * Send email verification
     */
    suspend fun sendEmailVerification(): AuthResult {
        return try {
            val user = getCurrentUser()
            if (user != null) {
                user.sendEmailVerification().await()
                AuthResult.Success(user)
            } else {
                AuthResult.Error("No user logged in")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to send verification email")
        }
    }

    /**
     * Update user profile
     */
    suspend fun updateUserProfile(displayName: String): AuthResult {
        return try {
            val user = getCurrentUser()
            if (user != null) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .build()
                user.updateProfile(profileUpdates).await()
                AuthResult.Success(user)
            } else {
                AuthResult.Error("No user logged in")
            }
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Failed to update profile")
        }
    }

    /**
     * Sign out current user
     */
    fun signOut() {
        firebaseAuth.signOut()
    }

    /**
     * Check if current user's email is verified
     */
    fun isEmailVerified(): Boolean {
        return getCurrentUser()?.isEmailVerified ?: false
    }
}

/**
 * Sealed class for authentication results
 */
sealed class AuthResult {
    data class Success(val user: FirebaseUser?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}