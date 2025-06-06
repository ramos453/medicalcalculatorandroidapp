package com.example.medicalcalculatorapp.presentation.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.FragmentSettingsBinding
import com.example.medicalcalculatorapp.presentation.settings.SettingsSectionAdapter


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private lateinit var settingsAdapter: SettingsSectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupProfileHeader()
        setupSettingsList()
        setupQuickActions()
        setupFAB()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupProfileHeader() {
        // TODO: Load actual user data from repository
        binding.tvUserName.text = "Dr. Usuario Ejemplo"
        binding.tvUserProfession.text = "Médico • Cardiología"

        // For now, just show placeholder
        // Later you'll load actual profile data here
    }

    private fun setupSettingsList() {
        val settingsSections = createSettingsSections()

        settingsAdapter = SettingsSectionAdapter { section ->
            handleSectionClick(section)
        }

        binding.recyclerViewSettings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = settingsAdapter
        }

        settingsAdapter.submitList(settingsSections)
    }

    private fun setupQuickActions() {
        binding.cardEditProfile.setOnClickListener {
            // 🔧 Navigate to profile edit
            try {
                findNavController().navigate(R.id.action_settingsFragment_to_profileEditFragment)
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error de navegación: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        binding.cardSignOut.setOnClickListener {
            // TODO: Show sign out confirmation dialog
            showSignOutDialog()
        }
    }

    private fun setupFAB() {
        binding.fabHelp.setOnClickListener {
            Toast.makeText(requireContext(), "Ayuda y Soporte - Próximamente", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createSettingsSections(): List<SettingsSection> {
        return listOf(
            SettingsSection(
                id = "app_preferences",
                title = "Configuración de la App",
                description = "Tema, idioma, notificaciones y preferencias",
                iconRes = R.drawable.ic_launcher_foreground, // Using available icon for now
                hasStatus = true,
                statusText = "Configurado"
            ),
            SettingsSection(
                id = "account_settings",
                title = "Configuración de Cuenta",
                description = "Email, contraseña y seguridad",
                iconRes = R.drawable.ic_launcher_foreground,
                hasStatus = false
            ),
            SettingsSection(
                id = "privacy_settings",
                title = "Privacidad y Datos",
                description = "Control de datos personales y privacidad",
                iconRes = R.drawable.ic_launcher_foreground,
                hasStatus = false
            ),
            SettingsSection(
                id = "calculator_settings",
                title = "Configuración de Calculadoras",
                description = "Preferencias de cálculo y historial",
                iconRes = R.drawable.ic_launcher_foreground,
                hasStatus = true,
                statusText = "Por defecto"
            ),
            SettingsSection(
                id = "help_support",
                title = "Ayuda y Soporte",
                description = "Guías, contacto y reportar problemas",
                iconRes = R.drawable.ic_launcher_foreground,
                hasStatus = false
            ),
            SettingsSection(
                id = "about",
                title = "Acerca de la App",
                description = "Versión, licencias y información legal",
                iconRes = R.drawable.ic_launcher_foreground,
                hasStatus = false
            )
        )
    }

    private fun handleSectionClick(section: SettingsSection) {
        when (section.id) {
            "app_preferences" -> {
                // 🔧 Navigate to app preferences
                try {
                    findNavController().navigate(R.id.action_settingsFragment_to_appPreferencesFragment)
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), "Error de navegación: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            "account_settings" -> {
                Toast.makeText(requireContext(), "${section.title} - Próximamente", Toast.LENGTH_SHORT).show()
            }
            "privacy_settings" -> {
                Toast.makeText(requireContext(), "${section.title} - Próximamente", Toast.LENGTH_SHORT).show()
            }
            "calculator_settings" -> {
                Toast.makeText(requireContext(), "${section.title} - Próximamente", Toast.LENGTH_SHORT).show()
            }
            "help_support" -> {
                Toast.makeText(requireContext(), "${section.title} - Próximamente", Toast.LENGTH_SHORT).show()
            }
            "about" -> {
                Toast.makeText(requireContext(), "${section.title} - Próximamente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showSignOutDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cerrar Sesión")
            .setMessage(getString(R.string.sign_out_confirm))
            .setPositiveButton("Cerrar Sesión") { _, _ ->
                // TODO: Implement actual sign out logic
                Toast.makeText(requireContext(), "Sesión cerrada", Toast.LENGTH_SHORT).show()
                // Navigate back to login
                findNavController().navigate(R.id.loginFragment)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

// Data class for settings sections
data class SettingsSection(
    val id: String,
    val title: String,
    val description: String,
    val iconRes: Int,
    val hasStatus: Boolean = false,
    val statusText: String = ""
)