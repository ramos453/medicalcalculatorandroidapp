package com.example.medicalcalculatorapp.ui.theme.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Setup click listeners
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        binding.tvRegister.setOnClickListener {
            //findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            Toast.makeText(requireContext(), "Would navigate to registration", Toast.LENGTH_LONG).show()
        }

        binding.tvForgotPassword.setOnClickListener {
            // Will implement later
            Toast.makeText(context, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = getString(R.string.email_required)
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.password_required)
            isValid = false
        } else {
            binding.tilPassword.error = null
        }

        return isValid
    }

//    private fun performLogin() {
//        binding.progressBar.visibility = View.VISIBLE
//
//        // Simulate network delay
//        view?.postDelayed({
//            binding.progressBar.visibility = View.GONE
//
//            // For now, simulate successful login
//            Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()
//
//            // Navigate to main screen (calculator list)
//            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
//        }, 1500)
//    }
    private fun performLogin() {
        binding.progressBar.visibility = View.VISIBLE

        // Simulate network delay
        view?.postDelayed({
            binding.progressBar.visibility = View.GONE

            // For now, simulate successful login
            Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()

            // Comment out navigation until fully implemented
            // findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)

            // Instead, just show a message
            Toast.makeText(requireContext(), "Would navigate to calculator list", Toast.LENGTH_LONG).show()
        }, 1500)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}