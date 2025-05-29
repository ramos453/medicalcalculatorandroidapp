// Replace your existing PrivacyAndDisclaimerDialogFragment.kt with this updated version

package com.example.medicalcalculatorapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.DialogPrivacyAndDisclaimerBinding
import com.example.medicalcalculatorapp.util.SecureStorageManager

class PrivacyAndDisclaimerDialogFragment : DialogFragment() {

    private var _binding: DialogPrivacyAndDisclaimerBinding? = null
    private val binding get() = _binding!!

    private lateinit var secureStorageManager: SecureStorageManager
    private var onAccepted: (() -> Unit)? = null

    fun setOnAcceptedListener(listener: () -> Unit) {
        onAccepted = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, android.R.style.Theme_Material_Dialog)
        isCancelable = false
        secureStorageManager = SecureStorageManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogPrivacyAndDisclaimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupTermsOfUse()
        setupButtons()
    }

    private fun setupTermsOfUse() {
        // Single comprehensive Terms of Use
        binding.tvPageIndicator.text = "Términos de Uso y Aviso Médico"
        binding.tvTitle.text = "📋 Términos de Uso"
        binding.tvContent.text = getString(R.string.terms_of_use_text)
        binding.cbAgree.text = "✅ He leído y acepto completamente los Términos de Uso y Aviso Médico"
        binding.cbAgree.isChecked = false
        binding.btnNext.text = "Aceptar y Continuar"
        binding.btnCancel.text = "Salir de la App"
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            // Exit app if they don't want to accept terms
            Toast.makeText(
                requireContext(),
                "⚠️ La aplicación requiere aceptación de los Términos de Uso para funcionar",
                Toast.LENGTH_LONG
            ).show()
            requireActivity().finish()
        }

        binding.btnNext.setOnClickListener {
            if (binding.cbAgree.isChecked) {
                // Terms accepted - save and continue
                secureStorageManager.saveDisclaimerAccepted(true)

                Toast.makeText(
                    requireContext(),
                    "✅ Términos aceptados. Bienvenido a MediCálculos",
                    Toast.LENGTH_SHORT
                ).show()

                onAccepted?.invoke()
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "⚠️ Debe aceptar los Términos de Uso y Aviso Médico para usar la aplicación",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            (resources.displayMetrics.heightPixels * 0.9).toInt()
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "TermsOfUseDialog"

        fun newInstance(): PrivacyAndDisclaimerDialogFragment {
            return PrivacyAndDisclaimerDialogFragment()
        }
    }
}