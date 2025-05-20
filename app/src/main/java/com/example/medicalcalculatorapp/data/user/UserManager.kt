package com.example.medicalcalculatorapp.data.user

import android.content.Context
import com.example.medicalcalculatorapp.util.SecureStorageManager

/**
 * Manages user-related operations and provides the current user ID.
 * In a more complex app, this would handle authentication state.
 */
class UserManager(context: Context) {

    private val secureStorageManager = SecureStorageManager(context)

    // For now, we'll use a simple implementation that uses the email as the user ID
    fun getCurrentUserId(): String {
        // Get the email from secure storage
        return secureStorageManager.getEmail() ?: DEFAULT_USER_ID
    }

    companion object {
        const val DEFAULT_USER_ID = "default_user"
    }
}