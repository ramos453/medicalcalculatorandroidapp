package com.example.medicalcalculatorapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.DialogMedicalDisclaimerBinding
import com.example.medicalcalculatorapp.util.SecureStorageManager

class DisclaimerDialogFragment : DialogFragment() {

    private var _binding: DialogMedicalDisclaimerBinding? = null
    private val binding get() = _binding!!

    private lateinit var secureStorageManager: SecureStorageManager

    private var onDisclaimerAccepted: (() -> Unit)? = null

    fun setOnDisclaimerAcceptedListener(listener: () -> Unit) {
        onDisclaimerAccepted = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setStyle(STYLE_NORMAL, R.style.Theme_MaterialComponents_Dialog_MinWidth)
        setStyle(STYLE_NORMAL, androidx.appcompat.R.style.Theme_AppCompat_Dialog_MinWidth)
        secureStorageManager = SecureStorageManager(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMedicalDisclaimerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnContinue.setOnClickListener {
            if (binding.cbAgree.isChecked) {
                // Save that user has accepted disclaimer
                secureStorageManager.saveDisclaimerAccepted(true)
                // Notify listener
                onDisclaimerAccepted?.invoke()
                dismiss()
            } else {
                binding.cbAgree.error = getString(R.string.must_agree)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "DisclaimerDialog"
    }
}