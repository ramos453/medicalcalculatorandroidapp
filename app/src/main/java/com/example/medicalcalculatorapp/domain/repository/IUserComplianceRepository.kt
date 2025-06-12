package com.example.medicalcalculatorapp.domain.repository

import com.example.medicalcalculatorapp.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * User Compliance Repository Interface - Clean Architecture
 *
 * Defines the contract for user compliance data access.
 * Follows Google Play Health App Policy requirements and repository pattern.
 */
interface IUserComplianceRepository {

    // ==== BASIC COMPLIANCE OPERATIONS ====

    /**
     * Get user compliance status (reactive)
     */
    fun getUserCompliance(userId: String): Flow<UserCompliance?>

    /**
     * Get user compliance status (one-time)
     */
    suspend fun getUserComplianceSync(userId: String): UserCompliance?

    /**
     * Create new compliance record for user
     */
    suspend fun createUserCompliance(userId: String): UserCompliance

    /**
     * Update existing compliance record
     */
    suspend fun updateUserCompliance(compliance: UserCompliance): Boolean

    /**
     * Delete user compliance record (GDPR compliance)
     */
    suspend fun deleteUserCompliance(userId: String): Boolean

    /**
     * Check if user has compliance record
     */
    suspend fun hasComplianceRecord(userId: String): Boolean

    // ==== COMPLIANCE STATUS OPERATIONS ====

    /**
     * Check if user is fully compliant
     */
    suspend fun isUserCompliant(userId: String): Boolean

    /**
     * Get users requiring compliance review
     */
    fun getUsersNeedingReview(): Flow<List<UserCompliance>>

    /**
     * Mark user for compliance review
     */
    suspend fun markUserForReview(userId: String, reason: String): Boolean

    /**
     * Clear review flag for user
     */
    suspend fun clearReviewFlag(userId: String): Boolean

    // ==== CONSENT MANAGEMENT ====

    /**
     * Record basic terms consent
     */
    suspend fun recordBasicTermsConsent(
        userId: String,
        accepted: Boolean,
        version: String,
        method: ConsentMethod = ConsentMethod.APP_DIALOG
    ): Boolean

    /**
     * Record medical disclaimer consent
     */
    suspend fun recordMedicalDisclaimerConsent(
        userId: String,
        accepted: Boolean,
        version: String,
        method: ConsentMethod = ConsentMethod.APP_DIALOG
    ): Boolean

    /**
     * Record professional verification
     */
    suspend fun recordProfessionalVerification(
        userId: String,
        verified: Boolean,
        professionalType: ProfessionalType?,
        licenseInfo: String? = null
    ): Boolean

    /**
     * Record privacy policy consent
     */
    suspend fun recordPrivacyPolicyConsent(
        userId: String,
        accepted: Boolean,
        version: String,
        method: ConsentMethod = ConsentMethod.APP_DIALOG
    ): Boolean

    // ==== PROFESSIONAL VERIFICATION ====

    /**
     * Check if user is verified professional
     */
    suspend fun isProfessionalVerified(userId: String): Boolean

    /**
     * Get user's professional type
     */
    suspend fun getUserProfessionalType(userId: String): ProfessionalType?

    /**
     * Get all verified professionals
     */
    fun getVerifiedProfessionals(): Flow<List<UserCompliance>>

    /**
     * Get count of verified professionals by type
     */
    suspend fun getVerifiedProfessionalCount(type: ProfessionalType): Int

    // ==== VERSION MANAGEMENT ====

    /**
     * Get users with outdated compliance
     */
    fun getUsersWithOutdatedCompliance(currentVersion: String): Flow<List<UserCompliance>>

    /**
     * Update compliance version for user
     */
    suspend fun updateComplianceVersion(userId: String, newVersion: String): Boolean

    /**
     * Batch update compliance version
     */
    suspend fun batchUpdateComplianceVersion(
        oldVersion: String,
        newVersion: String
    ): Int

    /**
     * Check if user needs compliance update
     */
    suspend fun needsComplianceUpdate(userId: String, currentVersion: String): Boolean

    // ==== GOOGLE PLAY COMPLIANCE REPORTING ====

    /**
     * Get compliance statistics for Google Play reporting
     */
    suspend fun getComplianceStatistics(): ComplianceStatistics

    /**
     * Get compliance audit trail for user
     */
    suspend fun getComplianceAuditTrail(userId: String): ComplianceAuditTrail

    /**
     * Export user compliance data (GDPR)
     */
    suspend fun exportUserComplianceData(userId: String): UserComplianceExport?

