package com.example.medicalcalculatorapp.domain.service

import android.content.Context
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.model.*
import com.example.medicalcalculatorapp.domain.repository.IUserComplianceRepository
import com.example.medicalcalculatorapp.util.SecureStorageManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Enhanced Compliance Manager Service - Google Play 2024 Compliant
 *
 * Manages user compliance according to Google Play Health App Policy requirements.
 * Integrates with existing legacy compliance system while providing new functionality.
 * Handles both authenticated users (persistent) and guest users (temporary).
 */
class ComplianceManagerService(
    private val context: Context,
    private val userManager: UserManager,
    private val complianceRepository: IUserComplianceRepository,
    private val secureStorageManager: SecureStorageManager
) {

    companion object {
        // Current policy versions - update these when policies change
        private const val CURRENT_BASIC_TERMS_VERSION = "2024.1"
        private const val CURRENT_MEDICAL_DISCLAIMER_VERSION = "2024.1"
        private const val CURRENT_PRIVACY_POLICY_VERSION = "2024.1"
        private const val CURRENT_COMPLIANCE_VERSION = "2024.1"

        // Compliance check intervals
        private const val COMPLIANCE_RECHECK_DAYS = 30L
        private const val MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000L
    }

    // ==== PUBLIC API METHODS ====

    /**
     * Get comprehensive compliance status for current user
     */
    suspend fun getComplianceStatus(): ComplianceStatusResult {
        val userId = userManager.getCurrentUserId()
        val userType = userManager.getUserType()

        return when (userType) {
            UserManager.UserType.AUTHENTICATED -> getAuthenticatedUserCompliance(userId)
            UserManager.UserType.GUEST -> getGuestUserCompliance(userId)
            UserManager.UserType.ANONYMOUS -> ComplianceStatusResult.requiresBasicIntroduction()
        }
    }

    /**
     * Determine what disclaimer flow should be shown to user
     */
    suspend fun getRequiredDisclaimerFlow(): DisclaimerFlow {
        val status = getComplianceStatus()

        return when {
            !status.hasBasicTerms -> DisclaimerFlow.BASIC_INTRODUCTION
            !status.hasMedicalDisclaimer || !status.isProfessionalVerified ->
                DisclaimerFlow.ENHANCED_MEDICAL_REQUIRED
            status.needsVersionUpdate -> DisclaimerFlow.COMPLIANCE_UPDATE_REQUIRED
            status.isFullyCompliant -> DisclaimerFlow.FULLY_COMPLIANT
            else -> DisclaimerFlow.BASIC_INTRODUCTION
        }
    }

    /**
     * Check if user can access medical calculators
     */
    suspend fun canAccessMedicalCalculators(): Boolean {
        val status = getComplianceStatus()
        return status.isFullyCompliant && !status.needsReview
    }

    /**
     * Record basic terms consent
     */
    suspend fun recordBasicTermsConsent(
        accepted: Boolean,
        method: ConsentMethod = ConsentMethod.APP_DIALOG
    ): Boolean {
        val userId = userManager.getCurrentUserId()
        val userType = userManager.getUserType()

        return when (userType) {
            UserManager.UserType.AUTHENTICATED -> {
                complianceRepository.recordBasicTermsConsent(
                    userId, accepted, CURRENT_BASIC_TERMS_VERSION, method
                )
            }
            UserManager.UserType.GUEST -> {
                // Store in secure storage for guest users
                secureStorageManager.saveGuestPreference("basic_terms_accepted", accepted.toString())
                secureStorageManager.saveGuestPreference("basic_terms_version", CURRENT_BASIC_TERMS_VERSION)
                secureStorageManager.saveGuestPreference("basic_terms_timestamp", System.currentTimeMillis().toString())
                true
            }
            UserManager.UserType.ANONYMOUS -> false
        }
    }

    /**
     * Record medical disclaimer consent
     */
    suspend fun recordMedicalDisclaimerConsent(
        accepted: Boolean,
        method: ConsentMethod = ConsentMethod.APP_DIALOG
    ): Boolean {
        val userId = userManager.getCurrentUserId()
        val userType = userManager.getUserType()

        return when (userType) {
            UserManager.UserType.AUTHENTICATED -> {
                complianceRepository.recordMedicalDisclaimerConsent(
                    userId, accepted, CURRENT_MEDICAL_DISCLAIMER_VERSION, method
                )
            }
            UserManager.UserType.GUEST -> {
                // Store in secure storage for guest users
                secureStorageManager.saveGuestPreference("medical_disclaimer_accepted", accepted.toString())
                secureStorageManager.saveGuestPreference("medical_disclaimer_version", CURRENT_MEDICAL_DISCLAIMER_VERSION)
                secureStorageManager.saveGuestPreference("medical_disclaimer_timestamp", System.currentTimeMillis().toString())

                // Also update legacy storage for backward compatibility
                secureStorageManager.saveGuestDisclaimerAccepted(accepted)
                true
            }
            UserManager.UserType.ANONYMOUS -> false
        }
    }

    /**
     * Record professional verification
     */
    suspend fun recordProfessionalVerification(
        verified: Boolean,
        professionalType: ProfessionalType?,
        licenseInfo: String? = null
    ): Boolean {
        val userId = userManager.getCurrentUserId()
        val userType = userManager.getUserType()

        return when (userType) {
            UserManager.UserType.AUTHENTICATED -> {
                complianceRepository.recordProfessionalVerification(
                    userId, verified, professionalType, licenseInfo
                )
            }
            UserManager.UserType.GUEST -> {
                // Store in secure storage for guest users
                secureStorageManager.saveGuestPreference("professional_verified", verified.toString())
                secureStorageManager.saveGuestPreference("professional_type", professionalType?.code ?: "")
                secureStorageManager.saveGuestPreference("professional_license", licenseInfo ?: "")
                secureStorageManager.saveGuestPreference("professional_timestamp", System.currentTimeMillis().toString())
                true
            }
            UserManager.UserType.ANONYMOUS -> false
        }
    }

    /**
     * Record privacy policy consent
     */
    suspend fun recordPrivacyPolicyConsent(
        accepted: Boolean,
        method: ConsentMethod = ConsentMethod.APP_DIALOG
    ): Boolean {
        val userId = userManager.getCurrentUserId()
        val userType = userManager.getUserType()

        return when (userType) {
            UserManager.UserType.AUTHENTICATED -> {
                complianceRepository.recordPrivacyPolicyConsent(
                    userId, accepted, CURRENT_PRIVACY_POLICY_VERSION, method
                )
            }
            UserManager.UserType.GUEST -> {
                // Store in secure storage for guest users
                secureStorageManager.saveGuestPreference("privacy_policy_accepted", accepted.toString())
                secureStorageManager.saveGuestPreference("privacy_policy_version", CURRENT_PRIVACY_POLICY_VERSION)
                secureStorageManager.saveGuestPreference("privacy_policy_timestamp", System.currentTimeMillis().toString())
                true
            }
            UserManager.UserType.ANONYMOUS -> false
        }
    }

    /**
     * Record complete compliance flow (all consents at once)
     */
    suspend fun recordCompleteCompliance(
        professionalType: ProfessionalType,
        licenseInfo: String? = null,
        method: ConsentMethod = ConsentMethod.APP_DIALOG
    ): Boolean {
        return try {
            val results = listOf(
                recordBasicTermsConsent(true, method),
                recordMedicalDisclaimerConsent(true, method),
                recordProfessionalVerification(true, professionalType, licenseInfo),
                recordPrivacyPolicyConsent(true, method)
            )

            results.all { it } // Return true only if all succeeded
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get compliance statistics for Google Play reporting
     */
    suspend fun getComplianceStatistics(): ComplianceStatistics {
        return complianceRepository.getComplianceStatistics()
    }

    /**
     * Export user compliance data for GDPR
     */
    suspend fun exportUserComplianceData(): UserComplianceExport? {
        val userId = userManager.getCurrentUserId()
        return if (userManager.getUserType() == UserManager.UserType.AUTHENTICATED) {
            complianceRepository.exportUserComplianceData(userId)
        } else {
            null // Guest data is not exported (temporary by design)
        }
    }

    /**
     * Migrate guest compliance to authenticated user
     */
    suspend fun migrateGuestToAuthenticated(authenticatedUserId: String): Boolean {
        return try {
            // Check if user has guest compliance data
            val hasGuestCompliance = hasGuestComplianceData()

            if (!hasGuestCompliance) {
                return true // Nothing to migrate
            }

            // Create compliance record for authenticated user
            complianceRepository.createUserCompliance(authenticatedUserId)

            // Migrate each consent type
            val basicTermsAccepted = secureStorageManager.getGuestPreference("basic_terms_accepted") == "true"
            if (basicTermsAccepted) {
                complianceRepository.recordBasicTermsConsent(
                    authenticatedUserId, true, CURRENT_BASIC_TERMS_VERSION
                )
            }

            val medicalDisclaimerAccepted = secureStorageManager.getGuestPreference("medical_disclaimer_accepted") == "true"
            if (medicalDisclaimerAccepted) {
                complianceRepository.recordMedicalDisclaimerConsent(
                    authenticatedUserId, true, CURRENT_MEDICAL_DISCLAIMER_VERSION
                )
            }

            val professionalVerified = secureStorageManager.getGuestPreference("professional_verified") == "true"
            if (professionalVerified) {
                val professionalTypeCode = secureStorageManager.getGuestPreference("professional_type")
                val professionalType = professionalTypeCode?.let { ProfessionalType.fromCode(it) }
                val licenseInfo = secureStorageManager.getGuestPreference("professional_license")

                complianceRepository.recordProfessionalVerification(
                    authenticatedUserId, true, professionalType, licenseInfo
                )
            }

            val privacyPolicyAccepted = secureStorageManager.getGuestPreference("privacy_policy_accepted") == "true"
            if (privacyPolicyAccepted) {
                complianceRepository.recordPrivacyPolicyConsent(
                    authenticatedUserId, true, CURRENT_PRIVACY_POLICY_VERSION
                )
            }

            // Clear guest compliance data after successful migration
            clearGuestComplianceData()

            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Reset compliance for user (for testing or policy updates)
     */
    suspend fun resetUserCompliance(): Boolean {
        val userId = userManager.getCurrentUserId()
        val userType = userManager.getUserType()

        return when (userType) {
            UserManager.UserType.AUTHENTICATED -> {
                complianceRepository.deleteUserCompliance(userId)
            }
            UserManager.UserType.GUEST -> {
                clearGuestComplianceData()
                true
            }
            UserManager.UserType.ANONYMOUS -> true
        }
    }

    // ==== PRIVATE HELPER METHODS ====

    /**
     * Get compliance status for authenticated users
     */
    private suspend fun getAuthenticatedUserCompliance(userId: String): ComplianceStatusResult {
        return try {
            val compliance = complianceRepository.getUserComplianceSync(userId)

            if (compliance == null) {
                // First time authenticated user - needs compliance setup
                complianceRepository.createUserCompliance(userId)
                return ComplianceStatusResult.requiresBasicIntroduction()
            }

            val needsVersionUpdate = compliance.needsUpdate(CURRENT_COMPLIANCE_VERSION)
            val needsPeriodicRecheck = needsPeriodicRecheck(compliance.lastUpdated)

            ComplianceStatusResult(
                hasBasicTerms = compliance.basicTermsConsent?.isAccepted == true,
                hasMedicalDisclaimer = compliance.medicalDisclaimerConsent?.isAccepted == true,
                isProfessionalVerified = compliance.professionalVerification?.isVerified == true,
                hasPrivacyPolicy = compliance.privacyPolicyConsent?.isAccepted == true,
                isFullyCompliant = compliance.isFullyCompliant(),
                needsReview = compliance.complianceStatus.needsReview,
                needsVersionUpdate = needsVersionUpdate,
                needsPeriodicRecheck = needsPeriodicRecheck,
                complianceVersion = compliance.complianceVersion,
                lastUpdated = compliance.lastUpdated,
                userType = UserManager.UserType.AUTHENTICATED,
                professionalType = compliance.professionalVerification?.professionalType
            )
        } catch (e: Exception) {
            ComplianceStatusResult.requiresBasicIntroduction()
        }
    }

    /**
     * Get compliance status for guest users
     */
    private suspend fun getGuestUserCompliance(userId: String): ComplianceStatusResult {
        val basicTermsAccepted = secureStorageManager.getGuestPreference("basic_terms_accepted") == "true"
        val medicalDisclaimerAccepted = secureStorageManager.getGuestPreference("medical_disclaimer_accepted") == "true" ||
                secureStorageManager.isGuestDisclaimerAccepted() // Legacy compatibility
        val professionalVerified = secureStorageManager.getGuestPreference("professional_verified") == "true"
        val privacyPolicyAccepted = secureStorageManager.getGuestPreference("privacy_policy_accepted") == "true"

        val professionalTypeCode = secureStorageManager.getGuestPreference("professional_type")
        val professionalType = professionalTypeCode?.let { ProfessionalType.fromCode(it) }

        val lastUpdatedStr = secureStorageManager.getGuestPreference("compliance_last_updated", "0")
        val lastUpdated = lastUpdatedStr.toLongOrNull() ?: 0L

        val isFullyCompliant = basicTermsAccepted && medicalDisclaimerAccepted &&
                professionalVerified && privacyPolicyAccepted

        return ComplianceStatusResult(
            hasBasicTerms = basicTermsAccepted,
            hasMedicalDisclaimer = medicalDisclaimerAccepted,
            isProfessionalVerified = professionalVerified,
            hasPrivacyPolicy = privacyPolicyAccepted,
            isFullyCompliant = isFullyCompliant,
            needsReview = false, // Guest users don't have review flags
            needsVersionUpdate = false, // Guest compliance is session-only
            needsPeriodicRecheck = false, // Guest sessions are temporary
            complianceVersion = CURRENT_COMPLIANCE_VERSION,
            lastUpdated = lastUpdated,
            userType = UserManager.UserType.GUEST,
            professionalType = professionalType
        )
    }

    /**
     * Check if guest user has any compliance data
     */
    private fun hasGuestComplianceData(): Boolean {
        return secureStorageManager.getGuestPreference("basic_terms_accepted").isNotEmpty() ||
                secureStorageManager.getGuestPreference("medical_disclaimer_accepted").isNotEmpty() ||
                secureStorageManager.getGuestPreference("professional_verified").isNotEmpty() ||
                secureStorageManager.getGuestPreference("privacy_policy_accepted").isNotEmpty() ||
                secureStorageManager.isGuestDisclaimerAccepted() // Legacy check
    }

    /**
     * Clear all guest compliance data
     */
    private fun clearGuestComplianceData() {
        val complianceKeys = listOf(
            "basic_terms_accepted", "basic_terms_version", "basic_terms_timestamp",
            "medical_disclaimer_accepted", "medical_disclaimer_version", "medical_disclaimer_timestamp",
            "professional_verified", "professional_type", "professional_license", "professional_timestamp",
            "privacy_policy_accepted", "privacy_policy_version", "privacy_policy_timestamp",
            "compliance_last_updated"
        )

        complianceKeys.forEach { key ->
            secureStorageManager.saveGuestPreference(key, "")
        }

        // Clear legacy storage
        secureStorageManager.saveGuestDisclaimerAccepted(false)
    }

    /**
     * Check if compliance needs periodic recheck
     */
    private fun needsPeriodicRecheck(lastUpdated: Long): Boolean {
        val daysSinceUpdate = (System.currentTimeMillis() - lastUpdated) / MILLISECONDS_PER_DAY
        return daysSinceUpdate >= COMPLIANCE_RECHECK_DAYS
    }
}

/**
 * Comprehensive compliance status result
 */
data class ComplianceStatusResult(
    val hasBasicTerms: Boolean,
    val hasMedicalDisclaimer: Boolean,
    val isProfessionalVerified: Boolean,
    val hasPrivacyPolicy: Boolean,
    val isFullyCompliant: Boolean,
    val needsReview: Boolean,
    val needsVersionUpdate: Boolean,
    val needsPeriodicRecheck: Boolean,
    val complianceVersion: String,
    val lastUpdated: Long,
    val userType: UserManager.UserType,
    val professionalType: ProfessionalType?
) {
    /**
     * Get missing requirements
     */
    fun getMissingRequirements(): List<ComplianceRequirement> {
        val missing = mutableListOf<ComplianceRequirement>()

        if (!hasBasicTerms) missing.add(ComplianceRequirement.BASIC_TERMS)
        if (!hasMedicalDisclaimer) missing.add(ComplianceRequirement.MEDICAL_DISCLAIMER)
        if (!isProfessionalVerified) missing.add(ComplianceRequirement.PROFESSIONAL_VERIFICATION)
        if (!hasPrivacyPolicy) missing.add(ComplianceRequirement.PRIVACY_POLICY)

        return missing
    }

    /**
     * Get completion percentage
     */
    fun getCompletionPercentage(): Float {
        val completed = listOf(hasBasicTerms, hasMedicalDisclaimer, isProfessionalVerified, hasPrivacyPolicy).count { it }
        return completed / 4f
    }

    /**
     * Get status summary text
     */
    fun getStatusSummary(): String {
        return when {
            isFullyCompliant -> "✅ Completamente Conforme"
            needsReview -> "⚠️ Requiere Revisión"
            needsVersionUpdate -> "📋 Actualización Requerida"
            else -> "❌ Conformidad Incompleta (${(getCompletionPercentage() * 100).toInt()}%)"
        }
    }

    companion object {
        /**
         * Create status for new users requiring basic introduction
         */
        fun requiresBasicIntroduction(): ComplianceStatusResult {
            return ComplianceStatusResult(
                hasBasicTerms = false,
                hasMedicalDisclaimer = false,
                isProfessionalVerified = false,
                hasPrivacyPolicy = false,
                isFullyCompliant = false,
                needsReview = false,
                needsVersionUpdate = false,
                needsPeriodicRecheck = false,
                complianceVersion = "",
                lastUpdated = 0L,
                userType = UserManager.UserType.ANONYMOUS,
                professionalType = null
            )
        }
    }
}