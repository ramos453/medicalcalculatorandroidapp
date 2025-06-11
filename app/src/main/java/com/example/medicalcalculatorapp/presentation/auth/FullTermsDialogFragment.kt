// Create this file: app/src/main/java/com/example/medicalcalculatorapp/presentation/auth/FullTermsDialogFragment.kt

package com.example.medicalcalculatorapp.presentation.auth

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.DialogFullTermsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Full Terms Dialog - Shows complete terms of use and privacy policy
 * This satisfies Google Play Store requirement for accessible full terms
 */
class FullTermsDialogFragment : DialogFragment() {

    private var _binding: DialogFullTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setView(createCustomView())
            .setPositiveButton("Cerrar") { _, _ -> dismiss() }
            .setNegativeButton("Compartir") { _, _ -> shareTerms() }
            .create()
    }

    private fun createCustomView(): View {
        _binding = DialogFullTermsBinding.inflate(
            LayoutInflater.from(requireContext())
        )

        setupContent()
        return binding.root
    }

    private fun setupContent() {
        // Set the complete terms text from strings.xml
        binding.tvFullTermsContent.text = getString(R.string.terms_of_use_text)

        // Add scroll listener to track if user has scrolled through terms
        binding.scrollViewTerms.setOnScrollChangeListener { _, _, scrollY, _, oldScrollY ->
            // Optional: Track that user has scrolled through terms
            if (scrollY > oldScrollY) {
                // User is scrolling down through the terms
            }
        }
    }

    private fun shareTerms() {
        // Allow users to share or export the terms (Google Play compliance)
        val shareIntent = android.content.Intent().apply {
            action = android.content.Intent.ACTION_SEND
            type = "text/plain"
            putExtra(android.content.Intent.EXTRA_TEXT,
                "Términos de Uso - MediCálculos\n\n${getString(R.string.terms_of_use_text)}")
            putExtra(android.content.Intent.EXTRA_SUBJECT, "Términos de Uso - MediCálculos")
        }

        startActivity(android.content.Intent.createChooser(shareIntent, "Compartir Términos"))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "FullTermsDialog"

        fun newInstance(): FullTermsDialogFragment {
            return FullTermsDialogFragment()
        }
    }
}