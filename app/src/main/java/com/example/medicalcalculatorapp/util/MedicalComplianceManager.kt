// Update this file: app/src/main/java/com/example/medicalcalculatorapp/util/MedicalComplianceManager.kt

package com.example.medicalcalculatorapp.util

import android.content.Context
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.model.DisclaimerFlow

/**
 * Medical Compliance Manager
 *
 * Manages progressive disclosure of disclaimers based on Google Play 2024 requirements:
 * - Simple disclaimer for app introduction
 * - Enhanced disclaimer for professional verification
 * - Tracks compliance status for different user types
 * - Coordinates with existing disclaimer system
 */
class MedicalComplianceManager(
    private val context: Context,
    private val secureStorageManager: SecureStorageManager,
    private val userManager: UserManager
) {

    companion object {
        // Compliance tracking keys
        private const val KEY_BASIC_DISCLAIMER_ACCEPTED = "basic_disclaimer_accepted"
        private const val KEY_ENHANCED_DISCLAIMER_ACCEPTED = "enhanced_disclaimer_accepted"
        private const val KEY_PROFESSIONAL_VERIFIED = "professional_verified"
        private const val KEY_GOOGLE_PLAY_COMPLIANCE_VERSION = "gp_compliance_version"
        private const val KEY_LAST_COMPLIANCE_CHECK = "last_compliance_check"

        // Current compliance version (increment when requirements change)
        private const val CURRENT_COMPLIANCE_VERSION = "2024.1"

        // Compliance check intervals
        private const val COMPLIANCE_RECHECK_DAYS = 30 // Re-verify every 30 days
    }

    /**
     * Determine which disclaimer flow to show based on user status and app context
     */
    fun getRequiredDisclaimerFlow(): DisclaimerFlow {
        val userType = userManager.getUserType()
        val hasBasicDisclaimer = hasAcceptedBasicDisclaimer()
        val hasEnhancedDisclaimer = hasAcceptedEnhancedDisclaimer()
        val isProfessionalVerified = isProfessionalVerified()
        val needsComplianceUpdate = needsComplianceVersionUpdate()

        return when {
            // New user - start with basic flow
            !hasBasicDisclaimer && userType == UserManager.UserType.ANONYMOUS -> {
                DisclaimerFlow.BASIC_INTRODUCTION
            }

            // Guest user with basic disclaimer but accessing medical features
            userType == UserManager.UserType.GUEST && hasBasicDisclaimer && !hasEnhancedDisclaimer -> {
                DisclaimerFlow.ENHANCED_MEDICAL_REQUIRED
            }

            // Professional user needs enhanced verification
            userType == UserManager.UserType.AUTHENTICATED && !isProfessionalVerified -> {
                DisclaimerFlow.PROFESSIONAL_VERIFICATION_REQUIRED
            }

            // Compliance version update required
            needsComplianceUpdate -> {
                DisclaimerFlow.COMPLIANCE_UPDATE_REQUIRED
            }

            // User is fully compliant
            hasBasicDisclaimer && hasEnhancedDisclaimer && isProfessionalVerified -> {
                DisclaimerFlow.FULLY_COMPLIANT
            }

            // Default to basic if uncertain
            else -> DisclaimerFlow.BASIC_INTRODUCTION
        }
    }

    /**
     * Check if user can access medical calculators
     */
    fun canAccessMedicalCalculators(): Boolean {
        val flow = getRequiredDisclaimerFlow()
        return flow == DisclaimerFlow.FULLY_COMPLIANT
    }

    /**
     * Check if enhanced disclaimer is required before calculator access
     */
    fun requiresEnhancedDisclaimerForCalculators(): Boolean {
        val flow = getRequiredDisclaimerFlow()
        return flow in listOf(
            DisclaimerFlow.ENHANCED_MEDICAL_REQUIRED,
            DisclaimerFlow.PROFESSIONAL_VERIFICATION_REQUIRED,
            DisclaimerFlow.COMPLIANCE_UPDATE_REQUIRED
        )
    }

    /**
     * Mark basic disclaimer as accepted (existing flow)
     */
    fun markBasicDisclaimerAccepted() {
        secureStorageManager.saveGuestPreference(KEY_BASIC_DISCLAIMER_ACCEPTED, "true")
        updateLastComplianceCheck()
    }

    /**
     * Mark enhanced disclaimer as accepted (new flow)
     */
    fun markEnhancedDisclaimerAccepted() {
        secureStorageManager.saveGuestPreference(KEY_ENHANCED_DISCLAIMER_ACCEPTED, "true")
        secureStorageManager.saveGuestPreference(KEY_GOOGLE_PLAY_COMPLIANCE_VERSION, CURRENT_COMPLIANCE_VERSION)
        updateLastComplianceCheck()
    }

    /**
     * Mark professional status as verified
     */
    fun markProfessionalVerified() {
        secureStorageManager.saveGuestPreference(KEY_PROFESSIONAL_VERIFIED, "true")
        updateLastComplianceCheck()
    }

    /**
     * Reset compliance status (for testing or policy updates)
     */
    fun resetComplianceStatus() {
        secureStorageManager.saveGuestPreference(KEY_BASIC_DISCLAIMER_ACCEPTED, "false")
        secureStorageManager.saveGuestPreference(KEY_ENHANCED_DISCLAIMER_ACCEPTED, "false")
        secureStorageManager.saveGuestPreference(KEY_PROFESSIONAL_VERIFIED, "false")
        secureStorageManager.saveGuestPreference(KEY_GOOGLE_PLAY_COMPLIANCE_VERSION, "")
    }

    /**
     * Get compliance status summary for debugging/reporting
     */
    fun getComplianceStatus(): ComplianceStatus {
        return ComplianceStatus(
            hasBasicDisclaimer = hasAcceptedBasicDisclaimer(),
            hasEnhancedDisclaimer = hasAcceptedEnhancedDisclaimer(),
            isProfessionalVerified = isProfessionalVerified(),
            complianceVersion = getCurrentComplianceVersion(),
            lastCheck = getLastComplianceCheck(),
            requiredFlow = getRequiredDisclaimerFlow(),
            canAccessCalculators = canAccessMedicalCalculators()
        )
    }

    /**
     * Check if Google Play compliance documentation is ready
     */
    fun isGooglePlayCompliant(): Boolean {
        val status = getComplianceStatus()
        return status.hasEnhancedDisclaimer &&
                status.isProfessionalVerified &&
                status.complianceVersion == CURRENT_COMPLIANCE_VERSION
    }

    // Private helper methods

    private fun hasAcceptedBasicDisclaimer(): Boolean {
        // Check both new system and existing system for backward compatibility
        return secureStorageManager.getGuestPreference(KEY_BASIC_DISCLAIMER_ACCEPTED) == "true" ||
                secureStorageManager.hasAcceptedDisclaimer() // Existing method
    }

    private fun hasAcceptedEnhancedDisclaimer(): Boolean {
        return secureStorageManager.getGuestPreference(KEY_ENHANCED_DISCLAIMER_ACCEPTED) == "true"
    }

    private fun isProfessionalVerified(): Boolean {
        return secureStorageManager.getGuestPreference(KEY_PROFESSIONAL_VERIFIED) == "true"
    }

    private fun getCurrentComplianceVersion(): String {
        return secureStorageManager.getGuestPreference(KEY_GOOGLE_PLAY_COMPLIANCE_VERSION, "")
    }

    private fun needsComplianceVersionUpdate(): Boolean {
        val savedVersion = getCurrentComplianceVersion()
        return savedVersion != CURRENT_COMPLIANCE_VERSION && hasAcceptedEnhancedDisclaimer()
    }

    private fun getLastComplianceCheck(): Long {
        val lastCheckStr = secureStorageManager.getGuestPreference(KEY_LAST_COMPLIANCE_CHECK, "0")
        return lastCheckStr.toLongOrNull() ?: 0L
    }

    private fun updateLastComplianceCheck() {
        secureStorageManager.saveGuestPreference(
            KEY_LAST_COMPLIANCE_CHECK,
            System.currentTimeMillis().toString()
        )
    }

    private fun needsPeriodicRecheck(): Boolean {
        val lastCheck = getLastComplianceCheck()
        val daysSinceCheck = (System.currentTimeMillis() - lastCheck) / (24 * 60 * 60 * 1000)
        return daysSinceCheck >= COMPLIANCE_RECHECK_DAYS
    }
}

