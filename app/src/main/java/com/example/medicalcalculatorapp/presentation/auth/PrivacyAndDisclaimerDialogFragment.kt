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

    private var currentStep = 1 // 1 = Privacy Policy, 2 = Medical Disclaimer
    private var privacyAccepted = false

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

        setupStep1() // Start with Privacy Policy
        setupButtons()
    }

    private fun setupStep1() {
        // Privacy Policy Step
        binding.tvPageIndicator.text = "1 de 2: Política de Privacidad"
        binding.tvTitle.text = "Política de Privacidad"
        binding.tvContent.text = getString(R.string.privacy_policy_text)
        binding.cbAgree.text = "He leído y acepto la Política de Privacidad"
        binding.cbAgree.isChecked = false
        binding.btnNext.text = "Continuar"
        currentStep = 1
    }

    private fun setupStep2() {
        // Medical Disclaimer Step
        binding.tvPageIndicator.text = "2 de 2: Aviso Médico Legal"
        binding.tvTitle.text = "Aviso Médico"
        binding.tvContent.text = getString(R.string.disclaimer_text)
        binding.cbAgree.text = getString(R.string.agree_disclaimer)
        binding.cbAgree.isChecked = false
        binding.btnNext.text = "Aceptar y Continuar"
        binding.btnCancel.text = "Atrás"
        currentStep = 2
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener {
            if (currentStep == 1) {
                // Exit app if they don't want to accept privacy policy
                requireActivity().finish()
            } else {
                // Go back to privacy policy
                setupStep1()
            }
        }

        binding.btnNext.setOnClickListener {
            if (currentStep == 1) {
                // Privacy Policy step
                if (binding.cbAgree.isChecked) {
                    privacyAccepted = true
                    setupStep2() // Move to medical disclaimer
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Debe aceptar la Política de Privacidad para continuar",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } else {
                // Medical Disclaimer step
                if (binding.cbAgree.isChecked) {
                    // Both steps completed - save and continue
                    secureStorageManager.saveDisclaimerAccepted(true)
                    onAccepted?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.must_agree),
                        Toast.LENGTH_LONG
                    ).show()
                }
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
        const val TAG = "PrivacyDisclaimerDialog"

        fun newInstance(): PrivacyAndDisclaimerDialogFragment {
            return PrivacyAndDisclaimerDialogFragment()
        }
    }
}