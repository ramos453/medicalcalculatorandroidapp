package com.example.medicalcalculatorapp.util

import android.content.Context
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.presentation.auth.PrivacyAndDisclaimerDialogFragment
import com.example.medicalcalculatorapp.data.user.UserManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Medical Access Controller - Ensures compliance with Google Play medical app requirements
 *
 * This controller implements a progressive access control system that:
 * 1. Verifies professional medical status
 * 2. Ensures educational disclaimers are accepted
 * 3. Provides educational alternatives for non-professionals
 * 4. Logs compliance for Google Play documentation
 */
class MedicalAccessController(
    private val context: Context,
    private val userManager: UserManager
) {

    private val secureStorageManager = SecureStorageManager(context)

    // Access levels for different user types
    enum class AccessLevel {
        FULL_ACCESS,              // Medical professional with all disclaimers
        REQUIRES_VERIFICATION,    // Needs professional verification
        REQUIRES_DISCLAIMER,      // Needs enhanced medical disclaimer
        EDUCATIONAL_ONLY,         // Non-professional, educational access only
        ACCESS_DENIED            // Doesn't meet minimum requirements
    }

    // Compliance status tracking
    data class ComplianceStatus(
        val accessLevel: AccessLevel,
        val hasProfessionalVerification: Boolean,
        val hasBasicDisclaimer: Boolean,
        val hasEnhancedDisclaimer: Boolean,
        val lastVerificationTime: Long,
        val accessAttempts: Int
    )

    /**
     * Main access control method - call this before opening any medical calculator
     */
    fun checkAccessAndExecute(
        fragment: Fragment,
        calculatorName: String,
        onAccessGranted: () -> Unit
    ) {
        val complianceStatus = getCurrentComplianceStatus()

        when (complianceStatus.accessLevel) {
            AccessLevel.FULL_ACCESS -> {
                logCompliantAccess(calculatorName)
                onAccessGranted()
            }

            AccessLevel.REQUIRES_VERIFICATION -> {
                showProfessionalVerificationDialog(fragment, calculatorName, onAccessGranted)
            }

            AccessLevel.REQUIRES_DISCLAIMER -> {
                showEnhancedDisclaimerDialog(fragment, calculatorName, onAccessGranted)
            }

            AccessLevel.EDUCATIONAL_ONLY -> {
                showEducationalAccessDialog(fragment, calculatorName, onAccessGranted)
            }

            AccessLevel.ACCESS_DENIED -> {
                showAccessDeniedDialog(fragment)
            }
        }
    }

    /**
     * Get current compliance status based on user state
     */
    private fun getCurrentComplianceStatus(): ComplianceStatus {
        val hasBasicDisclaimer = secureStorageManager.hasAcceptedDisclaimer()
        val hasProfessionalVerification = secureStorageManager.getGuestPreference("professional_verified", "false") == "true"
        val hasEnhancedDisclaimer = secureStorageManager.getGuestPreference("enhanced_disclaimer", "false") == "true"
        val lastVerificationTime = secureStorageManager.getGuestPreference("last_verification", "0").toLongOrNull() ?: 0L
        val accessAttempts = secureStorageManager.getGuestPreference("access_attempts", "0").toIntOrNull() ?: 0

        val accessLevel = when {
            hasBasicDisclaimer && hasProfessionalVerification && hasEnhancedDisclaimer -> {
                AccessLevel.FULL_ACCESS
            }
            hasBasicDisclaimer && hasProfessionalVerification -> {
                AccessLevel.REQUIRES_DISCLAIMER
            }
            hasBasicDisclaimer -> {
                AccessLevel.REQUIRES_VERIFICATION
            }
            else -> {
                AccessLevel.EDUCATIONAL_ONLY
            }
        }

        return ComplianceStatus(
            accessLevel = accessLevel,
            hasProfessionalVerification = hasProfessionalVerification,
            hasBasicDisclaimer = hasBasicDisclaimer,
            hasEnhancedDisclaimer = hasEnhancedDisclaimer,
            lastVerificationTime = lastVerificationTime,
            accessAttempts = accessAttempts
        )
    }

    /**
     * Show professional verification dialog
     */
    private fun showProfessionalVerificationDialog(
        fragment: Fragment,
        calculatorName: String,
        onAccessGranted: () -> Unit
    ) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("Verificación Profesional Requerida")
            .setMessage("""
                Para acceder a "$calculatorName", necesitamos verificar tu estatus como profesional de salud.
                
                ¿Confirmas que eres un profesional de salud licenciado con conocimiento clínico para interpretar estos resultados?
                
                ⚠️ Estas calculadoras son herramientas de apoyo clínico que NO reemplazan el juicio médico profesional.
            """.trimIndent())
            .setPositiveButton("Soy Profesional de Salud") { _, _ ->
                // Save professional verification
                secureStorageManager.saveGuestPreference("professional_verified", "true")
                secureStorageManager.saveGuestPreference("last_verification", System.currentTimeMillis().toString())

                // Show enhanced disclaimer next
                showEnhancedDisclaimerDialog(fragment, calculatorName, onAccessGranted)
            }
            .setNegativeButton("No soy Profesional") { _, _ ->
                showEducationalAccessDialog(fragment, calculatorName, onAccessGranted)
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show enhanced medical disclaimer
     */
    private fun showEnhancedDisclaimerDialog(
        fragment: Fragment,
        calculatorName: String,
        onAccessGranted: () -> Unit
    ) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("Responsabilidad Clínica")
            .setMessage("""
                IMPORTANTE - RESPONSABILIDAD DEL PROFESIONAL:
                
                ✅ Entiendo que soy responsable de verificar todos los cálculos
                ✅ Los resultados deben interpretarse en contexto clínico
                ✅ Esta herramienta NO diagnostica ni prescribe tratamientos
                ✅ Siempre debo ejercer mi juicio clínico independiente
                ✅ Consultaré fuentes adicionales cuando sea necesario
                
                Al continuar, acepto total responsabilidad por el uso clínico de esta información.
            """.trimIndent())
            .setPositiveButton("Acepto la Responsabilidad") { _, _ ->
                // Save enhanced disclaimer acceptance
                secureStorageManager.saveGuestPreference("enhanced_disclaimer", "true")

                logCompliantAccess(calculatorName)
                onAccessGranted()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    /**
     * Show educational access dialog for non-professionals
     */
    private fun showEducationalAccessDialog(
        fragment: Fragment,
        calculatorName: String,
        onAccessGranted: () -> Unit
    ) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("Acceso Educativo")
            .setMessage("""
                Puedes acceder a "$calculatorName" únicamente con fines educativos.
                
                🎓 SOLO PARA APRENDIZAJE:
                • Los resultados NO deben usarse para decisiones médicas
                • NO reemplazan la consulta con profesionales de salud
                • Requiere supervisión de un profesional licenciado
                
                ⚠️ Para uso clínico real, consulta siempre con un profesional de salud calificado.
            """.trimIndent())
            .setPositiveButton("Continuar (Solo Educativo)") { _, _ ->
                logEducationalAccess(calculatorName)
                onAccessGranted()
            }
            .setNegativeButton("Buscar Profesional") { _, _ ->
                showFindProfessionalDialog(fragment)
            }
            .show()
    }

    /**
     * Show access denied dialog
     */
    private fun showAccessDeniedDialog(fragment: Fragment) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("Acceso Restringido")
            .setMessage("""
                El acceso a las calculadoras médicas está restringido para garantizar un uso seguro y responsable.
                
                Para acceder, necesitas:
                • Aceptar los términos de uso médico
                • Verificar tu estatus profesional o educativo
                
                ¿Te gustaría revisar los requisitos?
            """.trimIndent())
            .setPositiveButton("Revisar Requisitos") { _, _ ->
                showPrivacyAndDisclaimerDialog(fragment)
            }
            .setNegativeButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Show find professional dialog
     */
    private fun showFindProfessionalDialog(fragment: Fragment) {
        AlertDialog.Builder(fragment.requireContext())
            .setTitle("Buscar Profesional de Salud")
            .setMessage("""
                Para obtener resultados médicos válidos, consulta con:
                
                🏥 Médicos licenciados
                👩‍⚕️ Enfermeros registrados
                💊 Farmacéuticos certificados
                🩺 Especialistas médicos
                
                Ellos pueden usar estas herramientas de manera segura y efectiva para tu cuidado médico.
            """.trimIndent())
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    /**
     * Show privacy and disclaimer dialog
     */
    private fun showPrivacyAndDisclaimerDialog(fragment: Fragment) {
        val dialog = PrivacyAndDisclaimerDialogFragment.newInstance()
        dialog.show(fragment.parentFragmentManager, PrivacyAndDisclaimerDialogFragment.TAG)
    }

    /**
     * Log compliant access for Google Play documentation
     */
    private fun logCompliantAccess(calculatorName: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val userType = if (userManager.isGuestMode()) "GUEST" else "AUTHENTICATED"

        println("✅ COMPLIANT ACCESS LOG: $timestamp - $userType accessed $calculatorName with full compliance")

        // Increment access counter
        val currentAttempts = secureStorageManager.getGuestPreference("access_attempts", "0").toIntOrNull() ?: 0
        secureStorageManager.saveGuestPreference("access_attempts", (currentAttempts + 1).toString())
    }

    /**
     * Log educational access
     */
    private fun logEducationalAccess(calculatorName: String) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        println("📚 EDUCATIONAL ACCESS LOG: $timestamp - Educational access to $calculatorName")

        // Mark as educational access
        secureStorageManager.saveGuestPreference("last_educational_access", timestamp)
    }

    /**
     * Check if user has exceeded access limits (for rate limiting)
     */
    fun hasExceededAccessLimits(): Boolean {
        val accessAttempts = secureStorageManager.getGuestPreference("access_attempts", "0").toIntOrNull() ?: 0
        val lastVerification = secureStorageManager.getGuestPreference("last_verification", "0").toLongOrNull() ?: 0L

        // For guest users, limit to 50 accesses per session
        if (userManager.isGuestMode() && accessAttempts >= 50) {
            return true
        }

        // Require re-verification every 24 hours
        val verificationAge = System.currentTimeMillis() - lastVerification
        val maxVerificationAge = 24 * 60 * 60 * 1000L // 24 hours

        return verificationAge > maxVerificationAge
    }

    /**
     * Get compliance summary for display
     */
    fun getComplianceSummary(): String {
        val status = getCurrentComplianceStatus()
        return when (status.accessLevel) {
            AccessLevel.FULL_ACCESS -> "✅ Acceso Completo - Profesional Verificado"
            AccessLevel.REQUIRES_VERIFICATION -> "⚠️ Requiere Verificación Profesional"
            AccessLevel.REQUIRES_DISCLAIMER -> "📋 Requiere Aceptar Responsabilidad"
            AccessLevel.EDUCATIONAL_ONLY -> "🎓 Solo Acceso Educativo"
            AccessLevel.ACCESS_DENIED -> "❌ Acceso Denegado"
        }
    }
}