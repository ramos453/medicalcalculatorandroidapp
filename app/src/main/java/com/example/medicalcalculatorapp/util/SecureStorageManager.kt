package com.example.medicalcalculatorapp.util

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

class SecureStorageManager(context: Context) {

    private val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    private val securePreferences: SharedPreferences = EncryptedSharedPreferences.create(
        "secure_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Regular SharedPreferences for non-sensitive guest data
    private val guestPreferences: SharedPreferences = context.getSharedPreferences(
        "guest_prefs",
        Context.MODE_PRIVATE
    )

    // === USER CREDENTIAL METHODS ===

    fun saveEmail(email: String) {
        securePreferences.edit().putString(KEY_EMAIL, email).apply()
    }

    fun getEmail(): String? {
        return securePreferences.getString(KEY_EMAIL, null)
    }

    fun saveRememberMeFlag(remember: Boolean) {
        securePreferences.edit().putBoolean(KEY_REMEMBER_ME, remember).apply()
    }

    fun getRememberMeFlag(): Boolean {
        return securePreferences.getBoolean(KEY_REMEMBER_ME, false)
    }

    fun clearCredentials() {
        securePreferences.edit()
            .remove(KEY_EMAIL)
            .remove(KEY_REMEMBER_ME)
            .apply()
    }

    // === DISCLAIMER METHODS (for both authenticated and guest users) ===

    /**
     * Save disclaimer acceptance for authenticated users
     */
    fun saveDisclaimerAccepted(accepted: Boolean) {
        securePreferences.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, accepted).apply()
    }

    /**
     * Check if authenticated user has accepted disclaimer
     */
    fun isDisclaimerAccepted(): Boolean {
        return securePreferences.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
    }

    /**
     * Save disclaimer acceptance for guest users (non-encrypted)
     */
    fun saveGuestDisclaimerAccepted(accepted: Boolean) {
        guestPreferences.edit().putBoolean(KEY_GUEST_DISCLAIMER_ACCEPTED, accepted).apply()
    }

    /**
     * Check if guest user has accepted disclaimer in current session
     */
    fun isGuestDisclaimerAccepted(): Boolean {
        return guestPreferences.getBoolean(KEY_GUEST_DISCLAIMER_ACCEPTED, false)
    }

    /**
     * Universal disclaimer check - works for both authenticated and guest users
     */
    fun hasAcceptedDisclaimer(): Boolean {
        return isDisclaimerAccepted() || isGuestDisclaimerAccepted()
    }

    // === GUEST SESSION METHODS ===

    /**
     * Save guest session preferences (temporary, cleared on app uninstall)
     */
    fun saveGuestPreference(key: String, value: String) {
        guestPreferences.edit().putString("guest_$key", value).apply()
    }

    fun getGuestPreference(key: String, defaultValue: String = ""): String {
        return guestPreferences.getString("guest_$key", defaultValue) ?: defaultValue
    }

    /**
     * Save guest session start time
     */
    fun saveGuestSessionStart(timestamp: Long) {
        guestPreferences.edit().putLong(KEY_GUEST_SESSION_START, timestamp).apply()
    }

    fun getGuestSessionStart(): Long {
        return guestPreferences.getLong(KEY_GUEST_SESSION_START, 0L)
    }

    /**
     * Track guest calculator usage (for rate limiting)
     */
    fun incrementGuestCalculationCount() {
        val currentCount = getGuestCalculationCount()
        guestPreferences.edit().putInt(KEY_GUEST_CALCULATION_COUNT, currentCount + 1).apply()
    }

    fun getGuestCalculationCount(): Int {
        return guestPreferences.getInt(KEY_GUEST_CALCULATION_COUNT, 0)
    }

    /**
     * Clear all guest session data
     */
    fun clearGuestSession() {
        guestPreferences.edit().clear().apply()
    }

    // === ONBOARDING AND FIRST-TIME USER EXPERIENCE ===

    /**
     * Track if this is the first time user is seeing the app
     */
    fun isFirstTimeUser(): Boolean {
        return !guestPreferences.getBoolean(KEY_HAS_LAUNCHED_BEFORE, false)
    }

    fun markAppAsLaunched() {
        guestPreferences.edit().putBoolean(KEY_HAS_LAUNCHED_BEFORE, true).apply()
    }

    /**
     * Track guest mode usage for analytics
     */
    fun incrementGuestModeUsage() {
        val currentCount = guestPreferences.getInt(KEY_GUEST_MODE_USAGE_COUNT, 0)
        guestPreferences.edit().putInt(KEY_GUEST_MODE_USAGE_COUNT, currentCount + 1).apply()
    }

    fun getGuestModeUsageCount(): Int {
        return guestPreferences.getInt(KEY_GUEST_MODE_USAGE_COUNT, 0)
    }

    // === PREMIUM FEATURE PROMPTS ===

    /**
     * Track how many times guest has been shown upgrade prompts
     */
    fun incrementUpgradePromptCount() {
        val currentCount = guestPreferences.getInt(KEY_UPGRADE_PROMPT_COUNT, 0)
        guestPreferences.edit().putInt(KEY_UPGRADE_PROMPT_COUNT, currentCount + 1).apply()
    }

    fun getUpgradePromptCount(): Int {
        return guestPreferences.getInt(KEY_UPGRADE_PROMPT_COUNT, 0)
    }

    fun shouldShowUpgradePrompt(): Boolean {
        return getUpgradePromptCount() < MAX_UPGRADE_PROMPTS
    }

    companion object {
        // Secure preferences keys (encrypted)
        private const val KEY_EMAIL = "user_email"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"

        // Guest preferences keys (non-encrypted, cleared on uninstall)
        private const val KEY_GUEST_DISCLAIMER_ACCEPTED = "guest_disclaimer_accepted"
        private const val KEY_GUEST_SESSION_START = "guest_session_start"
        private const val KEY_GUEST_CALCULATION_COUNT = "guest_calculation_count"
        private const val KEY_HAS_LAUNCHED_BEFORE = "has_launched_before"
        private const val KEY_GUEST_MODE_USAGE_COUNT = "guest_mode_usage_count"
        private const val KEY_UPGRADE_PROMPT_COUNT = "upgrade_prompt_count"

        // Limits
        private const val MAX_UPGRADE_PROMPTS = 3
    }
}