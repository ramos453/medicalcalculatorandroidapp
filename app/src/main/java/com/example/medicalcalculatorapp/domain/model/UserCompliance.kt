package com.example.medicalcalculatorapp.domain.model

/**
 * Complete User Compliance Domain Models
 *
 * Contains all compliance-related domain models for Google Play Health App Policy compliance.
 * Represents user compliance status in the domain layer, independent of database implementation.
 */

// ==== MAIN DOMAIN MODEL ====

/**
 * User Compliance Domain Model - Clean Architecture
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

// ==== CORE DATA CLASSES ====

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

    fun getAcceptedDate(): String {
        return if (acceptedAt != null) {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(acceptedAt))
        } else {
            "Not accepted"
        }
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

    fun isValid(): Boolean {
        return if (isVerified) {
            verifiedAt != null && professionalType != null
        } else {
            true // Not verified is valid state
        }
    }

    fun getVerifiedDate(): String {
        return if (verifiedAt != null) {
            java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(java.util.Date(verifiedAt))
        } else {
            "Not verified"
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
    val consentMethod: ConsentMethod,
    val sessionId: String? = null,
    val deviceInfo: String? = null
)

// ==== ENUMS ====

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

// ==== REPORTING AND AUDIT MODELS ====

/**
 * Compliance statistics for reporting and admin dashboards
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

    fun getCompliancePercentage(): String {
        return "${(getComplianceRate() * 100).toInt()}%"
    }

    fun getProfessionalPercentage(): String {
        return "${(getProfessionalVerificationRate() * 100).toInt()}%"
    }
}

/**
 * Individual compliance events for audit trail
 */
data class ComplianceEvent(
    val eventType: ComplianceEventType,
    val timestamp: Long,
    val details: String,
    val version: String?,
    val method: ConsentMethod,
    val userId: String,
    val sessionId: String? = null
) {
    fun getEventDate(): String {
        return java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            .format(java.util.Date(timestamp))
    }
}

/**
 * Types of compliance events for audit trail
 */
enum class ComplianceEventType(val description: String) {
    BASIC_TERMS_ACCEPTED("Basic terms accepted"),
    MEDICAL_DISCLAIMER_ACCEPTED("Medical disclaimer accepted"),
    PROFESSIONAL_VERIFIED("Professional status verified"),
    PRIVACY_POLICY_ACCEPTED("Privacy policy accepted"),
    COMPLIANCE_UPDATED("Compliance status updated"),
    REVIEW_REQUIRED("Marked for review"),
    REVIEW_CLEARED("Review flag cleared"),
    VERSION_UPDATED("Compliance version updated"),
    DATA_EXPORTED("Compliance data exported"),
    CONSENT_WITHDRAWN("Consent withdrawn");

    fun getEventDescription(): String = description
}

/**
 * Compliance audit trail for individual users
 */
data class ComplianceAuditTrail(
    val userId: String,
    val events: List<ComplianceEvent>,
    val currentStatus: UserCompliance,
    val generatedAt: Long
) {
    fun getEventCount(): Int = events.size

    fun getLatestEvent(): ComplianceEvent? = events.maxByOrNull { it.timestamp }

    fun getEventsByType(type: ComplianceEventType): List<ComplianceEvent> {
        return events.filter { it.eventType == type }
    }

    fun getEventsInDateRange(startDate: Long, endDate: Long): List<ComplianceEvent> {
        return events.filter { it.timestamp in startDate..endDate }
    }
}

/**
 * User compliance data export for GDPR compliance
 */
data class UserComplianceExport(
    val userId: String,
    val compliance: UserCompliance,
    val auditTrail: ComplianceAuditTrail,
    val exportedAt: Long,
    val exportFormat: String = "JSON"
) {
    fun toJsonString(): String {
        return """
        {
            "userId": "$userId",
            "exportedAt": $exportedAt,
            "exportFormat": "$exportFormat",
            "compliance": {
                "isFullyCompliant": ${compliance.isFullyCompliant()},
                "complianceVersion": "${compliance.complianceVersion}",
                "basicTermsAccepted": ${compliance.basicTermsConsent?.isAccepted ?: false},
                "medicalDisclaimerAccepted": ${compliance.medicalDisclaimerConsent?.isAccepted ?: false},
                "professionalVerified": ${compliance.professionalVerification?.isVerified ?: false},
                "privacyPolicyAccepted": ${compliance.privacyPolicyConsent?.isAccepted ?: false},
                "lastUpdated": ${compliance.lastUpdated},
                "createdAt": ${compliance.createdAt}
            },
            "auditTrail": {
                "eventCount": ${auditTrail.events.size},
                "generatedAt": ${auditTrail.generatedAt}
            }
        }
        """.trimIndent()
    }

    fun getExportSize(): String {
        val jsonString = toJsonString()
        val sizeInBytes = jsonString.toByteArray().size
        return when {
            sizeInBytes < 1024 -> "$sizeInBytes bytes"
            sizeInBytes < 1024 * 1024 -> "${sizeInBytes / 1024} KB"
            else -> "${sizeInBytes / (1024 * 1024)} MB"
        }
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

    fun getErrorCount(): Int = errors.size

    fun getWarningCount(): Int = warnings.size

    fun getSummary(): String {
        return when {
            hasErrors() -> "❌ Validation Failed: ${errors.size} errors, ${warnings.size} warnings"
            hasWarnings() -> "⚠️ Validation Passed with Warnings: ${warnings.size} warnings"
            else -> "✅ Validation Passed"
        }
    }

    fun getDetailedReport(): String {
        val report = StringBuilder()
        report.appendLine("Compliance Validation Report")
        report.appendLine("Checked at: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(checkedAt))}")
        report.appendLine("Status: ${getSummary()}")

        if (hasErrors()) {
            report.appendLine("\nErrors:")
            errors.forEach { error ->
                report.appendLine("  - $error")
            }
        }

        if (hasWarnings()) {
            report.appendLine("\nWarnings:")
            warnings.forEach { warning ->
                report.appendLine("  - $warning")
            }
        }

        return report.toString()
    }
}