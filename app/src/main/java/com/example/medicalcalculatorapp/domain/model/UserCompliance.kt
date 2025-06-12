package com.example.medicalcalculatorapp.domain.model

/**
 * User Compliance Domain Model - Clean Architecture
 *
 * Represents user compliance status in the domain layer,
 * independent of database implementation details.
 * Follows Google Play Health App Policy requirements.
 */
data class UserCompliance(
    val userId: String,

    // Basic Consent
    val basicTermsConsent: ConsentRecord?,

    // Medical Disclaimer
    val medicalDisclaimerConsent: ConsentRecord?,

    // Professional Verification
    val professionalVerification: ProfessionalVerification?,

    // Privacy Policy
    val privacyPolicyConsent: ConsentRecord?,

    // Overall Status
    val complianceStatus: ComplianceStatus,

    // Metadata
    val complianceVersion: String,
    val lastUpdated: Long,
    val createdAt: Long,
    val auditInfo: AuditInfo?
) {
    /**
     * Check if user meets all compliance requirements
     */
    fun isFullyCompliant(): Boolean {
        return basicTermsConsent?.isAccepted == true &&
                medicalDisclaimerConsent?.isAccepted == true &&
                professionalVerification?.isVerified == true &&
                privacyPolicyConsent?.isAccepted == true &&
                complianceStatus.isCompliant
    }

    /**
     * Check if compliance needs update due to version changes
     */
    fun needsUpdate(currentVersion: String): Boolean {
        return complianceVersion != currentVersion ||
                complianceStatus.needsReview ||
                isExpired()
    }

    /**
     * Check if any consent has expired (if applicable)
     */
    fun isExpired(): Boolean {
        // For medical apps, compliance typically doesn't expire
        // but this allows for future implementation
        return false
    }

    /**
     * Get missing compliance requirements
     */
    fun getMissingRequirements(): List<ComplianceRequirement> {
        val missing = mutableListOf<ComplianceRequirement>()

        if (basicTermsConsent?.isAccepted != true) {
            missing.add(ComplianceRequirement.BASIC_TERMS)
        }
        if (medicalDisclaimerConsent?.isAccepted != true) {
            missing.add(ComplianceRequirement.MEDICAL_DISCLAIMER)
        }
        if (professionalVerification?.isVerified != true) {
            missing.add(ComplianceRequirement.PROFESSIONAL_VERIFICATION)
        }
        if (privacyPolicyConsent?.isAccepted != true) {
            missing.add(ComplianceRequirement.PRIVACY_POLICY)
        }

        return missing
    }

    /**
     * Get compliance progress as percentage
     */
    fun getComplianceProgress(): Float {
        val total = 4f // Total requirements
        val completed = listOf(
            basicTermsConsent?.isAccepted == true,
            medicalDisclaimerConsent?.isAccepted == true,
            professionalVerification?.isVerified == true,
            privacyPolicyConsent?.isAccepted == true
        ).count { it }

        return completed / total
    }
}

/**
 * Represents a single consent record
 */
data class ConsentRecord(
    val isAccepted: Boolean,
    val acceptedAt: Long?,
    val version: String?,
    val consentMethod: ConsentMethod
) {
    fun isValidForVersion(requiredVersion: String): Boolean {
        return isAccepted && version == requiredVersion
    }
}

/**
 * Professional verification details
 */
data class ProfessionalVerification(
    val isVerified: Boolean,
    val verifiedAt: Long?,
    val professionalType: ProfessionalType?,
    val licenseInfo: String?
) {
    fun getDisplayText(): String {
        return when (professionalType) {
            ProfessionalType.DOCTOR -> "Médico Verificado"
            ProfessionalType.NURSE -> "Enfermero/a Verificado/a"
            ProfessionalType.PHARMACIST -> "Farmacéutico/a Verificado/a"
            ProfessionalType.MEDICAL_STUDENT -> "Estudiante de Medicina"
            ProfessionalType.RESEARCHER -> "Investigador Médico"
            ProfessionalType.OTHER_HEALTHCARE -> "Profesional de Salud"
            null -> "No Verificado"
        }
    }
}

/**
 * Overall compliance status
 */
data class ComplianceStatus(
    val isCompliant: Boolean,
    val needsReview: Boolean,
    val notes: String?
) {
    fun getStatusText(): String {
        return when {
            isCompliant -> "✅ Completamente Conforme"
            needsReview -> "⚠️ Requiere Revisión"
            else -> "❌ Conformidad Incompleta"
        }
    }
}

/**
 * Audit information for compliance tracking
 */
data class AuditInfo(
    val ipAddress: String?,
    val userAgent: String?,
    val consentMethod: ConsentMethod
)

/**
 * Types of compliance requirements
 */
enum class ComplianceRequirement(val displayName: String) {
    BASIC_TERMS("Términos Básicos"),
    MEDICAL_DISCLAIMER("Aviso Médico"),
    PROFESSIONAL_VERIFICATION("Verificación Profesional"),
    PRIVACY_POLICY("Política de Privacidad");

