package com.example.medicalcalculatorapp.data.repository

import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
import com.example.medicalcalculatorapp.data.db.entity.UserComplianceEntity
import com.example.medicalcalculatorapp.data.db.mapper.UserComplianceMapper
import com.example.medicalcalculatorapp.domain.model.*
import com.example.medicalcalculatorapp.domain.repository.IUserComplianceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.catch

/**
 * User Compliance Repository Implementation
 *
 * Concrete implementation of IUserComplianceRepository that handles:
 * - Google Play Health App Policy compliance data
 * - Database operations with proper error handling
 * - Domain model mapping and validation
 * - GDPR-compliant data management
 */
class UserComplianceRepository(
    private val database: MedicalCalculatorDatabase,
    private val mapper: UserComplianceMapper
) : IUserComplianceRepository {

    private val dao = database.userComplianceDao()

    // ==== BASIC COMPLIANCE OPERATIONS ====

    override fun getUserCompliance(userId: String): Flow<UserCompliance?> {
        return dao.getUserCompliance(userId)
            .map { entity ->
                entity?.let { mapper.mapEntityToDomain(it) }
            }
            .catch { exception ->
                println("❌ Error getting user compliance for $userId: ${exception.message}")
                emit(null)
            }
    }

    override suspend fun getUserComplianceSync(userId: String): UserCompliance? {
        return try {
            val entity = dao.getUserComplianceSync(userId)
            entity?.let { mapper.mapEntityToDomain(it) }
        } catch (e: Exception) {
            println("❌ Error getting user compliance sync for $userId: ${e.message}")
            null
        }
    }

    override suspend fun createUserCompliance(userId: String): UserCompliance {
        return try {
            // Create default compliance record
            val defaultCompliance = createDefaultCompliance(userId)
            val entity = mapper.mapDomainToEntity(defaultCompliance)

            dao.insertUserCompliance(entity)
            println("✅ Created compliance record for user: $userId")

            defaultCompliance
        } catch (e: Exception) {
            println("❌ Error creating user compliance for $userId: ${e.message}")
            throw e
        }
    }

    override suspend fun updateUserCompliance(compliance: UserCompliance): Boolean {
        return try {
            // Validate domain model before saving
            val validationErrors = mapper.validateDomainModel(compliance)
            if (validationErrors.isNotEmpty()) {
                println("❌ Validation errors for compliance update: $validationErrors")
                return false
            }

            // Update timestamp and map to entity
            val updatedCompliance = compliance.copy(lastUpdated = System.currentTimeMillis())
            val entity = mapper.mapDomainToEntity(updatedCompliance)

            dao.updateUserCompliance(entity)
            println("✅ Updated compliance record for user: ${compliance.userId}")

            true
        } catch (e: Exception) {
            println("❌ Error updating user compliance: ${e.message}")
            false
        }
    }

    override suspend fun deleteUserCompliance(userId: String): Boolean {
        return try {
            dao.deleteUserComplianceById(userId)
            println("✅ Deleted compliance record for user: $userId")
            true
        } catch (e: Exception) {
            println("❌ Error deleting user compliance for $userId: ${e.message}")
            false
        }
    }

    override suspend fun hasComplianceRecord(userId: String): Boolean {
        return try {
            dao.hasComplianceRecord(userId)
        } catch (e: Exception) {
            println("❌ Error checking compliance record for $userId: ${e.message}")
            false
        }
    }

    // ==== COMPLIANCE STATUS OPERATIONS ====

    override suspend fun isUserCompliant(userId: String): Boolean {
        return try {
            dao.isUserCompliant(userId) ?: false
        } catch (e: Exception) {
            println("❌ Error checking user compliance for $userId: ${e.message}")
            false
        }
    }

    override fun getUsersNeedingReview(): Flow<List<UserCompliance>> {
        return dao.getUsersNeedingReview()
            .map { entities ->
                entities.map { mapper.mapEntityToDomain(it) }
            }
            .catch { exception ->
                println("❌ Error getting users needing review: ${exception.message}")
                emit(emptyList())
            }
    }

    override suspend fun markUserForReview(userId: String, reason: String): Boolean {
        return try {
            val entity = dao.getUserComplianceSync(userId)
            if (entity != null) {
                val updatedEntity = entity.copy(
                    needsReview = true,
                    complianceNotes = reason,
                    lastUpdated = System.currentTimeMillis()
                )
                dao.updateUserCompliance(updatedEntity)
                println("✅ Marked user $userId for review: $reason")
                true
            } else {
                println("❌ Cannot mark user $userId for review - no compliance record found")
                false
            }
        } catch (e: Exception) {
            println("❌ Error marking user $userId for review: ${e.message}")
            false
        }
    }

    override suspend fun clearReviewFlag(userId: String): Boolean {
        return try {
            val entity = dao.getUserComplianceSync(userId)
            if (entity != null) {
                val updatedEntity = entity.copy(
                    needsReview = false,
                    complianceNotes = null,
                    lastUpdated = System.currentTimeMillis()
                )
                dao.updateUserCompliance(updatedEntity)
                println("✅ Cleared review flag for user: $userId")
                true
            } else {
                println("❌ Cannot clear review flag for $userId - no compliance record found")
                false
            }
        } catch (e: Exception) {
            println("❌ Error clearing review flag for $userId: ${e.message}")
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
            dao.updateBasicTermsConsent(userId, accepted, timestamp, version)
            println("✅ Recorded basic terms consent for $userId: accepted=$accepted")
            true
        } catch (e: Exception) {
            println("❌ Error recording basic terms consent for $userId: ${e.message}")
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
            dao.updateMedicalDisclaimerConsent(userId, accepted, timestamp, version)
            println("✅ Recorded medical disclaimer consent for $userId: accepted=$accepted")
            true
        } catch (e: Exception) {
            println("❌ Error recording medical disclaimer consent for $userId: ${e.message}")
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
            dao.updateProfessionalVerification(
                userId,
                verified,
                timestamp,
                professionalType?.code,
                licenseInfo
            )
            println("✅ Recorded professional verification for $userId: verified=$verified")
            true
        } catch (e: Exception) {
            println("❌ Error recording professional verification for $userId: ${e.message}")
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
            // For privacy policy, we need to update the entity manually since there's no specific DAO method
            val entity = dao.getUserComplianceSync(userId)
            if (entity != null) {
                val timestamp = System.currentTimeMillis()
                val updatedEntity = entity.copy(
                    hasAcceptedPrivacyPolicy = accepted,
                    privacyPolicyAcceptedAt = if (accepted) timestamp else null,
                    privacyPolicyVersion = if (accepted) version else null,
                    lastUpdated = timestamp
                )
                dao.updateUserCompliance(updatedEntity)
                println("✅ Recorded privacy policy consent for $userId: accepted=$accepted")
                true
            } else {
                println("❌ Cannot record privacy policy consent for $userId - no compliance record found")
                false
            }
        } catch (e: Exception) {
            println("❌ Error recording privacy policy consent for $userId: ${e.message}")
            false
        }
    }

    // ==== PROFESSIONAL VERIFICATION OPERATIONS ====

    override suspend fun isProfessionalVerified(userId: String): Boolean {
        return try {
            dao.isProfessionalVerified(userId) ?: false
        } catch (e: Exception) {
            println("❌ Error checking professional verification for $userId: ${e.message}")
            false
        }
    }

    override suspend fun getUserProfessionalType(userId: String): ProfessionalType? {
        return try {
            val typeCode = dao.getProfessionalType(userId)
            typeCode?.let { ProfessionalType.fromCode(it) }
        } catch (e: Exception) {
            println("❌ Error getting professional type for $userId: ${e.message}")
            null
        }
    }

    override suspend fun getVerifiedProfessionalCount(type: ProfessionalType): Int {
        return try {
            dao.getVerifiedProfessionalCount(type.code)
        } catch (e: Exception) {
            println("❌ Error getting verified professional count: ${e.message}")
            0
        }
    }

    override fun getVerifiedProfessionals(): Flow<List<UserCompliance>> {
        return dao.getAllVerifiedProfessionals()
            .map { entities ->
                entities.map { mapper.mapEntityToDomain(it) }
            }
            .catch { exception ->
                println("❌ Error getting verified professionals: ${exception.message}")
                emit(emptyList())
            }
    }

    // ==== VERSION MANAGEMENT ====

    override fun getUsersWithOutdatedCompliance(currentVersion: String): Flow<List<UserCompliance>> {
        return dao.getUsersWithOutdatedCompliance(currentVersion)
            .map { entities ->
                entities.map { mapper.mapEntityToDomain(it) }
            }
            .catch { exception ->
                println("❌ Error getting users with outdated compliance: ${exception.message}")
                emit(emptyList())
            }
    }

    override suspend fun updateComplianceVersion(userId: String, newVersion: String): Boolean {
        return try {
            val entity = dao.getUserComplianceSync(userId)
            if (entity != null) {
                val updatedEntity = entity.copy(
                    complianceVersion = newVersion,
                    lastUpdated = System.currentTimeMillis()
                )
                dao.updateUserCompliance(updatedEntity)
                println("✅ Updated compliance version for $userId to $newVersion")
                true
            } else {
                println("❌ Cannot update compliance version for $userId - no compliance record found")
                false
            }
        } catch (e: Exception) {
            println("❌ Error updating compliance version for $userId: ${e.message}")
            false
        }
    }

    override suspend fun batchUpdateComplianceVersion(
        oldVersion: String,
        newVersion: String
    ): Int {
        return try {
            // Get all users with the old version
            var updatedCount = 0
            val usersFlow = dao.getUsersWithOutdatedCompliance(newVersion)

            // Note: In a real implementation, you'd want to handle this more efficiently
            // This is a simplified approach for demonstration
            usersFlow.collect { userEntities ->
                userEntities.forEach { entity ->
                    if (entity.complianceVersion == oldVersion) {
                        val updatedEntity = entity.copy(
                            complianceVersion = newVersion,
                            lastUpdated = System.currentTimeMillis()
                        )
                        dao.updateUserCompliance(updatedEntity)
                        updatedCount++
                    }
                }
            }

            println("✅ Batch updated $updatedCount users from version $oldVersion to $newVersion")
            updatedCount
        } catch (e: Exception) {
            println("❌ Error batch updating compliance version: ${e.message}")
            0
        }
    }

    override suspend fun needsComplianceUpdate(userId: String, currentVersion: String): Boolean {
        return try {
            val entity = dao.getUserComplianceSync(userId)
            entity?.complianceVersion != currentVersion
        } catch (e: Exception) {
            println("❌ Error checking compliance update need for $userId: ${e.message}")
            false
        }
    }

    // ==== GOOGLE PLAY COMPLIANCE REPORTING ====

    override suspend fun getComplianceStatistics(): ComplianceStatistics {
        return try {
            // For a complete implementation, you'd need additional DAO methods
            // This is a basic implementation that can be expanded
            val allCompliantUsers = dao.getAllCompliantUsers()
            val allVerifiedProfessionals = dao.getAllVerifiedProfessionals()

            var compliantCount = 0
            var verifiedCount = 0
            val professionalTypeCounts = mutableMapOf<ProfessionalType, Int>()
            val versionCounts = mutableMapOf<String, Int>()

            // Count compliant users
            allCompliantUsers.collect { entities ->
                compliantCount = entities.size
                entities.forEach { entity ->
                    versionCounts[entity.complianceVersion] =
                        versionCounts.getOrDefault(entity.complianceVersion, 0) + 1
                }
            }

            // Count verified professionals
            allVerifiedProfessionals.collect { entities ->
                verifiedCount = entities.size
                entities.forEach { entity ->
                    entity.professionalType?.let { typeCode ->
                        ProfessionalType.fromCode(typeCode)?.let { type ->
                            professionalTypeCounts[type] =
                                professionalTypeCounts.getOrDefault(type, 0) + 1
                        }
                    }
                }
            }

            ComplianceStatistics(
                totalUsers = compliantCount + verifiedCount, // Simplified calculation
                compliantUsers = compliantCount,
                verifiedProfessionals = verifiedCount,
                pendingReviews = 0, // Would need additional DAO method
                byProfessionalType = professionalTypeCounts,
                byComplianceVersion = versionCounts,
                lastUpdated = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            println("❌ Error getting compliance statistics: ${e.message}")
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
        return try {
            val compliance = getUserComplianceSync(userId)
            if (compliance != null) {
                // For a full implementation, you'd track actual events
                // This is a simplified version that shows the current status
                val events = listOf<ComplianceEvent>() // Would be populated from event tracking

                ComplianceAuditTrail(
                    userId = userId,
                    events = events,
                    currentStatus = compliance,
                    generatedAt = System.currentTimeMillis()
                )
            } else {
                throw Exception("No compliance record found for user $userId")
            }
        } catch (e: Exception) {
            println("❌ Error getting compliance audit trail for $userId: ${e.message}")
            throw e
        }
    }

    override suspend fun exportUserComplianceData(userId: String): UserComplianceExport? {
        return try {
            val compliance = getUserComplianceSync(userId)
            if (compliance != null) {
                val auditTrail = getComplianceAuditTrail(userId)
                UserComplianceExport(
                    userId = userId,
                    compliance = compliance,
                    auditTrail = auditTrail,
                    exportedAt = System.currentTimeMillis()
                )
            } else {
                println("❌ No compliance data found for user $userId")
                null
            }
        } catch (e: Exception) {
            println("❌ Error exporting compliance data for $userId: ${e.message}")
            null
        }
    }

    // ==== ADDITIONAL INTERFACE METHODS ====

    override suspend fun getConsentAcceptanceReport(
        consentType: ComplianceRequirement,
        startDate: Long,
        endDate: Long
    ): List<UserCompliance> {
        return try {
            // This would need specific DAO methods to implement properly
            // For now, returning empty list as a placeholder
            println("⚠️ getConsentAcceptanceReport not fully implemented yet")
            emptyList()
        } catch (e: Exception) {
            println("❌ Error getting consent acceptance report: ${e.message}")
            emptyList()
        }
    }

    override suspend fun createBulkCompliance(userIds: List<String>): List<UserCompliance> {
        return try {
            val complianceList = mutableListOf<UserCompliance>()
            userIds.forEach { userId ->
                val compliance = createUserCompliance(userId)
                complianceList.add(compliance)
            }
            println("✅ Created bulk compliance for ${userIds.size} users")
            complianceList
        } catch (e: Exception) {
            println("❌ Error creating bulk compliance: ${e.message}")
            emptyList()
        }
    }

    override suspend fun updateBulkCompliance(complianceList: List<UserCompliance>): Boolean {
        return try {
            var successCount = 0
            complianceList.forEach { compliance ->
                if (updateUserCompliance(compliance)) {
                    successCount++
                }
            }
            println("✅ Updated $successCount/${complianceList.size} compliance records")
            successCount == complianceList.size
        } catch (e: Exception) {
            println("❌ Error updating bulk compliance: ${e.message}")
            false
        }
    }

    override suspend fun cleanupOldRecords(cutoffDate: Long): Int {
        return try {
            // This would need specific DAO methods to implement properly
            println("⚠️ cleanupOldRecords not fully implemented yet")
            0
        } catch (e: Exception) {
            println("❌ Error cleaning up old records: ${e.message}")
            0
        }
    }

    override suspend fun validateComplianceIntegrity(userId: String): ComplianceValidationResult {
        return try {
            val compliance = getUserComplianceSync(userId)
            if (compliance != null) {
                val errors = mapper.validateDomainModel(compliance)
                ComplianceValidationResult(
                    isValid = errors.isEmpty(),
                    errors = errors,
                    warnings = emptyList(),
                    checkedAt = System.currentTimeMillis()
                )
            } else {
                ComplianceValidationResult(
                    isValid = false,
                    errors = listOf("No compliance record found for user $userId"),
                    warnings = emptyList(),
                    checkedAt = System.currentTimeMillis()
                )
            }
        } catch (e: Exception) {
            println("❌ Error validating compliance integrity for $userId: ${e.message}")
            ComplianceValidationResult(
                isValid = false,
                errors = listOf("Validation failed: ${e.message}"),
                warnings = emptyList(),
                checkedAt = System.currentTimeMillis()
            )
        }
    }

    override suspend fun checkDataCorruption(): List<String> {
        return try {
            // This would need specific checks to implement properly
            println("⚠️ checkDataCorruption not fully implemented yet")
            emptyList()
        } catch (e: Exception) {
            println("❌ Error checking data corruption: ${e.message}")
            listOf("Error checking corruption: ${e.message}")
        }
    }

    // ==== PRIVATE HELPER METHODS ====

    private fun createDefaultCompliance(userId: String): UserCompliance {
        val currentTime = System.currentTimeMillis()
        return UserCompliance(
            userId = userId,
            basicTermsConsent = null,
            medicalDisclaimerConsent = null,
            professionalVerification = null,
            privacyPolicyConsent = null,
            complianceStatus = ComplianceStatus(
                isCompliant = false,
                needsReview = false,
                notes = "Newly created compliance record"
            ),
            complianceVersion = "2024.1",
            lastUpdated = currentTime,
            createdAt = currentTime,
            auditInfo = null
        )
    }
}