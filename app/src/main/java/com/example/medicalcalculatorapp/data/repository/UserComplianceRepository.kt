package com.example.medicalcalculatorapp.data.repository

import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
import com.example.medicalcalculatorapp.data.db.mapper.UserComplianceMapper
import com.example.medicalcalculatorapp.data.db.entity.UserComplianceEntity
import com.example.medicalcalculatorapp.domain.model.*
import com.example.medicalcalculatorapp.domain.repository.IUserComplianceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

/**
 * User Compliance Repository Implementation
 *
 * Implements compliance data access according to Google Play Health App Policy.
 * Handles all database operations and business logic for compliance management.
 */
class UserComplianceRepository(
    private val database: MedicalCalculatorDatabase,
    private val mapper: UserComplianceMapper
) : IUserComplianceRepository {

    private val complianceDao = database.userComplianceDao()

    // ==== BASIC COMPLIANCE OPERATIONS ====

    override fun getUserCompliance(userId: String): Flow<UserCompliance?> {
        return complianceDao.getUserCompliance(userId)
            .map { entity -> entity?.let { mapper.mapEntityToDomain(it) } }
    }

    override suspend fun getUserComplianceSync(userId: String): UserCompliance? {
        val entity = complianceDao.getUserComplianceSync(userId)
        return entity?.let { mapper.mapEntityToDomain(it) }
    }

    override suspend fun createUserCompliance(userId: String): UserCompliance {
        return try {
            // Check if compliance already exists
            val existing = complianceDao.getUserComplianceSync(userId)
            if (existing != null) {
                return mapper.mapEntityToDomain(existing)
            }

            // Create new compliance record
            val newCompliance = mapper.createNewComplianceRecord(userId)
            val entity = mapper.mapDomainToEntity(newCompliance)

            complianceDao.insertUserCompliance(entity)

            newCompliance
        } catch (e: Exception) {
            throw ComplianceException("Failed to create compliance record for user $userId", e)
        }
    }

    override suspend fun updateUserCompliance(compliance: UserCompliance): Boolean {
        return try {
            // Validate compliance data
            val validationErrors = mapper.validateDomainModel(compliance)
            if (validationErrors.isNotEmpty()) {
                throw ComplianceException("Invalid compliance data: ${validationErrors.joinToString(", ")}")
            }

            val entity = mapper.mapDomainToEntity(compliance)
            complianceDao.updateUserCompliance(entity)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteUserCompliance(userId: String): Boolean {
        return try {
            complianceDao.deleteUserComplianceById(userId)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun hasComplianceRecord(userId: String): Boolean {
        return complianceDao.hasComplianceRecord(userId)
    }

    // ==== COMPLIANCE STATUS OPERATIONS ====

    override suspend fun isUserCompliant(userId: String): Boolean {
        return complianceDao.isUserCompliant(userId) ?: false
    }

    override fun getUsersNeedingReview(): Flow<List<UserCompliance>> {
        return complianceDao.getUsersNeedingReview()
            .map { entities -> mapper.mapEntitiesToDomain(entities) }
    }

    override suspend fun markUserForReview(userId: String, reason: String): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()
            complianceDao.markForReview(userId, true, reason, timestamp)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun clearReviewFlag(userId: String): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()
            complianceDao.markForReview(userId, false, null, timestamp)
            true
        } catch (e: Exception) {
            false
        }
    }

    // ==== CONSENT MANAGEMENT ====

    override suspend fun recordBasicTermsConsent(
        userId: String,
        accepted: Boolean,
        version: String,
        method: ConsentMethod
    ): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()

            // Ensure compliance record exists
            if (!hasComplianceRecord(userId)) {
                createUserCompliance(userId)
            }

            complianceDao.updateBasicTermsConsent(userId, accepted, timestamp, version)

            // Update overall compliance status
            updateOverallComplianceStatus(userId)

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun recordMedicalDisclaimerConsent(
        userId: String,
        accepted: Boolean,
        version: String,
        method: ConsentMethod
    ): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()

            // Ensure compliance record exists
            if (!hasComplianceRecord(userId)) {
                createUserCompliance(userId)
            }

            complianceDao.updateMedicalDisclaimerConsent(userId, accepted, timestamp, version)

            // Update overall compliance status
            updateOverallComplianceStatus(userId)

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun recordProfessionalVerification(
        userId: String,
        verified: Boolean,
        professionalType: ProfessionalType?,
        licenseInfo: String?
    ): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()

            // Ensure compliance record exists
            if (!hasComplianceRecord(userId)) {
                createUserCompliance(userId)
            }

            complianceDao.updateProfessionalVerification(
                userId, verified, timestamp, professionalType?.code, licenseInfo
            )

            // Update overall compliance status
            updateOverallComplianceStatus(userId)

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun recordPrivacyPolicyConsent(
        userId: String,
        accepted: Boolean,
        version: String,
        method: ConsentMethod
    ): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()

            // Ensure compliance record exists
            if (!hasComplianceRecord(userId)) {
                createUserCompliance(userId)
            }

            complianceDao.updatePrivacyPolicyConsent(userId, accepted, timestamp, version)

            // Update overall compliance status
            updateOverallComplianceStatus(userId)

            true
        } catch (e: Exception) {
            false
        }
    }

    // ==== PROFESSIONAL VERIFICATION ====

    override suspend fun isProfessionalVerified(userId: String): Boolean {
        return complianceDao.isProfessionalVerified(userId) ?: false
    }

    override suspend fun getUserProfessionalType(userId: String): ProfessionalType? {
        val typeCode = complianceDao.getProfessionalType(userId)
        return typeCode?.let { ProfessionalType.fromCode(it) }
    }

    override fun getVerifiedProfessionals(): Flow<List<UserCompliance>> {
        return complianceDao.getAllVerifiedProfessionals()
            .map { entities -> mapper.mapEntitiesToDomain(entities) }
    }

    override suspend fun getVerifiedProfessionalCount(type: ProfessionalType): Int {
        return complianceDao.getVerifiedProfessionalCount(type.code)
    }

    // ==== VERSION MANAGEMENT ====

    override fun getUsersWithOutdatedCompliance(currentVersion: String): Flow<List<UserCompliance>> {
        return complianceDao.getUsersWithOutdatedCompliance(currentVersion)
            .map { entities -> mapper.mapEntitiesToDomain(entities) }
    }

    override suspend fun updateComplianceVersion(userId: String, newVersion: String): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()
            complianceDao.updateComplianceStatus(userId, false, true, newVersion, timestamp)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun batchUpdateComplianceVersion(oldVersion: String, newVersion: String): Int {
        return try {
            val timestamp = System.currentTimeMillis()
            complianceDao.batchUpdateComplianceVersion(oldVersion, newVersion, timestamp)
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun needsComplianceUpdate(userId: String, currentVersion: String): Boolean {
        val compliance = getUserComplianceSync(userId) ?: return true
        return compliance.needsUpdate(currentVersion)
    }

    // ==== GOOGLE PLAY COMPLIANCE REPORTING ====

    override suspend fun getComplianceStatistics(): ComplianceStatistics {
        return try {
            val totalUsers = complianceDao.getTotalComplianceRecords()
            val compliantUsers = complianceDao.getCompliantUserCount()
            val verifiedProfessionals = complianceDao.getVerifiedProfessionalTotalCount()
            val pendingReviews = getUsersNeedingReview().first().size

            // Get professional type breakdown
            val professionalTypes = ProfessionalType.values()
            val byProfessionalType = professionalTypes.associateWith { type ->
                complianceDao.getVerifiedProfessionalCount(type.code)
            }

            // Note: byComplianceVersion would require additional DAO method
            val byComplianceVersion = mapOf(
                UserComplianceEntity.CURRENT_COMPLIANCE_VERSION to compliantUsers
            )

            ComplianceStatistics(
                totalUsers = totalUsers,
                compliantUsers = compliantUsers,
                verifiedProfessionals = verifiedProfessionals,
                pendingReviews = pendingReviews,
                byProfessionalType = byProfessionalType,
                byComplianceVersion = byComplianceVersion,
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            // Return empty statistics on error
            ComplianceStatistics(
                totalUsers = 0,
                compliantUsers = 0,
                verifiedProfessionals = 0,
                pendingReviews = 0,
                byProfessionalType = emptyMap(),
                byComplianceVersion = emptyMap(),
                lastUpdated = System.currentTimeMillis()
            )
        }
    }

    override suspend fun getComplianceAuditTrail(userId: String): ComplianceAuditTrail {
        val compliance = getUserComplianceSync(userId)
        val events = mutableListOf<ComplianceEvent>()

        // Build audit events from compliance record
        compliance?.let { comp ->
            comp.basicTermsConsent?.let { consent ->
                if (consent.isAccepted && consent.acceptedAt != null) {
                    events.add(
                        ComplianceEvent(
                            eventType = ComplianceEventType.BASIC_TERMS_ACCEPTED,
                            timestamp = consent.acceptedAt,
                            details = "Basic terms accepted version ${consent.version}",
                            version = consent.version,
                            method = consent.consentMethod
                        )
                    )
                }
            }

            comp.medicalDisclaimerConsent?.let { consent ->
                if (consent.isAccepted && consent.acceptedAt != null) {
                    events.add(
                        ComplianceEvent(
                            eventType = ComplianceEventType.MEDICAL_DISCLAIMER_ACCEPTED,
                            timestamp = consent.acceptedAt,
                            details = "Medical disclaimer accepted version ${consent.version}",
                            version = consent.version,
                            method = consent.consentMethod
                        )
                    )
                }
            }

            comp.professionalVerification?.let { verification ->
                if (verification.isVerified && verification.verifiedAt != null) {
                    events.add(
                        ComplianceEvent(
                            eventType = ComplianceEventType.PROFESSIONAL_VERIFIED,
                            timestamp = verification.verifiedAt,
                            details = "Professional verified as ${verification.professionalType?.displayName ?: "Unknown"}",
                            version = comp.complianceVersion,
                            method = comp.auditInfo?.consentMethod ?: ConsentMethod.APP_DIALOG
                        )
                    )
                }
            }

            comp.privacyPolicyConsent?.let { consent ->
                if (consent.isAccepted && consent.acceptedAt != null) {
                    events.add(
                        ComplianceEvent(
                            eventType = ComplianceEventType.PRIVACY_POLICY_ACCEPTED,
                            timestamp = consent.acceptedAt,
                            details = "Privacy policy accepted version ${consent.version}",
                            version = consent.version,
                            method = consent.consentMethod
                        )
                    )
                }
            }
        }

        return ComplianceAuditTrail(
            userId = userId,
            events = events.sortedBy { it.timestamp },
            currentStatus = compliance ?: mapper.createNewComplianceRecord(userId),
            generatedAt = System.currentTimeMillis()
        )
    }

    override suspend fun exportUserComplianceData(userId: String): UserComplianceExport? {
        val compliance = getUserComplianceSync(userId) ?: return null
        val auditTrail = getComplianceAuditTrail(userId)

        return UserComplianceExport(
            userId = userId,
            compliance = compliance,
            auditTrail = auditTrail,
            exportedAt = System.currentTimeMillis()
        )
    }

    override suspend fun getConsentAcceptanceReport(
        consentType: ComplianceRequirement,
        startDate: Long,
        endDate: Long
    ): List<UserCompliance> {
        // This would require additional DAO methods for date-range queries
        // For now, return empty list - implement when needed
        return emptyList()
    }

    // ==== BULK OPERATIONS ====

    override suspend fun createBulkCompliance(userIds: List<String>): List<UserCompliance> {
        return try {
            val complianceList = userIds.map { userId ->
                mapper.createNewComplianceRecord(userId)
            }

            val entities = mapper.mapDomainToEntities(complianceList)
            complianceDao.insertMultipleCompliance(entities)

            complianceList
        } catch (e: Exception) {
            emptyList()
        }
    }

    override suspend fun updateBulkCompliance(complianceList: List<UserCompliance>): Boolean {
        return try {
            // Validate all records first
            val validationErrors = complianceList.flatMap { mapper.validateDomainModel(it) }
            if (validationErrors.isNotEmpty()) {
                return false
            }

            val entities = mapper.mapDomainToEntities(complianceList)
            complianceDao.insertMultipleCompliance(entities) // INSERT OR REPLACE

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun cleanupOldRecords(cutoffDate: Long): Int {
        return try {
            complianceDao.deleteOldComplianceRecords(cutoffDate)
        } catch (e: Exception) {
            0
        }
    }

    // ==== VALIDATION ====

    override suspend fun validateComplianceIntegrity(userId: String): ComplianceValidationResult {
        val errors = mutableListOf<String>()
        val warnings = mutableListOf<String>()

        try {
            val compliance = getUserComplianceSync(userId)

            if (compliance == null) {
                errors.add("No compliance record found for user")
                return ComplianceValidationResult(false, errors, warnings, System.currentTimeMillis())
            }

            // Validate domain model
            val domainErrors = mapper.validateDomainModel(compliance)
            errors.addAll(domainErrors)

            // Check business logic
            if (compliance.isFullyCompliant()) {
                val missing = compliance.getMissingRequirements()
                if (missing.isNotEmpty()) {
                    warnings.add("User marked as compliant but missing: ${missing.joinToString(", ") { it.displayName }}")
                }
            }

            // Check version consistency
            val currentVersion = UserComplianceEntity.CURRENT_COMPLIANCE_VERSION
            if (compliance.needsUpdate(currentVersion)) {
                warnings.add("Compliance version ${compliance.complianceVersion} is outdated (current: $currentVersion)")
            }

        } catch (e: Exception) {
            errors.add("Validation failed: ${e.message}")
        }

        return ComplianceValidationResult(
            isValid = errors.isEmpty(),
            errors = errors,
            warnings = warnings,
            checkedAt = System.currentTimeMillis()
        )
    }

    override suspend fun checkDataCorruption(): List<String> {
        val issues = mutableListOf<String>()

        try {
            // Check for basic data integrity issues
            val totalRecords = complianceDao.getTotalComplianceRecords()
            val compliantUsers = complianceDao.getCompliantUserCount()

            if (compliantUsers > totalRecords) {
                issues.add("More compliant users ($compliantUsers) than total records ($totalRecords)")
            }

            // Additional integrity checks could be added here

        } catch (e: Exception) {
            issues.add("Data corruption check failed: ${e.message}")
        }

        return issues
    }

    // ==== PRIVATE HELPER METHODS ====

    /**
     * Update overall compliance status based on individual consents
     */
    private suspend fun updateOverallComplianceStatus(userId: String) {
        try {
            val compliance = getUserComplianceSync(userId) ?: return

            val isCompliant = compliance.isFullyCompliant()
            val needsReview = !isCompliant
            val timestamp = System.currentTimeMillis()

            complianceDao.updateComplianceStatus(
                userId = userId,
                compliant = isCompliant,
                needsReview = needsReview,
                version = compliance.complianceVersion,
                timestamp = timestamp
            )
        } catch (e: Exception) {
            // Log error but don't throw - this is a background update
            println("Failed to update overall compliance status for user $userId: ${e.message}")
        }
    }
}

/**
 * Custom exception for compliance operations
 */
class ComplianceException(message: String, cause: Throwable? = null) : Exception(message, cause)