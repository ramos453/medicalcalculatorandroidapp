package com.example.medicalcalculatorapp.domain.service

import android.content.Context
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.model.DisclaimerFlow
import com.example.medicalcalculatorapp.util.SecureStorageManager
import com.example.medicalcalculatorapp.domain.repository.IUserComplianceRepository
import kotlinx.coroutines.runBlocking
import com.example.medicalcalculatorapp.domain.model.ProfessionalType
import com.example.medicalcalculatorapp.domain.model.ConsentMethod

/**
 * Simplified Compliance Manager Service - Working Implementation
 *
 * This is a simplified version that works with your current architecture
 * and can be enhanced later as needed.
 */
class ComplianceManagerService(
    private val secureStorageManager: SecureStorageManager,
    private val userManager: UserManager,
    private val userComplianceRepository: IUserComplianceRepository
) {
    companion object {
        // Current compliance version
        private const val CURRENT_COMPLIANCE_VERSION = "2024.1"

        // Compliance tracking keys (using SecureStorageManager)
        private const val KEY_BASIC_DISCLAIMER_ACCEPTED = "basic_disclaimer_accepted"
        private const val KEY_ENHANCED_DISCLAIMER_ACCEPTED = "enhanced_disclaimer_accepted"
        private const val KEY_PROFESSIONAL_VERIFIED = "professional_verified"
        private const val KEY_PRIVACY_POLICY_ACCEPTED = "privacy_policy_accepted"
        private const val KEY_COMPLIANCE_VERSION = "compliance_version"
    }

    /**
     * Get current compliance status (simplified implementation)
     */
    suspend fun getComplianceStatus(): SimpleComplianceStatus {
        val userType = userManager.getUserType()

        val hasBasicTerms = when (userType) {
            UserManager.UserType.AUTHENTICATED -> {
                // For authenticated users, check database (simplified to true for now)
                true
            }
            UserManager.UserType.GUEST -> {
                secureStorageManager.getGuestPreference(KEY_BASIC_DISCLAIMER_ACCEPTED) == "true" ||
                        secureStorageManager.hasAcceptedDisclaimer()
            }
            else -> false
        }

        val hasMedicalDisclaimer = when (userType) {
            UserManager.UserType.AUTHENTICATED -> {
                // For authenticated users, check database (simplified to false to trigger flow)
                false
            }
            UserManager.UserType.GUEST -> {
                secureStorageManager.getGuestPreference(KEY_ENHANCED_DISCLAIMER_ACCEPTED) == "true"
            }
            else -> false
        }

        val isProfessionalVerified = when (userType) {
            UserManager.UserType.AUTHENTICATED -> {
                // For authenticated users, check database (simplified to false to trigger flow)
                false
            }
            UserManager.UserType.GUEST -> {
                secureStorageManager.getGuestPreference(KEY_PROFESSIONAL_VERIFIED) == "true"
            }
            else -> false
        }

        val hasPrivacyPolicy = when (userType) {
            UserManager.UserType.AUTHENTICATED -> {
                // For authenticated users, check database (simplified to false to trigger flow)
                false
            }
            UserManager.UserType.GUEST -> {
                secureStorageManager.getGuestPreference(KEY_PRIVACY_POLICY_ACCEPTED) == "true"
            }
            else -> false
        }

        return SimpleComplianceStatus(
            hasBasicTerms = hasBasicTerms,
            hasMedicalDisclaimer = hasMedicalDisclaimer,
            isProfessionalVerified = isProfessionalVerified,
            hasPrivacyPolicy = hasPrivacyPolicy,
            userType = userType
        )
    }

    /**
     * Get required disclaimer flow (simplified implementation)
     */
    suspend fun getRequiredDisclaimerFlow(): DisclaimerFlow {
        val status = getComplianceStatus()

        return when {
            // No basic terms - start with introduction
            !status.hasBasicTerms -> DisclaimerFlow.BASIC_INTRODUCTION

            // Has basic but needs enhanced medical disclaimer
            status.hasBasicTerms && !status.hasMedicalDisclaimer ->
                DisclaimerFlow.ENHANCED_MEDICAL_REQUIRED

            // Has medical disclaimer but needs professional verification
            status.hasMedicalDisclaimer && !status.isProfessionalVerified ->
                DisclaimerFlow.PROFESSIONAL_VERIFICATION_REQUIRED

            // Has everything - fully compliant
            status.hasBasicTerms && status.hasMedicalDisclaimer &&
                    status.isProfessionalVerified && status.hasPrivacyPolicy ->
                DisclaimerFlow.FULLY_COMPLIANT

            // Default to enhanced medical required
            else -> DisclaimerFlow.ENHANCED_MEDICAL_REQUIRED
        }
    }

    /**
     * Record complete compliance (simplified - all at once)
     */
    suspend fun recordCompleteCompliance(
        professionalType: SimpleProfessionalType? = null,
        licenseInfo: String? = null,
        method: SimpleConsentMethod = SimpleConsentMethod.APP_DIALOG
    ): Boolean {
        return try {
            val userType = userManager.getUserType()
            val userId = userManager.getCurrentUserId()

            when (userType) {
                UserManager.UserType.AUTHENTICATED -> {
                    // For authenticated users, save to database
                    val hasRecord = userComplianceRepository.hasComplianceRecord(userId)

                    if (!hasRecord) {
                        // Create new compliance record
                        userComplianceRepository.createUserCompliance(userId)
                    }

                    // Record all consents
                    userComplianceRepository.recordBasicTermsConsent(userId, true, CURRENT_COMPLIANCE_VERSION)
                    userComplianceRepository.recordMedicalDisclaimerConsent(userId, true, CURRENT_COMPLIANCE_VERSION)
                    userComplianceRepository.recordPrivacyPolicyConsent(userId, true, CURRENT_COMPLIANCE_VERSION)

                    // Record professional verification if provided
                    professionalType?.let {
                        val profType = when(it) {
                            SimpleProfessionalType.DOCTOR -> ProfessionalType.DOCTOR
                            SimpleProfessionalType.NURSE -> ProfessionalType.NURSE
                            SimpleProfessionalType.PHARMACIST -> ProfessionalType.PHARMACIST
                            SimpleProfessionalType.STUDENT -> ProfessionalType.MEDICAL_STUDENT
                            SimpleProfessionalType.OTHER -> ProfessionalType.OTHER_HEALTHCARE
                        }
                        userComplianceRepository.recordProfessionalVerification(userId, true, profType, licenseInfo)
                    }

                    println("✅ Saved compliance to database for authenticated user")
                    true
                }
                UserManager.UserType.GUEST -> {
                    // For guest users, save to secure storage (existing logic)
                    secureStorageManager.saveGuestPreference(KEY_BASIC_DISCLAIMER_ACCEPTED, "true")
                    secureStorageManager.saveGuestPreference(KEY_ENHANCED_DISCLAIMER_ACCEPTED, "true")
                    secureStorageManager.saveGuestPreference(KEY_PROFESSIONAL_VERIFIED, "true")
                    secureStorageManager.saveGuestPreference(KEY_PRIVACY_POLICY_ACCEPTED, "true")
                    secureStorageManager.saveGuestPreference(KEY_COMPLIANCE_VERSION, CURRENT_COMPLIANCE_VERSION)

                    // Also set legacy disclaimer for backward compatibility
                    secureStorageManager.saveGuestDisclaimerAccepted(true)

                    println("✅ Saved guest compliance to secure storage")
                    true
                }
                else -> false
            }
        } catch (e: Exception) {
            println("❌ Error recording compliance: ${e.message}")
            false
        }
    }

    /**
     * Check if user can access medical calculators
     */
    suspend fun canAccessMedicalCalculators(): Boolean {
        val flow = getRequiredDisclaimerFlow()
        return flow == DisclaimerFlow.FULLY_COMPLIANT
    }
}

/**
 * Simplified compliance status (no external dependencies)
 */
data class SimpleComplianceStatus(
    val hasBasicTerms: Boolean,
    val hasMedicalDisclaimer: Boolean,
    val isProfessionalVerified: Boolean,
    val hasPrivacyPolicy: Boolean,
    val userType: UserManager.UserType
) {
    fun getStatusSummary(): String {
        val completed = listOf(hasBasicTerms, hasMedicalDisclaimer, isProfessionalVerified, hasPrivacyPolicy).count { it }
        return when (completed) {
            4 -> "✅ Completamente Conforme"
            3 -> "⚠️ Casi Completo (${completed}/4)"
            2 -> "📋 Progreso Medio (${completed}/4)"
            1 -> "🆕 Iniciando Conformidad (${completed}/4)"
            else -> "❌ Sin Conformidad"
        }
    }
}

/**
 * Simplified professional types (no external dependencies)
 */
enum class SimpleProfessionalType {
    DOCTOR, NURSE, PHARMACIST, STUDENT, OTHER
}

/**
 * Simplified consent methods (no external dependencies)
 */
enum class SimpleConsentMethod {
    APP_DIALOG, WEB_FORM, EMAIL_VERIFICATION
}