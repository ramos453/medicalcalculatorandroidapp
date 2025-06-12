package com.example.medicalcalculatorapp.data.db.dao

import androidx.room.*
import com.example.medicalcalculatorapp.data.db.entity.UserComplianceEntity
import kotlinx.coroutines.flow.Flow

/**
 * User Compliance DAO - Google Play Compliance Data Access
 *
 * Handles all database operations for user compliance tracking
 * according to Google Play Health App Policy requirements
 */
@Dao
interface UserComplianceDao {

    // ==== BASIC CRUD OPERATIONS ====

    @Query("SELECT * FROM user_compliance WHERE userId = :userId")
    fun getUserCompliance(userId: String): Flow<UserComplianceEntity?>

    @Query("SELECT * FROM user_compliance WHERE userId = :userId")
    suspend fun getUserComplianceSync(userId: String): UserComplianceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCompliance(compliance: UserComplianceEntity)

    @Update
    suspend fun updateUserCompliance(compliance: UserComplianceEntity)

    @Delete
    suspend fun deleteUserCompliance(compliance: UserComplianceEntity)

    @Query("DELETE FROM user_compliance WHERE userId = :userId")
    suspend fun deleteUserComplianceById(userId: String)

    // ==== COMPLIANCE STATUS QUERIES ====

    @Query("SELECT EXISTS(SELECT 1 FROM user_compliance WHERE userId = :userId)")
    suspend fun hasComplianceRecord(userId: String): Boolean

    @Query("SELECT isCompliant FROM user_compliance WHERE userId = :userId")
    suspend fun isUserCompliant(userId: String): Boolean?

    @Query("SELECT * FROM user_compliance WHERE isCompliant = 1")
    fun getAllCompliantUsers(): Flow<List<UserComplianceEntity>>

    @Query("SELECT * FROM user_compliance WHERE isCompliant = 0 OR needsReview = 1")
    fun getUsersNeedingReview(): Flow<List<UserComplianceEntity>>

    // ==== PROFESSIONAL VERIFICATION QUERIES ====

    @Query("SELECT isProfessionalVerified FROM user_compliance WHERE userId = :userId")
    suspend fun isProfessionalVerified(userId: String): Boolean?

    @Query("SELECT professionalType FROM user_compliance WHERE userId = :userId")
    suspend fun getProfessionalType(userId: String): String?

    @Query("SELECT * FROM user_compliance WHERE isProfessionalVerified = 1")
    fun getAllVerifiedProfessionals(): Flow<List<UserComplianceEntity>>

    @Query("SELECT COUNT(*) FROM user_compliance WHERE isProfessionalVerified = 1 AND professionalType = :type")
    suspend fun getVerifiedProfessionalCount(type: String): Int

    // ==== VERSION MANAGEMENT QUERIES ====

    @Query("SELECT * FROM user_compliance WHERE complianceVersion != :currentVersion")
    fun getUsersWithOutdatedCompliance(currentVersion: String): Flow<List<UserComplianceEntity>>

    @Query("SELECT * FROM user_compliance WHERE medicalDisclaimerVersion != :currentVersion OR medicalDisclaimerVersion IS NULL")
    fun getUsersNeedingMedicalDisclaimerUpdate(currentVersion: String): Flow<List<UserComplianceEntity>>

    @Query("SELECT * FROM user_compliance WHERE privacyPolicyVersion != :currentVersion OR privacyPolicyVersion IS NULL")
    fun getUsersNeedingPrivacyPolicyUpdate(currentVersion: String): Flow<List<UserComplianceEntity>>

    // ==== SPECIFIC CONSENT UPDATES ====

    @Query("""
        UPDATE user_compliance 
        SET hasAcceptedBasicTerms = :accepted,
            basicTermsAcceptedAt = :timestamp,
            basicTermsVersion = :version,
            lastUpdated = :timestamp
        WHERE userId = :userId
    """)
    suspend fun updateBasicTermsConsent(
        userId: String,
        accepted: Boolean,
        timestamp: Long,
        version: String
    )

