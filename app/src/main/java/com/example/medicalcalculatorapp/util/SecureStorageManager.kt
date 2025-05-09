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

    // User credential methods
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
            .apply()
    }

    // Disclaimer methods
    fun saveDisclaimerAccepted(accepted: Boolean) {
        securePreferences.edit().putBoolean(KEY_DISCLAIMER_ACCEPTED, accepted).apply()
    }

    fun isDisclaimerAccepted(): Boolean {
        return securePreferences.getBoolean(KEY_DISCLAIMER_ACCEPTED, false)
    }

    companion object {
        private const val KEY_EMAIL = "user_email"
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_DISCLAIMER_ACCEPTED = "disclaimer_accepted"
    }
}