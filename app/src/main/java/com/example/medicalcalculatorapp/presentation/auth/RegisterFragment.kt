package com.example.medicalcalculatorapp.presentation.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.FragmentRegisterBinding
import com.example.medicalcalculatorapp.util.ValidationUtils

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup click listeners
        binding.btnRegister.setOnClickListener {
            if (validateInputs()) {
                performRegistration()
            }
        }

        binding.tvLogin.setOnClickListener {
            findNavController().popBackStack()
        }

        binding.ivBack.setOnClickListener {
            findNavController().popBackStack()
        }
    }

//    private fun validateInputs(): Boolean {
//        var isValid = true
//
//        val name = binding.etName.text.toString().trim()
//        val email = binding.etEmail.text.toString().trim()
//        val password = binding.etPassword.text.toString().trim()
//        val confirmPassword = binding.etConfirmPassword.text.toString().trim()
//
//        // Name validation
//        if (name.isEmpty()) {
//            binding.tilName.error = getString(R.string.name_required)
//            isValid = false
//        } else {
//            binding.tilName.error = null
//        }
//
//        // Email validation
//        if (email.isEmpty()) {
//            binding.tilEmail.error = getString(R.string.email_required)
//            isValid = false
//        } else if (!isValidEmail(email)) {
//            binding.tilEmail.error = getString(R.string.invalid_email)
//            isValid = false
//        } else {
//            binding.tilEmail.error = null
//        }
//
//        // Password validation
//        if (password.isEmpty()) {
//            binding.tilPassword.error = getString(R.string.password_required)
//            isValid = false
//        } else if (password.length < 6) {
//            binding.tilPassword.error = getString(R.string.password_too_short)
//            isValid = false
//        } else {
//            binding.tilPassword.error = null
//        }
//
//        // Confirm password validation
//        if (password != confirmPassword) {
//            binding.tilConfirmPassword.error = getString(R.string.passwords_not_matching)
//            isValid = false
//        } else {
//            binding.tilConfirmPassword.error = null
//        }
//
//        return isValid
//    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val name = ValidationUtils.sanitizeInput(binding.etName.text.toString().trim())
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Update the name field with sanitized input
        if (binding.etName.text.toString() != name) {
            binding.etName.setText(name)
        }

        // Name validation
        if (name.isEmpty()) {
            binding.tilName.error = getString(R.string.name_required)
            isValid = false
        } else {
            binding.tilName.error = null
        }

        // Email validation
        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.email_required)
            isValid = false
        } else if (!ValidationUtils.isValidEmail(email)) {
            binding.tilEmail.error = getString(R.string.invalid_email)
            isValid = false
        } else if (ValidationUtils.containsSuspiciousPatterns(email)) {
            binding.tilEmail.error = getString(R.string.invalid_input)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        // Password validation
        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.password_required)
            isValid = false
        } else if (!ValidationUtils.isValidPassword(password)) {
            binding.tilPassword.error = getString(R.string.password_requirements)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        // Confirm password validation
        if (password != confirmPassword) {
            binding.tilConfirmPassword.error = getString(R.string.passwords_not_matching)
            isValid = false
        } else {
            binding.tilConfirmPassword.error = null
        }

        return isValid
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun performRegistration() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnRegister.isEnabled = false

        // Simulate network delay
        view?.postDelayed({
            binding.progressBar.visibility = View.GONE
            binding.btnRegister.isEnabled = true

            // For now, simulate successful registration
            Toast.makeText(requireContext(), R.string.register_success, Toast.LENGTH_SHORT).show()

            // Navigate back to login screen
            findNavController().popBackStack()
        }, 1500)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

