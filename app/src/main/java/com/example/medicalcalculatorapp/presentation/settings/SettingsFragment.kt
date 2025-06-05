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
            // TODO: Navigate to profile edit
            Toast.makeText(requireContext(), "Editar Perfil - Próximamente", Toast.LENGTH_SHORT).show()
        }

        binding.cardSignOut.setOnClickListener {
            // TODO: Show sign out confirmation dialog
            Toast.makeText(requireContext(), "Cerrar Sesión - Próximamente", Toast.LENGTH_SHORT).show()
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
                iconRes = R.drawable.ic_settings,
                hasStatus = true,
                statusText = "Configurado"
            ),
            SettingsSection(
                id = "account_settings",
                title = "Configuración de Cuenta",
                description = "Email, contraseña y seguridad",
                iconRes = R.drawable.ic_account,
                hasStatus = false
            ),
            SettingsSection(
                id = "privacy_settings",
                title = "Privacidad y Datos",
                description = "Control de datos personales y privacidad",
                iconRes = R.drawable.ic_privacy,
                hasStatus = false
            ),
            SettingsSection(
                id = "calculator_settings",
                title = "Configuración de Calculadoras",
                description = "Preferencias de cálculo y historial",
                iconRes = R.drawable.ic_calculator,
                hasStatus = true,
                statusText = "Por defecto"
            ),
            SettingsSection(
                id = "help_support",
                title = "Ayuda y Soporte",
                description = "Guías, contacto y reportar problemas",
                iconRes = R.drawable.ic_help,
                hasStatus = false
            ),
            SettingsSection(
                id = "about",
                title = "Acerca de la App",
                description = "Versión, licencias y información legal",
                iconRes = R.drawable.ic_info,
                hasStatus = false
            )
        )
    }

    private fun handleSectionClick(section: SettingsSection) {
        when (section.id) {
            "app_preferences" -> {
                // TODO: Navigate to app preferences
                Toast.makeText(requireContext(), "Navegando a ${section.title}", Toast.LENGTH_SHORT).show()
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