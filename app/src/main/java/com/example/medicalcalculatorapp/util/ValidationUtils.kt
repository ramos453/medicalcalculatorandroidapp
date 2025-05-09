package com.example.medicalcalculatorapp.util

import android.util.Patterns
import java.util.regex.Pattern

object ValidationUtils {

    // Email validation with regex pattern
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Password validation with security requirements
    fun isValidPassword(password: String): Boolean {
        if (password.length < 8) return false

        // Check for at least one digit
        val hasDigit = password.any { it.isDigit() }
        // Check for at least one letter
        val hasLetter = password.any { it.isLetter() }
        // Check for at least one special character
        val specialCharPattern = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]")
        val hasSpecialChar = specialCharPattern.matcher(password).find()

        return hasDigit && hasLetter && hasSpecialChar
    }

    // Input sanitization to prevent injection attacks
    fun sanitizeInput(input: String): String {
        // Remove potentially harmful characters
        return input.replace("[<>&'\"]".toRegex(), "")
    }

    // Check for potentially malicious input patterns
    fun containsSuspiciousPatterns(input: String): Boolean {
        val suspiciousPatterns = listOf(
            "SELECT", "INSERT", "UPDATE", "DELETE", "DROP", "EXEC",
            "<script>", "javascript:", "alert(", "onerror="
        )

        val lowercaseInput = input.lowercase()
        return suspiciousPatterns.any { lowercaseInput.contains(it.lowercase()) }
    }
}