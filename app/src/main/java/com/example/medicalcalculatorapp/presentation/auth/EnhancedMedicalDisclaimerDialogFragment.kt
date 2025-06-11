// Create this file: app/src/main/java/com/example/medicalcalculatorapp/presentation/auth/EnhancedMedicalDisclaimerDialogFragment.kt

package com.example.medicalcalculatorapp.presentation.auth

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.core.content.ContextCompat
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.DialogEnhancedMedicalDisclaimerBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Enhanced Medical Disclaimer Dialog for Google Play Compliance
 *
 * This dialog meets the new Google Play Health App Policy requirements by:
 * - Displaying comprehensive medical disclaimers
 * - Verifying professional licensing
 * - Showing emergency warnings prominently
 * - Requiring explicit acceptance
 */
class EnhancedMedicalDisclaimerDialogFragment : DialogFragment() {

    private var _binding: DialogEnhancedMedicalDisclaimerBinding? = null
    private val binding get() = _binding!!

    private var onAcceptedListener: (() -> Unit)? = null
    private var onRejectedListener: (() -> Unit)? = null

    // Track which sections have been read/acknowledged
    private var hasReadEmergencyWarning = false
    private var hasReadProfessionalRequirements = false
    private var hasReadLiabilityLimitation = false
    private var hasConfirmedProfessionalStatus = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make dialog non-dismissible - user must explicitly accept or reject
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(createCustomView())
            .setCancelable(false) // Force user to make a choice
            .create()
    }

    private fun createCustomView(): View {
        _binding = DialogEnhancedMedicalDisclaimerBinding.inflate(
            LayoutInflater.from(requireContext())
        )

        setupUI()
        setupInteractionTracking()
        setupButtons()

        return binding.root
    }

    private fun setupUI() {
        // Set emergency warning with high visibility
        binding.tvEmergencyWarning.apply {
            text = getString(R.string.enhanced_emergency_warning)
            setTextColor(ContextCompat.getColor(requireContext(), R.color.error))
            setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.warning))
        }

        // Set regulatory disclaimer
        binding.tvRegulatoryDisclaimer.text = getString(R.string.fda_anvisa_disclaimer)

        // Set professional requirements
        binding.tvProfessionalRequirements.text = getString(R.string.professional_license_requirement)

        // Set liability limitation
        binding.tvLiabilityLimitation.text = getString(R.string.enhanced_liability_limitation)

        // Set final acceptance text
        binding.tvFinalAcceptance.text = getString(R.string.final_acceptance_declaration)

        // Initially disable accept button
        binding.btnAcceptDisclaimer.isEnabled = false
    }

    private fun setupInteractionTracking() {
        // Track scrolling through emergency warning
        binding.scrollViewEmergency.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 0) {
                hasReadEmergencyWarning = true
                binding.iconEmergencyRead.visibility = View.VISIBLE
                updateAcceptButtonState()
            }
        }

        // Track scrolling through professional requirements
        binding.scrollViewProfessional.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 0) {
                hasReadProfessionalRequirements = true
                binding.iconProfessionalRead.visibility = View.VISIBLE
                updateAcceptButtonState()
            }
        }

        // Track scrolling through liability section
        binding.scrollViewLiability.setOnScrollChangeListener { _, _, scrollY, _, _ ->
            if (scrollY > 0) {
                hasReadLiabilityLimitation = true
                binding.iconLiabilityRead.visibility = View.VISIBLE
                updateAcceptButtonState()
            }
        }

        // Professional status confirmation checkbox
        binding.checkboxProfessionalStatus.setOnCheckedChangeListener { _, isChecked ->
            hasConfirmedProfessionalStatus = isChecked
            updateAcceptButtonState()
        }
    }

    private fun setupButtons() {
        binding.btnAcceptDisclaimer.setOnClickListener {
            if (allRequirementsMet()) {
                showFinalConfirmationDialog()
            }
        }

        binding.btnRejectDisclaimer.setOnClickListener {
            showRejectionDialog()
        }

        // View full terms button
        binding.btnViewFullTerms.setOnClickListener {
            showFullTermsDialog()
        }
    }

    private fun updateAcceptButtonState() {
        val allRead = hasReadEmergencyWarning &&
                hasReadProfessionalRequirements &&
                hasReadLiabilityLimitation &&
                hasConfirmedProfessionalStatus

        binding.btnAcceptDisclaimer.isEnabled = allRead

        if (allRead) {
            binding.btnAcceptDisclaimer.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.primary)
            )
            binding.tvAcceptanceRequirement.visibility = View.GONE
        } else {
            binding.tvAcceptanceRequirement.visibility = View.VISIBLE
            binding.tvAcceptanceRequirement.text = getRequirementText()
        }
    }

    private fun getRequirementText(): String {
        val missing = mutableListOf<String>()

        if (!hasReadEmergencyWarning) missing.add("emergencia")
        if (!hasReadProfessionalRequirements) missing.add("requisitos profesionales")
        if (!hasReadLiabilityLimitation) missing.add("limitación de responsabilidad")
        if (!hasConfirmedProfessionalStatus) missing.add("confirmar estado profesional")

        return "Debe leer y confirmar: ${missing.joinToString(", ")}"
    }

    private fun allRequirementsMet(): Boolean {
        return hasReadEmergencyWarning &&
                hasReadProfessionalRequirements &&
                hasReadLiabilityLimitation &&
                hasConfirmedProfessionalStatus
    }

    private fun showFinalConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("⚕️ Confirmación Final")
            .setMessage(
                "Al aceptar, confirma que:\n\n" +
                        "✓ Es un profesional de salud licenciado\n" +
                        "✓ Ha leído todos los avisos médicos\n" +
                        "✓ Entiende las limitaciones de la aplicación\n" +
                        "✓ Acepta toda la responsabilidad por decisiones médicas\n\n" +
                        "¿Proceder con el uso de la aplicación?"
            )
            .setPositiveButton("Confirmo y Acepto") { _, _ ->
                onAcceptedListener?.invoke()
                dismiss()
            }
            .setNegativeButton("Revisar Nuevamente") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showRejectionDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Acceso Denegado")
            .setMessage(
                "Sin aceptar estos términos médicos, no puede usar la aplicación.\n\n" +
                        "Esta aplicación está diseñada exclusivamente para profesionales de salud licenciados.\n\n" +
                        "¿Desea salir de la aplicación?"
            )
            .setPositiveButton("Salir de la App") { _, _ ->
                onRejectedListener?.invoke()
                requireActivity().finish()
            }
            .setNegativeButton("Revisar Términos") { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    private fun showFullTermsDialog() {
        val termsDialog = FullTermsDialogFragment.newInstance()
        termsDialog.show(parentFragmentManager, "full_terms")
    }

    fun setOnAcceptedListener(listener: () -> Unit) {
        onAcceptedListener = listener
    }

    fun setOnRejectedListener(listener: () -> Unit) {
        onRejectedListener = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "EnhancedMedicalDisclaimerDialog"

        fun newInstance(): EnhancedMedicalDisclaimerDialogFragment {
            return EnhancedMedicalDisclaimerDialogFragment()
        }
    }
}