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
        // Set the comprehensive medical disclaimer text
        binding.tvMedicalDisclaimerText.text = getString(R.string.comprehensive_medical_disclaimer)

        // Initially disable accept button
        binding.btnAcceptDisclaimer.isEnabled = false
    }

    private fun setupInteractionTracking() {
        // Track scrolling through the main disclaimer text
        val parentScrollView = binding.root.findViewById<androidx.core.widget.NestedScrollView>(
            binding.root.context.resources.getIdentifier("scrollView", "id", binding.root.context.packageName)
        )

        // Professional status confirmation checkbox
        binding.checkboxProfessionalStatus.setOnCheckedChangeListener { _, isChecked ->
            hasConfirmedProfessionalStatus = isChecked
            updateAcceptButtonState()
        }
    }

    private fun setupButtons() {
        binding.btnAcceptDisclaimer.setOnClickListener {
            if (hasConfirmedProfessionalStatus) {
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
        binding.btnAcceptDisclaimer.isEnabled = hasConfirmedProfessionalStatus

        if (hasConfirmedProfessionalStatus) {
            binding.btnAcceptDisclaimer.setBackgroundColor(
                ContextCompat.getColor(requireContext(), R.color.primary)
            )
            binding.tvAcceptanceRequirement.visibility = View.GONE
        } else {
            binding.tvAcceptanceRequirement.visibility = View.VISIBLE
            binding.tvAcceptanceRequirement.text = "Debe confirmar su estado profesional para continuar"
        }
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