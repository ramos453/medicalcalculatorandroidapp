package com.example.medicalcalculatorapp.data.db.mapper

import com.example.medicalcalculatorapp.data.db.entity.UserComplianceEntity
import com.example.medicalcalculatorapp.domain.model.*

/**
 * User Compliance Mapper - Clean Architecture Data Mapping
 *
 * Maps between database entities and domain models for user compliance.
 * Handles all conversion logic and null safety according to Google Play requirements.
 */
class UserComplianceMapper {

    /**
     * Convert database entity to domain model
     */
    fun mapEntityToDomain(entity: UserComplianceEntity): UserCompliance {
        return UserCompliance(
            userId = entity.userId,
            basicTermsConsent = mapBasicTermsConsent(entity),
            medicalDisclaimerConsent = mapMedicalDisclaimerConsent(entity),
            professionalVerification = mapProfessionalVerification(entity),
            privacyPolicyConsent = mapPrivacyPolicyConsent(entity),
            complianceStatus = mapComplianceStatus(entity),
            complianceVersion = entity.complianceVersion,
            lastUpdated = entity.lastUpdated,
            createdAt = entity.createdAt,
            auditInfo = mapAuditInfo(entity)
        )
    }

    /**
     * Convert domain model to database entity
     */
    fun mapDomainToEntity(domain: UserCompliance): UserComplianceEntity {
        return UserComplianceEntity(
            userId = domain.userId,

            // Basic Terms
            hasAcceptedBasicTerms = domain.basicTermsConsent?.isAccepted ?: false,
            basicTermsAcceptedAt = domain.basicTermsConsent?.acceptedAt,
            basicTermsVersion = domain.basicTermsConsent?.version,

            // Medical Disclaimer
            hasAcceptedMedicalDisclaimer = domain.medicalDisclaimerConsent?.isAccepted ?: false,
            medicalDisclaimerAcceptedAt = domain.medicalDisclaimerConsent?.acceptedAt,
            medicalDisclaimerVersion = domain.medicalDisclaimerConsent?.version,

            // Professional Verification
            isProfessionalVerified = domain.professionalVerification?.isVerified ?: false,
            professionalVerifiedAt = domain.professionalVerification?.verifiedAt,
            professionalType = domain.professionalVerification?.professionalType?.code,
            professionalLicenseInfo = domain.professionalVerification?.licenseInfo,

            // Privacy Policy
            hasAcceptedPrivacyPolicy = domain.privacyPolicyConsent?.isAccepted ?: false,
            privacyPolicyAcceptedAt = domain.privacyPolicyConsent?.acceptedAt,
            privacyPolicyVersion = domain.privacyPolicyConsent?.version,

            // Compliance Status
            isCompliant = domain.complianceStatus.isCompliant,
            needsReview = domain.complianceStatus.needsReview,
            complianceNotes = domain.complianceStatus.notes,

            // Metadata
            complianceVersion = domain.complianceVersion,
            lastUpdated = domain.lastUpdated,
            createdAt = domain.createdAt,

            // Audit Info
            ipAddress = domain.auditInfo?.ipAddress,
            userAgent = domain.auditInfo?.userAgent,
            consentMethod = domain.auditInfo?.consentMethod?.code ?: ConsentMethod.APP_DIALOG.code
        )
    }

    /**
     * Create new compliance record for user
     */
    fun createNewComplianceRecord(userId: String): UserCompliance {
        val now = System.currentTimeMillis()

        return UserCompliance(
            userId = userId,
            basicTermsConsent = null,
            medicalDisclaimerConsent = null,
            professionalVerification = null,
            privacyPolicyConsent = null,
            complianceStatus = ComplianceStatus(
                isCompliant = false,
                needsReview = false,
                notes = null
            ),
            complianceVersion = UserComplianceEntity.CURRENT_COMPLIANCE_VERSION,
            lastUpdated = now,
            createdAt = now,
            auditInfo = null
        )
    }

