package com.example.medicalcalculatorapp.domain.model

/**
 * Represents user profile information
 */
data class UserProfile(
    val id: String,
    val email: String,
    val fullName: String? = null,
    val profession: String? = null,
    val specialization: String? = null,
    val institution: String? = null,
    val licenseNumber: String? = null,
    val country: String? = null,
    val language: String = "es", // Spanish by default
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Represents user preferences and settings
 */
data class UserSettings(
    val userId: String,
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: String = "es",
    val notificationsEnabled: Boolean = true,
    val biometricAuthEnabled: Boolean = false,
    val autoSaveCalculations: Boolean = true,
    val defaultUnits: UnitSystem = UnitSystem.METRIC,
    val privacySettings: PrivacySettings = PrivacySettings(),
    val calculatorPreferences: CalculatorPreferences = CalculatorPreferences(),
    val lastSyncTime: Long? = null
)

/**
 * Privacy-related settings
 */
data class PrivacySettings(
    val shareUsageData: Boolean = false,
    val shareAnonymousStatistics: Boolean = true,
    val allowDataExport: Boolean = true,
    val dataRetentionDays: Int = 365
)

/**
 * Calculator-specific preferences
 */
data class CalculatorPreferences(
    val showDetailedResults: Boolean = true,
    val showReferences: Boolean = true,
    val confirmBeforeReset: Boolean = true,
    val saveCalculationHistory: Boolean = true,
    val maxHistoryItems: Int = 100
)

/**
 * App theme options
 */
enum class AppTheme {
    LIGHT, DARK, SYSTEM
}

/**
 * Unit system preferences
 */
enum class UnitSystem {
    METRIC, IMPERIAL
}

/**
 * Medical professions for profile
 */
enum class MedicalProfession(val displayName: String) {
    DOCTOR("Médico"),
    NURSE("Enfermero/a"),
    PHARMACIST("Farmacéutico/a"),
    PHYSIOTHERAPIST("Fisioterapeuta"),
    NUTRITIONIST("Nutricionista"),
    MEDICAL_STUDENT("Estudiante de Medicina"),
    NURSING_STUDENT("Estudiante de Enfermería"),
    RESIDENT("Residente"),
    SPECIALIST("Especialista"),
    OTHER("Otro")
}