/**
 * Comprehensive compliance status for reporting and debugging
 */
data class ComplianceStatus(
    val hasBasicDisclaimer: Boolean,
    val hasEnhancedDisclaimer: Boolean,
    val isProfessionalVerified: Boolean,
    val complianceVersion: String,
    val lastCheck: Long,
    val requiredFlow: DisclaimerFlow,
    val canAccessCalculators: Boolean
) {
    /**
     * Generate compliance report for Google Play Store documentation
     */
    fun generateComplianceReport(): String {
        return """
            📋 MEDICAL APP COMPLIANCE REPORT
            ================================
            
            📱 App: MediCálculos
            📅 Report Date: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date())}
            
            ✅ COMPLIANCE STATUS:
            • Basic Disclaimer: ${if (hasBasicDisclaimer) "✓ ACCEPTED" else "✗ PENDING"}
            • Enhanced Medical Disclaimer: ${if (hasEnhancedDisclaimer) "✓ ACCEPTED" else "✗ PENDING"}
            • Professional Verification: ${if (isProfessionalVerified) "✓ VERIFIED" else "✗ PENDING"}
            • Google Play Compliance Version: $complianceVersion
            
            🎯 CURRENT STATUS: ${requiredFlow.getDescription()}
            🏥 Medical Calculator Access: ${if (canAccessCalculators) "GRANTED" else "RESTRICTED"}
            
            📋 GOOGLE PLAY STORE COMPLIANCE:
            ${if (hasEnhancedDisclaimer && isProfessionalVerified) "✅ MEETS 2024 HEALTH APP REQUIREMENTS" else "⚠️ COMPLIANCE IN PROGRESS"}
            
            Last Compliance Check: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(lastCheck))}
        """.trimIndent()
    }
}