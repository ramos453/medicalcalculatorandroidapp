// Create this file: app/src/main/java/com/example/medicalcalculatorapp/domain/model/DisclaimerFlow.kt

package com.example.medicalcalculatorapp.domain.model

/**
 * Represents different disclaimer flows based on user status and compliance requirements
 *
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
            FULLY_COMPLIANT -> null
        }
    }

    companion object {
        /**
         * Get the appropriate flow for a new user
         */
        fun getInitialFlow(): DisclaimerFlow {
            return BASIC_INTRODUCTION
        }

        /**
         * Get the flow that requires the highest priority action
         */
        fun getHighestPriorityFlow(flows: List<DisclaimerFlow>): DisclaimerFlow? {
            return flows.maxByOrNull { it.getPriority() }
        }
    }
}