    /**
     * Update compliance with new consent
     */
    fun updateWithBasicTermsConsent(
        existing: UserCompliance,
        accepted: Boolean,
        version: String,
        method: ConsentMethod = ConsentMethod.APP_DIALOG
    ): UserCompliance {
        val consentRecord = if (accepted) {
            ConsentRecord(
                isAccepted = true,
                acceptedAt = System.currentTimeMillis(),
                version = version,
                consentMethod = method
            )
        } else null

        return existing.copy(
            basicTermsConsent = consentRecord,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Update compliance with medical disclaimer consent
     */
    fun updateWithMedicalDisclaimerConsent(
        existing: UserCompliance,
        accepted: Boolean,
        version: String,
        method: ConsentMethod = ConsentMethod.APP_DIALOG
    ): UserCompliance {
        val consentRecord = if (accepted) {
            ConsentRecord(
                isAccepted = true,
                acceptedAt = System.currentTimeMillis(),
                version = version,
                consentMethod = method
            )
        } else null

        return existing.copy(
            medicalDisclaimerConsent = consentRecord,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Update compliance with professional verification
     */
    fun updateWithProfessionalVerification(
        existing: UserCompliance,
        verified: Boolean,
        professionalType: ProfessionalType?,
        licenseInfo: String? = null
    ): UserCompliance {
        val verification = if (verified) {
            ProfessionalVerification(
                isVerified = true,
                verifiedAt = System.currentTimeMillis(),
                professionalType = professionalType,
                licenseInfo = licenseInfo
            )
        } else null

        return existing.copy(
            professionalVerification = verification,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Update compliance with privacy policy consent
     */
    fun updateWithPrivacyPolicyConsent(
        existing: UserCompliance,
        accepted: Boolean,
        version: String,
        method: ConsentMethod = ConsentMethod.APP_DIALOG
    ): UserCompliance {
        val consentRecord = if (accepted) {
            ConsentRecord(
                isAccepted = true,
                acceptedAt = System.currentTimeMillis(),
                version = version,
                consentMethod = method
            )
        } else null

        return existing.copy(
            privacyPolicyConsent = consentRecord,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Update compliance status
     */
    fun updateComplianceStatus(
        existing: UserCompliance,
        isCompliant: Boolean,
        needsReview: Boolean,
        notes: String? = null
    ): UserCompliance {
        val newStatus = ComplianceStatus(
            isCompliant = isCompliant,
            needsReview = needsReview,
            notes = notes
        )

        return existing.copy(
            complianceStatus = newStatus,
            lastUpdated = System.currentTimeMillis()
        )
    }

    // Private helper methods for mapping individual components

    private fun mapBasicTermsConsent(entity: UserComplianceEntity): ConsentRecord? {
        return if (entity.hasAcceptedBasicTerms) {
            ConsentRecord(
                isAccepted = true,
                acceptedAt = entity.basicTermsAcceptedAt,
                version = entity.basicTermsVersion,
                consentMethod = ConsentMethod.fromCode(entity.consentMethod)
            )
        } else null
    }

    private fun mapMedicalDisclaimerConsent(entity: UserComplianceEntity): ConsentRecord? {
        return if (entity.hasAcceptedMedicalDisclaimer) {
            ConsentRecord(
                isAccepted = true,
                acceptedAt = entity.medicalDisclaimerAcceptedAt,
                version = entity.medicalDisclaimerVersion,
                consentMethod = ConsentMethod.fromCode(entity.consentMethod)
            )
        } else null
    }

    private fun mapProfessionalVerification(entity: UserComplianceEntity): ProfessionalVerification? {
        return if (entity.isProfessionalVerified) {
            ProfessionalVerification(
                isVerified = true,
                verifiedAt = entity.professionalVerifiedAt,
                professionalType = entity.professionalType?.let { ProfessionalType.fromCode(it) },
                licenseInfo = entity.professionalLicenseInfo
            )
        } else null
    }

    private fun mapPrivacyPolicyConsent(entity: UserComplianceEntity): ConsentRecord? {
        return if (entity.hasAcceptedPrivacyPolicy) {
            ConsentRecord(
                isAccepted = true,
                acceptedAt = entity.privacyPolicyAcceptedAt,
                version = entity.privacyPolicyVersion,
                consentMethod = ConsentMethod.fromCode(entity.consentMethod)
            )
        } else null
    }

    private fun mapComplianceStatus(entity: UserComplianceEntity): ComplianceStatus {
        return ComplianceStatus(
            isCompliant = entity.isCompliant,
            needsReview = entity.needsReview,
            notes = entity.complianceNotes
        )
    }

    private fun mapAuditInfo(entity: UserComplianceEntity): AuditInfo? {
        return if (entity.ipAddress != null || entity.userAgent != null) {
            AuditInfo(
                ipAddress = entity.ipAddress,
                userAgent = entity.userAgent,
                consentMethod = ConsentMethod.fromCode(entity.consentMethod)
            )
        } else null
    }

    /**
     * Batch conversion helpers
     */
    fun mapEntitiesToDomain(entities: List<UserComplianceEntity>): List<UserCompliance> {
        return entities.map { mapEntityToDomain(it) }
    }

    fun mapDomainToEntities(domainList: List<UserCompliance>): List<UserComplianceEntity> {
        return domainList.map { mapDomainToEntity(it) }
    }

    /**
     * Validation helpers
     */
    fun validateDomainModel(domain: UserCompliance): List<String> {
        val errors = mutableListOf<String>()

        if (domain.userId.isBlank()) {
            errors.add("User ID cannot be blank")
        }

        if (domain.complianceVersion.isBlank()) {
            errors.add("Compliance version cannot be blank")
        }

        if (domain.createdAt > domain.lastUpdated) {
            errors.add("Created date cannot be after last updated date")
        }

        // Validate consent records
        domain.basicTermsConsent?.let { consent ->
            if (consent.isAccepted && consent.acceptedAt == null) {
                errors.add("Basic terms consent accepted but missing timestamp")
            }
        }

        domain.medicalDisclaimerConsent?.let { consent ->
            if (consent.isAccepted && consent.acceptedAt == null) {
                errors.add("Medical disclaimer consent accepted but missing timestamp")
            }
        }

        domain.professionalVerification?.let { verification ->
            if (verification.isVerified && verification.verifiedAt == null) {
                errors.add("Professional verification accepted but missing timestamp")
            }
        }

        domain.privacyPolicyConsent?.let { consent ->
            if (consent.isAccepted && consent.acceptedAt == null) {
                errors.add("Privacy policy consent accepted but missing timestamp")
            }
        }

        return errors
    }
}