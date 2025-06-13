package com.example.medicalcalculatorapp.domain.model

/**
 * Disclaimer Flow Control - Google Play Health App Policy Compliance
 *
 * Represents different disclaimer flows based on user status and compliance requirements.
 * This follows Google Play Health App Policy 2024 requirements for progressive disclosure
 * and ensures users understand medical liability at appropriate points in the app flow.
 */
enum class DisclaimerFlow {
    /**
     * Show basic app introduction and terms for new users
     * - Basic app terms of use
     * - General privacy policy
     * - Introduction to medical app concept
     */
    BASIC_INTRODUCTION,

    /**
     * Show enhanced medical disclaimer for calculator access
     * - Professional medical disclaimers
     * - Liability and responsibility warnings
     * - Educational vs clinical use distinction
     */
    ENHANCED_MEDICAL_REQUIRED,

    /**
     * Show professional verification process
     * - Professional licensing verification
     * - Medical credentials confirmation
     * - Professional responsibility acceptance
     */
    PROFESSIONAL_VERIFICATION_REQUIRED,

    /**
     * Show updated compliance requirements
     * - Policy version updates
     * - New regulatory requirements
     * - Re-consent for updated terms
     */
    COMPLIANCE_UPDATE_REQUIRED,

    /**
     * User has completed all requirements
     * - All disclaimers accepted
     * - Professional status verified
     * - Full app access granted
     */
    FULLY_COMPLIANT;

    /**
     * Get user-friendly description of this disclaimer flow
     */
    fun getDescription(): String {
        return when (this) {
            BASIC_INTRODUCTION -> "Introducción básica y términos de uso"
            ENHANCED_MEDICAL_REQUIRED -> "Aviso médico profesional requerido"
            PROFESSIONAL_VERIFICATION_REQUIRED -> "Verificación profesional médica requerida"
            COMPLIANCE_UPDATE_REQUIRED -> "Actualización de políticas requerida"
            FULLY_COMPLIANT -> "Completamente conforme - acceso completo"
        }
    }

    /**
     * Check if this flow requires user interaction
     */
    fun requiresUserInteraction(): Boolean {
        return this != FULLY_COMPLIANT
    }

    /**
     * Check if this flow allows app access
     */
    fun allowsAppAccess(): Boolean {
        return this == FULLY_COMPLIANT
    }

    /**
     * Get the priority level (higher numbers = more urgent)
     */
    fun getPriority(): Int {
        return when (this) {
            BASIC_INTRODUCTION -> 1
            ENHANCED_MEDICAL_REQUIRED -> 3
            PROFESSIONAL_VERIFICATION_REQUIRED -> 4
            COMPLIANCE_UPDATE_REQUIRED -> 5
            FULLY_COMPLIANT -> 0
        }
    }

    /**
     * Get the next logical flow step (if any)
     */
    fun getNextFlow(): DisclaimerFlow? {
        return when (this) {
            BASIC_INTRODUCTION -> ENHANCED_MEDICAL_REQUIRED
            ENHANCED_MEDICAL_REQUIRED -> PROFESSIONAL_VERIFICATION_REQUIRED
            PROFESSIONAL_VERIFICATION_REQUIRED -> FULLY_COMPLIANT
            COMPLIANCE_UPDATE_REQUIRED -> FULLY_COMPLIANT
            FULLY_COMPLIANT -> null // No next step
        }
    }

    /**
     * Check if this flow should block app access
     */
    fun blocksAppAccess(): Boolean {
        return when (this) {
            BASIC_INTRODUCTION -> true
            ENHANCED_MEDICAL_REQUIRED -> true
            PROFESSIONAL_VERIFICATION_REQUIRED -> false // Can use app but with limitations
            COMPLIANCE_UPDATE_REQUIRED -> true
            FULLY_COMPLIANT -> false
        }
    }

    /**
     * Get recommended action for this flow
     */
    fun getRecommendedAction(): String {
        return when (this) {
            BASIC_INTRODUCTION -> "Mostrar términos básicos y introducción"
            ENHANCED_MEDICAL_REQUIRED -> "Mostrar aviso médico profesional"
            PROFESSIONAL_VERIFICATION_REQUIRED -> "Solicitar verificación profesional"
            COMPLIANCE_UPDATE_REQUIRED -> "Mostrar actualización de políticas"
            FULLY_COMPLIANT -> "Permitir acceso completo a la aplicación"
        }
    }

    /**
     * Get flow status for logging/debugging
     */
    fun getDebugInfo(): String {
        return """
            DisclaimerFlow: $name
            Description: ${getDescription()}
            Priority: ${getPriority()}
            Requires Interaction: ${requiresUserInteraction()}
            Allows Access: ${allowsAppAccess()}
            Blocks Access: ${blocksAppAccess()}
            Next Step: ${getNextFlow()?.name ?: "None"}
            Action: ${getRecommendedAction()}
        """.trimIndent()
    }
}