    @Query("""
        UPDATE user_compliance 
        SET hasAcceptedMedicalDisclaimer = :accepted,
            medicalDisclaimerAcceptedAt = :timestamp,
            medicalDisclaimerVersion = :version,
            lastUpdated = :timestamp
        WHERE userId = :userId
    """)
    suspend fun updateMedicalDisclaimerConsent(
        userId: String,
        accepted: Boolean,
        timestamp: Long,
        version: String
    )

    @Query("""
        UPDATE user_compliance 
        SET isProfessionalVerified = :verified,
            professionalVerifiedAt = :timestamp,
            professionalType = :professionalType,
            professionalLicenseInfo = :licenseInfo,
            lastUpdated = :timestamp
        WHERE userId = :userId
    """)
    suspend fun updateProfessionalVerification(
        userId: String,
        verified: Boolean,
        timestamp: Long,
        professionalType: String?,
        licenseInfo: String?
    )

    @Query("""
        UPDATE user_compliance 
        SET hasAcceptedPrivacyPolicy = :accepted,
            privacyPolicyAcceptedAt = :timestamp,
            privacyPolicyVersion = :version,
            lastUpdated = :timestamp
        WHERE userId = :userId
    """)
    suspend fun updatePrivacyPolicyConsent(
        userId: String,
        accepted: Boolean,
        timestamp: Long,
        version: String
    )

    // ==== COMPLIANCE STATUS UPDATES ====

    @Query("""
        UPDATE user_compliance 
        SET isCompliant = :compliant,
            needsReview = :needsReview,
            complianceVersion = :version,
            lastUpdated = :timestamp
        WHERE userId = :userId
    """)
    suspend fun updateComplianceStatus(
        userId: String,
        compliant: Boolean,
        needsReview: Boolean,
        version: String,
        timestamp: Long
    )

    @Query("""
        UPDATE user_compliance 
        SET needsReview = :needsReview,
            complianceNotes = :notes,
            lastUpdated = :timestamp
        WHERE userId = :userId
    """)
    suspend fun markForReview(
        userId: String,
        needsReview: Boolean,
        notes: String?,
        timestamp: Long
    )

    // ==== AUDIT AND REPORTING QUERIES ====

    @Query("SELECT COUNT(*) FROM user_compliance")
    suspend fun getTotalComplianceRecords(): Int

    @Query("SELECT COUNT(*) FROM user_compliance WHERE isCompliant = 1")
    suspend fun getCompliantUserCount(): Int

    @Query("SELECT COUNT(*) FROM user_compliance WHERE isProfessionalVerified = 1")
    suspend fun getVerifiedProfessionalTotalCount(): Int

    @Query("""
        SELECT COUNT(*) FROM user_compliance 
        WHERE hasAcceptedMedicalDisclaimer = 1 
        AND medicalDisclaimerAcceptedAt >= :startTime 
        AND medicalDisclaimerAcceptedAt <= :endTime
    """)
    suspend fun getMedicalDisclaimerAcceptanceCount(startTime: Long, endTime: Long): Int

    @Query("""
        SELECT * FROM user_compliance 
        WHERE lastUpdated >= :startTime 
        ORDER BY lastUpdated DESC
    """)
    fun getRecentComplianceActivity(startTime: Long): Flow<List<UserComplianceEntity>>

    // ==== GDPR COMPLIANCE ====

    @Query("SELECT * FROM user_compliance WHERE userId = :userId")
    suspend fun exportUserComplianceData(userId: String): UserComplianceEntity?

    @Query("SELECT COUNT(*) FROM user_compliance WHERE userId = :userId")
    suspend fun hasUserComplianceData(userId: String): Int

    // ==== BATCH OPERATIONS ====

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMultipleCompliance(complianceList: List<UserComplianceEntity>)

    @Query("DELETE FROM user_compliance WHERE lastUpdated < :cutoffTime")
    suspend fun deleteOldComplianceRecords(cutoffTime: Long): Int

    @Query("""
        UPDATE user_compliance 
        SET complianceVersion = :newVersion,
            needsReview = 1,
            lastUpdated = :timestamp
        WHERE complianceVersion = :oldVersion
    """)
    suspend fun batchUpdateComplianceVersion(
        oldVersion: String,
        newVersion: String,
        timestamp: Long
    ): Int
}