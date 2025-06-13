package com.example.medicalcalculatorapp.domain.repository

import com.example.medicalcalculatorapp.domain.model.*
import kotlinx.coroutines.flow.Flow
import com.example.medicalcalculatorapp.domain.model.*

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