    /**
     * Get users who accepted specific consent in date range
     */
    suspend fun getConsentAcceptanceReport(
        consentType: ComplianceRequirement,
        startDate: Long,
        endDate: Long
    ): List<UserCompliance>

    // ==== BULK OPERATIONS ====

    /**
     * Create compliance records for multiple users
     */
    suspend fun createBulkCompliance(userIds: List<String>): List<UserCompliance>

    /**
     * Update multiple compliance records
     */
    suspend fun updateBulkCompliance(complianceList: List<UserCompliance>): Boolean

    /**
     * Clean up old compliance records
     */
    suspend fun cleanupOldRecords(cutoffDate: Long): Int

    // ==== VALIDATION ====

    /**
     * Validate compliance data integrity
     */
    suspend fun validateComplianceIntegrity(userId: String): ComplianceValidationResult

    /**
     * Check for compliance data corruption
     */
    suspend fun checkDataCorruption(): List<String>
}

/**
 * Compliance statistics for reporting
 */
data class ComplianceStatistics(
    val totalUsers: Int,
    val compliantUsers: Int,
    val verifiedProfessionals: Int,
    val pendingReviews: Int,
    val byProfessionalType: Map<ProfessionalType, Int>,
    val byComplianceVersion: Map<String, Int>,
    val lastUpdated: Long
) {
    fun getComplianceRate(): Float {
        return if (totalUsers > 0) compliantUsers.toFloat() / totalUsers else 0f
    }

    fun getProfessionalVerificationRate(): Float {
        return if (totalUsers > 0) verifiedProfessionals.toFloat() / totalUsers else 0f
    }
}

/**
 * Compliance audit trail for individual users
 */
data class ComplianceAuditTrail(
    val userId: String,
    val events: List<ComplianceEvent>,
    val currentStatus: UserCompliance,
    val generatedAt: Long
)

/**
 * Individual compliance events for audit
 */
data class ComplianceEvent(
    val eventType: ComplianceEventType,
    val timestamp: Long,
    val details: String,
    val version: String?,
    val method: ConsentMethod
)

/**
 * Types of compliance events
 */
enum class ComplianceEventType(val description: String) {
    BASIC_TERMS_ACCEPTED("Basic terms accepted"),
    MEDICAL_DISCLAIMER_ACCEPTED("Medical disclaimer accepted"),
    PROFESSIONAL_VERIFIED("Professional status verified"),
    PRIVACY_POLICY_ACCEPTED("Privacy policy accepted"),
    COMPLIANCE_UPDATED("Compliance status updated"),
    REVIEW_REQUIRED("Marked for review"),
    REVIEW_CLEARED("Review flag cleared"),
    VERSION_UPDATED("Compliance version updated")
}

/**
 * User compliance data export for GDPR
 */
data class UserComplianceExport(
    val userId: String,
    val compliance: UserCompliance,
    val auditTrail: ComplianceAuditTrail,
    val exportedAt: Long,
    val exportFormat: String = "JSON"
) {
    fun toJsonString(): String {
        // This would typically use a JSON serialization library
        return """
        {
            "userId": "$userId",
            "exportedAt": $exportedAt,
            "compliance": {
                "isFullyCompliant": ${compliance.isFullyCompliant()},
                "complianceVersion": "${compliance.complianceVersion}",
                "basicTermsAccepted": ${compliance.basicTermsConsent?.isAccepted ?: false},
                "medicalDisclaimerAccepted": ${compliance.medicalDisclaimerConsent?.isAccepted ?: false},
                "professionalVerified": ${compliance.professionalVerification?.isVerified ?: false},
                "privacyPolicyAccepted": ${compliance.privacyPolicyConsent?.isAccepted ?: false}
            },
            "auditEvents": ${auditTrail.events.size}
        }
        """.trimIndent()
    }
}

/**
 * Compliance validation result
 */
data class ComplianceValidationResult(
    val isValid: Boolean,
    val errors: List<String>,
    val warnings: List<String>,
    val checkedAt: Long
) {
    fun hasErrors(): Boolean = errors.isNotEmpty()
    fun hasWarnings(): Boolean = warnings.isNotEmpty()

    fun getSummary(): String {
        return when {
            hasErrors() -> "❌ Validation Failed: ${errors.size} errors, ${warnings.size} warnings"
            hasWarnings() -> "⚠️ Validation Passed with Warnings: ${warnings.size} warnings"
            else -> "✅ Validation Passed"
        }
    }
}