    fun getDescription(): String {
        return when (this) {
            BASIC_TERMS -> "Aceptar los términos básicos de uso de la aplicación"
            MEDICAL_DISCLAIMER -> "Aceptar el aviso médico y responsabilidades profesionales"
            PROFESSIONAL_VERIFICATION -> "Verificar estatus como profesional de salud licenciado"
            PRIVACY_POLICY -> "Aceptar la política de privacidad y manejo de datos"
        }
    }
}

/**
 * Professional types for verification
 */
enum class ProfessionalType(val code: String, val displayName: String) {
    DOCTOR("DOCTOR", "Médico"),
    NURSE("NURSE", "Enfermero/a"),
    PHARMACIST("PHARMACIST", "Farmacéutico/a"),
    MEDICAL_STUDENT("MEDICAL_STUDENT", "Estudiante de Medicina"),
    RESEARCHER("RESEARCHER", "Investigador Médico"),
    OTHER_HEALTHCARE("OTHER_HEALTHCARE", "Otro Profesional de Salud");

    companion object {
        fun fromCode(code: String): ProfessionalType? {
            return values().find { it.code == code }
        }

        fun getAllOptions(): List<ProfessionalType> {
            return values().toList()
        }
    }
}

/**
 * Methods of obtaining consent
 */
enum class ConsentMethod(val code: String, val displayName: String) {
    APP_DIALOG("APP_DIALOG", "Diálogo en la Aplicación"),
    WEB_FORM("WEB_FORM", "Formulario Web"),
    EMAIL_VERIFICATION("EMAIL_VERIFICATION", "Verificación por Email"),
    PHONE_VERIFICATION("PHONE_VERIFICATION", "Verificación Telefónica");

    companion object {
        fun fromCode(code: String): ConsentMethod {
            return values().find { it.code == code } ?: APP_DIALOG
        }
    }
}

/**
 * Compliance builder for easy creation
 */
class UserComplianceBuilder(private val userId: String) {
    private var basicTermsConsent: ConsentRecord? = null
    private var medicalDisclaimerConsent: ConsentRecord? = null
    private var professionalVerification: ProfessionalVerification? = null
    private var privacyPolicyConsent: ConsentRecord? = null
    private var complianceStatus = ComplianceStatus(false, false, null)
    private var complianceVersion = "2024.1"
    private var auditInfo: AuditInfo? = null

    fun withBasicTerms(accepted: Boolean, version: String, method: ConsentMethod = ConsentMethod.APP_DIALOG): UserComplianceBuilder {
        basicTermsConsent = ConsentRecord(
            isAccepted = accepted,
            acceptedAt = if (accepted) System.currentTimeMillis() else null,
            version = if (accepted) version else null,
            consentMethod = method
        )
        return this
    }

    fun withMedicalDisclaimer(accepted: Boolean, version: String, method: ConsentMethod = ConsentMethod.APP_DIALOG): UserComplianceBuilder {
        medicalDisclaimerConsent = ConsentRecord(
            isAccepted = accepted,
            acceptedAt = if (accepted) System.currentTimeMillis() else null,
            version = if (accepted) version else null,
            consentMethod = method
        )
        return this
    }

    fun withProfessionalVerification(verified: Boolean, type: ProfessionalType?, licenseInfo: String? = null): UserComplianceBuilder {
        professionalVerification = ProfessionalVerification(
            isVerified = verified,
            verifiedAt = if (verified) System.currentTimeMillis() else null,
            professionalType = if (verified) type else null,
            licenseInfo = licenseInfo
        )
        return this
    }

    fun withPrivacyPolicy(accepted: Boolean, version: String, method: ConsentMethod = ConsentMethod.APP_DIALOG): UserComplianceBuilder {
        privacyPolicyConsent = ConsentRecord(
            isAccepted = accepted,
            acceptedAt = if (accepted) System.currentTimeMillis() else null,
            version = if (accepted) version else null,
            consentMethod = method
        )
        return this
    }

    fun withComplianceVersion(version: String): UserComplianceBuilder {
        complianceVersion = version
        return this
    }

    fun withAuditInfo(ipAddress: String?, userAgent: String?, method: ConsentMethod): UserComplianceBuilder {
        auditInfo = AuditInfo(ipAddress, userAgent, method)
        return this
    }

    fun build(): UserCompliance {
        val now = System.currentTimeMillis()

        // Auto-calculate compliance status
        val isCompliant = basicTermsConsent?.isAccepted == true &&
                medicalDisclaimerConsent?.isAccepted == true &&
                professionalVerification?.isVerified == true &&
                privacyPolicyConsent?.isAccepted == true

        val finalComplianceStatus = ComplianceStatus(
            isCompliant = isCompliant,
            needsReview = complianceStatus.needsReview,
            notes = complianceStatus.notes
        )

        return UserCompliance(
            userId = userId,
            basicTermsConsent = basicTermsConsent,
            medicalDisclaimerConsent = medicalDisclaimerConsent,
            professionalVerification = professionalVerification,
            privacyPolicyConsent = privacyPolicyConsent,
            complianceStatus = finalComplianceStatus,
            complianceVersion = complianceVersion,
            lastUpdated = now,
            createdAt = now,
            auditInfo = auditInfo
        )
    }
}