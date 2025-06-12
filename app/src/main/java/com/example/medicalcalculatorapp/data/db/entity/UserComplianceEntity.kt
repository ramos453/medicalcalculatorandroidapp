package com.example.medicalcalculatorapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

/**
 * User Compliance Entity - Google Play Health App Policy Compliance
 *
 * Tracks user consent and compliance status according to:
 * - Google Play Health App Policy 2024
 * - Medical device regulations
 * - Professional licensing verification
 * - GDPR consent management
 */
@Entity(
    tableName = "user_compliance",
    indices = [
        Index(value = ["userId"], unique = true),
        Index(value = ["complianceVersion"]),
        Index(value = ["lastUpdated"])
    ]
)
data class UserComplianceEntity(
    @PrimaryKey
    val userId: String,

    // Basic Consent Tracking
    val hasAcceptedBasicTerms: Boolean = false,
    val basicTermsAcceptedAt: Long? = null,
    val basicTermsVersion: String? = null,

    // Medical Disclaimer Compliance
    val hasAcceptedMedicalDisclaimer: Boolean = false,
    val medicalDisclaimerAcceptedAt: Long? = null,
    val medicalDisclaimerVersion: String? = null,

    // Professional Verification
    val isProfessionalVerified: Boolean = false,
    val professionalVerifiedAt: Long? = null,
    val professionalType: String? = null, // "DOCTOR", "NURSE", "PHARMACIST", etc.
    val professionalLicenseInfo: String? = null, // Optional license details

    // Privacy Consent
    val hasAcceptedPrivacyPolicy: Boolean = false,
    val privacyPolicyAcceptedAt: Long? = null,
    val privacyPolicyVersion: String? = null,

    // Compliance Versioning
    val complianceVersion: String = "2024.1", // Current compliance version
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),

    // Audit Trail
    val ipAddress: String? = null, // For compliance audit (optional)
    val userAgent: String? = null, // Device info for audit (optional)
    val consentMethod: String = "APP_DIALOG", // How consent was obtained

    // Status Flags
    val isCompliant: Boolean = false, // Overall compliance status
    val needsReview: Boolean = false, // Requires compliance review
    val complianceNotes: String? = null // Optional compliance notes
) {
    /**
     * Check if user is fully compliant with all requirements
     */
    fun isFullyCompliant(): Boolean {
        return hasAcceptedBasicTerms &&
                hasAcceptedMedicalDisclaimer &&
                isProfessionalVerified &&
                hasAcceptedPrivacyPolicy &&
                isCompliant
    }

    /**
     * Check if compliance needs refresh due to version changes
     */
    fun needsComplianceUpdate(currentVersion: String): Boolean {
        return complianceVersion != currentVersion || needsReview
    }

    /**
     * Get compliance summary for debugging/reporting
     */
    fun getComplianceSummary(): String {
        return """
            Compliance Status for User: $userId
            =====================================
            Basic Terms: ${if (hasAcceptedBasicTerms) "✓" else "✗"} (${basicTermsVersion ?: "N/A"})
            Medical Disclaimer: ${if (hasAcceptedMedicalDisclaimer) "✓" else "✗"} (${medicalDisclaimerVersion ?: "N/A"})
            Professional Verified: ${if (isProfessionalVerified) "✓" else "✗"} ($professionalType)
            Privacy Policy: ${if (hasAcceptedPrivacyPolicy) "✓" else "✗"} (${privacyPolicyVersion ?: "N/A"})
            Overall Compliance: ${if (isFullyCompliant()) "✓ COMPLIANT" else "✗ INCOMPLETE"}
            Version: $complianceVersion
            Last Updated: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(lastUpdated))}
        """.trimIndent()
    }

    companion object {
        // Current policy versions
        const val CURRENT_BASIC_TERMS_VERSION = "2024.1"
        const val CURRENT_MEDICAL_DISCLAIMER_VERSION = "2024.1"
        const val CURRENT_PRIVACY_POLICY_VERSION = "2024.1"
        const val CURRENT_COMPLIANCE_VERSION = "2024.1"

        // Professional types
        const val PROFESSIONAL_TYPE_DOCTOR = "DOCTOR"
        const val PROFESSIONAL_TYPE_NURSE = "NURSE"
        const val PROFESSIONAL_TYPE_PHARMACIST = "PHARMACIST"
        const val PROFESSIONAL_TYPE_STUDENT = "MEDICAL_STUDENT"
        const val PROFESSIONAL_TYPE_RESEARCHER = "RESEARCHER"
        const val PROFESSIONAL_TYPE_OTHER = "OTHER_HEALTHCARE"

        // Consent methods
        const val CONSENT_METHOD_APP_DIALOG = "APP_DIALOG"
        const val CONSENT_METHOD_WEB_FORM = "WEB_FORM"
        const val CONSENT_METHOD_EMAIL_VERIFICATION = "EMAIL_VERIFICATION"
        const val CONSENT_METHOD_PHONE_VERIFICATION = "PHONE_VERIFICATION"

        /**
         * Create a new compliance record for a user
         */
        fun createNewComplianceRecord(userId: String): UserComplianceEntity {
            return UserComplianceEntity(
                userId = userId,
                complianceVersion = CURRENT_COMPLIANCE_VERSION,
                createdAt = System.currentTimeMillis(),
                lastUpdated = System.currentTimeMillis()
            )
        }
    }
}