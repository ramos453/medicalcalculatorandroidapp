package com.example.medicalcalculatorapp.domain.model

/**
 * Represents the current user session state
 */
data class UserSessionState(
    val userId: String,
    val userType: UserType,
    val displayName: String,
    val canSaveData: Boolean,
    val hasAcceptedDisclaimer: Boolean,
    val sessionStartTime: Long = System.currentTimeMillis()
)

/**
 * Enhanced user type enumeration
 */
enum class UserType {
    ANONYMOUS,      // No session started (should not occur in normal flow)
    GUEST,          // Guest session - limited features
    AUTHENTICATED;  // Full user account - all features

    /**
     * Check if this user type has limitations
     */
    fun hasLimitations(): Boolean = this == GUEST

    /**
     * Get display text for user type
     */
    fun getDisplayText(): String = when (this) {
        ANONYMOUS -> "Sin sesión"
        GUEST -> "Invitado"
        AUTHENTICATED -> "Usuario registrado"
    }
}

/**
 * Guest session limitations and prompts
 */
data class GuestLimitations(
    val canSaveHistory: Boolean = false,
    val canSaveFavorites: Boolean = false,
    val canSyncData: Boolean = false,
    val canExportData: Boolean = false,
    val maxCalculationsPerSession: Int = 50,
    val sessionTimeoutHours: Int = 24,
    val showUpgradePrompts: Boolean = true
)

/**
 * Account upgrade prompt configuration
 */
data class UpgradePromptConfig(
    val trigger: UpgradeTrigger,
    val title: String,
    val message: String,
    val primaryAction: String = "Crear Cuenta",
    val secondaryAction: String = "Continuar como Invitado"
)

enum class UpgradeTrigger {
    CALCULATION_LIMIT,      // After X calculations
    FAVORITE_ATTEMPT,       // When trying to add favorite
    HISTORY_VIEW,          // When trying to view history
    EXPORT_ATTEMPT,        // When trying to export
    SESSION_TIMEOUT,       // Near session timeout
    MANUAL                 // User initiated
}