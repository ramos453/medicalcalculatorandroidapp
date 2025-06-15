package com.example.medicalcalculatorapp.presentation.auth

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

/**
 * Professional Verification Dialog - Google Play Compliance
 *
 * Handles professional licensing verification according to
 * Google Play Health App Policy requirements
 */
class ProfessionalVerificationDialogFragment : DialogFragment() {

    private var onVerifiedListener: ((String, String?) -> Unit)? = null
    private var onSkippedListener: (() -> Unit)? = null

    // Store references to our views
    private lateinit var professionalSpinner: Spinner
    private lateinit var licenseInput: EditText

    companion object {
        const val TAG = "ProfessionalVerificationDialog"

        fun newInstance(): ProfessionalVerificationDialogFragment {
            return ProfessionalVerificationDialogFragment()
        }
    }

    fun setOnVerifiedListener(listener: (professionalType: String, licenseInfo: String?) -> Unit) {
        this.onVerifiedListener = listener
    }

    fun setOnSkippedListener(listener: () -> Unit) {
        this.onSkippedListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = false // Force user to make a choice
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = createCustomView()

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle("🏥 Verificación Profesional Médica")
            .setView(view)
            .setPositiveButton("Verificar como Profesional") { _, _ ->
                handleProfessionalVerification()
            }
            .setNegativeButton("Continuar como Usuario General") { _, _ ->
                handleGeneralUser()
            }
            .create()
    }

    private fun createCustomView(): View {
        // Create main container
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 32)
        }

        // Professional type selection
        val professionalLabel = TextView(requireContext()).apply {
            text = "Tipo de Profesional Médico:"
            textSize = 16f
            setPadding(0, 0, 0, 16)
        }

        // Create spinner with professional types
        professionalSpinner = Spinner(requireContext()).apply {
            val professionalTypes = arrayOf(
                "Médico",
                "Enfermero/a",
                "Farmacéutico/a",
                "Estudiante de Medicina",
                "Estudiante de Enfermería",
                "Investigador Médico",
                "Otro Profesional de Salud"
            )

            adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                professionalTypes
            ).also { adapter ->
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
        }

        // License info input
        val licenseLabel = TextView(requireContext()).apply {
            text = "Número de Licencia (Opcional):"
            textSize = 16f
            setPadding(0, 32, 0, 16)
        }

        licenseInput = EditText(requireContext()).apply {
            hint = "Ej: 12345-MD-2024"
            setPadding(16, 16, 16, 16)
            setBackgroundResource(android.R.drawable.edit_text)
        }

        // Educational notice
        val notice = TextView(requireContext()).apply {
            text = "⚠️ Usuarios generales tendrán acceso limitado solo para fines educativos"
            textSize = 14f
            setPadding(0, 32, 0, 0)
            setTextColor(resources.getColor(android.R.color.holo_orange_dark, null))
        }

        // Add all views to layout
        layout.addView(professionalLabel)
        layout.addView(professionalSpinner)
        layout.addView(licenseLabel)
        layout.addView(licenseInput)
        layout.addView(notice)

        return layout
    }

    private fun handleProfessionalVerification() {
        val professionalType = professionalSpinner.selectedItem.toString()
        val licenseInfo = licenseInput.text.toString().trim().takeIf { it.isNotEmpty() }

        lifecycleScope.launch {
            onVerifiedListener?.invoke(professionalType, licenseInfo)
        }
    }

    private fun handleGeneralUser() {
        lifecycleScope.launch {
            onSkippedListener?.invoke()
        }
    }
}