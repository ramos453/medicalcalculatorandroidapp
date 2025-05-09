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
import com.example.medicalcalculatorapp.util.SecureStorageManager
import android.content.Context

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private lateinit var secureStorageManager: SecureStorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        secureStorageManager = SecureStorageManager(requireContext())
    }

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
        // Load any saved credentials
        loadSavedCredentials()
        // Setup click listeners
        binding.btnLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }
        binding.tvPrivacyPolicy.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_privacyPolicyFragment)
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
            //Toast.makeText(requireContext(), "Would navigate to registration", Toast.LENGTH_LONG).show()
        }

        binding.tvForgotPassword.setOnClickListener {
            // Will implement later
            Toast.makeText(context, "Forgot password clicked", Toast.LENGTH_SHORT).show()
        }
    }

//    private fun validateInputs(): Boolean {
//        var isValid = true
//
//        val email = binding.etEmail.text.toString().trim()
//        val password = binding.etPassword.text.toString().trim()
//
//        if (email.isEmpty()) {
//            binding.tilEmail.error = getString(R.string.email_required)
//            isValid = false
//        } else {
//            binding.tilEmail.error = null
//        }
//
//        if (password.isEmpty()) {
//            binding.tilPassword.error = getString(R.string.password_required)
//            isValid = false
//        } else {
//            binding.tilPassword.error = null
//        }
//
//        return isValid
//    }

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
private fun validateInputs(): Boolean {
    var isValid = true

    val email = binding.etEmail.text.toString().trim()
    val password = binding.etPassword.text.toString().trim()

    // Email validation
    if (email.isEmpty()) {
        binding.tilEmail.error = getString(R.string.email_required)
        isValid = false
    } else if (!isValidEmail(email)) {
        binding.tilEmail.error = getString(R.string.invalid_email)
        isValid = false
    } else {
        binding.tilEmail.error = null
    }

    // Password validation
    if (password.isEmpty()) {
        binding.tilPassword.error = getString(R.string.password_required)
        isValid = false
    } else if (password.length < 6) {
        binding.tilPassword.error = getString(R.string.password_too_short)
        isValid = false
    } else {
        binding.tilPassword.error = null
    }

    return isValid
}

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun loadSavedCredentials() {
        val rememberCredentials = secureStorageManager.getRememberMeFlag()
        if (rememberCredentials) {
            val savedEmail = secureStorageManager.getEmail()
            savedEmail?.let {
                binding.etEmail.setText(it)
                binding.cbRememberMe.isChecked = true
            }
        }
    }

    private fun saveCredentialsIfNeeded() {
        val email = binding.etEmail.text.toString().trim()
        val isChecked = binding.cbRememberMe.isChecked

        secureStorageManager.saveRememberMeFlag(isChecked)
        if (isChecked) {
            secureStorageManager.saveEmail(email)
        } else {
            secureStorageManager.clearCredentials()
        }
    }
//    private fun saveCredentialsIfNeeded() {
//        val email = binding.etEmail.text.toString().trim()
//        val isChecked = binding.cbRememberMe.isChecked
//
//        val sharedPrefs = requireActivity().getSharedPreferences(
//            "auth_prefs",
//            Context.MODE_PRIVATE
//        )
//
//        with(sharedPrefs.edit()) {
//            putBoolean("remember_credentials", isChecked)
//            if (isChecked) {
//                putString("saved_email", email)
//            } else {
//                remove("saved_email")
//            }
//            apply()
//        }
//    }
//
//    private fun loadSavedCredentials() {
//        val sharedPrefs = requireActivity().getSharedPreferences(
//            "auth_prefs",
//            Context.MODE_PRIVATE
//        )
//
//        val rememberCredentials = sharedPrefs.getBoolean("remember_credentials", false)
//        if (rememberCredentials) {
//            val savedEmail = sharedPrefs.getString("saved_email", "")
//            binding.etEmail.setText(savedEmail)
//            binding.cbRememberMe.isChecked = true
//        }
//    }


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
//            // Comment out navigation until fully implemented
//            // findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
//
//            // Instead, just show a message
//            Toast.makeText(requireContext(), "Would navigate to calculator list", Toast.LENGTH_LONG).show()
//        }, 1500)
//    }
    private fun performLogin() {
        binding.progressBar.visibility = View.VISIBLE
        binding.btnLogin.isEnabled = false

        // Save credentials if "Remember me" is checked
        saveCredentialsIfNeeded()

        // Simulate network delay
        view?.postDelayed({
            binding.progressBar.visibility = View.GONE
            binding.btnLogin.isEnabled = true

            // For now, simulate successful login
            Toast.makeText(requireContext(), R.string.login_success, Toast.LENGTH_SHORT).show()

            // Navigate to main screen (calculator list)
            findNavController().navigate(R.id.action_loginFragment_to_calculatorListFragment)
        }, 1500)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}