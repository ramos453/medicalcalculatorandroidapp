package com.example.medicalcalculatorapp.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.FragmentAppPreferencesBinding
import com.example.medicalcalculatorapp.di.AppDependencies
import com.example.medicalcalculatorapp.domain.model.AppTheme
import kotlinx.coroutines.launch

class AppPreferencesFragment : Fragment() {

    private var _binding: FragmentAppPreferencesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AppPreferencesViewModel by viewModels {
        AppPreferencesViewModel.Factory(
            userRepository = AppDependencies.provideUserRepository(requireContext()),
            userManager = AppDependencies.provideUserManager(requireContext())
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAppPreferencesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupClickListeners()
        observeViewModel()

        // Load current settings
        viewModel.loadUserSettings()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupClickListeners() {
        // Theme selection
        binding.root.findViewById<View>(R.id.layoutLanguage)?.setOnClickListener {
            showThemeSelectionDialog()
        }

        // Language selection
        binding.layoutLanguage.setOnClickListener {
            showLanguageSelectionDialog()
        }

        // Notification toggle
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateNotificationSettings(isChecked)
        }

        // Auto-save toggle
        binding.switchAutoSave.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAutoSaveSettings(isChecked)
        }

        // Show references toggle
        binding.switchShowReferences.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateShowReferencesSettings(isChecked)
        }

        // Anonymous statistics toggle
        binding.switchAnonymousStats.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAnonymousStatsSettings(isChecked)
        }

        // Reset settings button
        binding.btnResetSettings.setOnClickListener {
            showResetSettingsDialog()
        }
    }

    private fun showThemeSelectionDialog() {
        val themes = arrayOf("Claro", "Oscuro", "Seguir sistema")
        val currentTheme = when (viewModel.currentSettings.value?.theme) {
            AppTheme.LIGHT -> 0
            AppTheme.DARK -> 1
            AppTheme.SYSTEM -> 2
            else -> 2
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar tema")
            .setSingleChoiceItems(themes, currentTheme) { dialog, which ->
                val selectedTheme = when (which) {
                    0 -> AppTheme.LIGHT
                    1 -> AppTheme.DARK
                    else -> AppTheme.SYSTEM
                }
                viewModel.updateTheme(selectedTheme)
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showLanguageSelectionDialog() {
        val languages = arrayOf("Español", "English", "Português")
        val languageCodes = arrayOf("es", "en", "pt")

        val currentLanguage = when (viewModel.currentSettings.value?.language) {
            "en" -> 1
            "pt" -> 2
            else -> 0
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Seleccionar idioma")
            .setSingleChoiceItems(languages, currentLanguage) { dialog, which ->
                viewModel.updateLanguage(languageCodes[which])
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun showResetSettingsDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Restablecer configuración")
            .setMessage("¿Estás seguro de que quieres restablecer todas las configuraciones a los valores predeterminados?")
            .setPositiveButton("Restablecer") { _, _ ->
                viewModel.resetSettingsToDefault()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe settings changes
                launch {
                    viewModel.currentSettings.collect { settings ->
                        settings?.let { updateUI(it) }
                    }
                }

                // Observe success messages
                launch {
                    viewModel.updateSuccess.collect { success ->
                        if (success) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.settings_updated_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.clearUpdateSuccess()
                        }
                    }
                }

                // Observe reset success
                launch {
                    viewModel.resetSuccess.collect { success ->
                        if (success) {
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.settings_reset_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            viewModel.clearResetSuccess()
                        }
                    }
                }

                // Observe errors
                launch {
                    viewModel.error.collect { errorMessage ->
                        errorMessage?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                            viewModel.clearError()
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(settings: com.example.medicalcalculatorapp.domain.model.UserSettings) {
        // Update theme description
        binding.tvThemeDescription.text = when (settings.theme) {
            AppTheme.LIGHT -> getString(R.string.theme_light)
            AppTheme.DARK -> getString(R.string.theme_dark)
            AppTheme.SYSTEM -> getString(R.string.theme_system)
        }

        // Update language description
        binding.tvLanguageDescription.text = when (settings.language) {
            "en" -> getString(R.string.english)
            "pt" -> getString(R.string.portuguese)
            else -> getString(R.string.spanish)
        }

        // Update switches without triggering listeners
        binding.switchNotifications.setOnCheckedChangeListener(null)
        binding.switchNotifications.isChecked = settings.notificationsEnabled
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateNotificationSettings(isChecked)
        }

        binding.switchAutoSave.setOnCheckedChangeListener(null)
        binding.switchAutoSave.isChecked = settings.autoSaveCalculations
        binding.switchAutoSave.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAutoSaveSettings(isChecked)
        }

        binding.switchShowReferences.setOnCheckedChangeListener(null)
        binding.switchShowReferences.isChecked = settings.calculatorPreferences.showReferences
        binding.switchShowReferences.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateShowReferencesSettings(isChecked)
        }

        binding.switchAnonymousStats.setOnCheckedChangeListener(null)
        binding.switchAnonymousStats.isChecked = settings.privacySettings.shareAnonymousStatistics
        binding.switchAnonymousStats.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateAnonymousStatsSettings(isChecked)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}