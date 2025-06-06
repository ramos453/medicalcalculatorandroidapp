package com.example.medicalcalculatorapp.presentation.settings

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.databinding.FragmentProfileEditBinding
import com.example.medicalcalculatorapp.di.AppDependencies
import com.example.medicalcalculatorapp.domain.model.MedicalProfession
import com.example.medicalcalculatorapp.util.ValidationUtils
import kotlinx.coroutines.launch

class ProfileEditFragment : Fragment() {

    private var _binding: FragmentProfileEditBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileEditViewModel by viewModels {
        ProfileEditViewModel.Factory(
            userRepository = AppDependencies.provideUserRepository(requireContext()),
            userManager = AppDependencies.provideUserManager(requireContext())
        )
    }

    private var selectedImageUri: Uri? = null
    private var hasUnsavedChanges = false

    // Photo picker launcher
    private val photoPickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            binding.ivProfilePicture.setImageURI(it)
            hasUnsavedChanges = true
            updateSaveButtonState()
        }
    }

    // Camera launcher
    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageUri = result.data?.data
            imageUri?.let {
                selectedImageUri = it
                binding.ivProfilePicture.setImageURI(it)
                hasUnsavedChanges = true
                updateSaveButtonState()
            }
        }
    }

    // Permission launcher
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openCamera()
        } else {
            Toast.makeText(
                requireContext(),
                "Permiso de cámara requerido para tomar fotos",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupDropdowns()
        setupFormValidation()
        setupClickListeners()
        observeViewModel()

        // Load current profile data
        viewModel.loadUserProfile()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            handleBackNavigation()
        }
    }

    private fun setupDropdowns() {
        // Language dropdown
        val languages = arrayOf("Español", "English", "Português")
        val languageAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, languages)
        binding.actvLanguage.setAdapter(languageAdapter)

        // Country dropdown
        val countries = arrayOf(
            "Argentina", "Bolivia", "Brasil", "Chile", "Colombia",
            "Costa Rica", "Ecuador", "El Salvador", "Guatemala",
            "Honduras", "México", "Nicaragua", "Panamá", "Paraguay",
            "Perú", "República Dominicana", "Uruguay", "Venezuela"
        )
        val countryAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, countries)
        binding.actvCountry.setAdapter(countryAdapter)

        // Profession dropdown
        val professions = MedicalProfession.values().map { it.displayName }.toTypedArray()
        val professionAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, professions)
        binding.actvProfession.setAdapter(professionAdapter)
    }

    private fun setupFormValidation() {
        // Add text watchers to detect changes
        val textWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                hasUnsavedChanges = true
                updateSaveButtonState()
                clearFieldErrors()
            }
        }

        binding.etFullName.addTextChangedListener(textWatcher)
        binding.etSpecialization.addTextChangedListener(textWatcher)
        binding.etInstitution.addTextChangedListener(textWatcher)
        binding.etLicenseNumber.addTextChangedListener(textWatcher)

        // Dropdown listeners
        binding.actvLanguage.setOnItemClickListener { _, _, _, _ ->
            hasUnsavedChanges = true
            updateSaveButtonState()
        }

        binding.actvCountry.setOnItemClickListener { _, _, _, _ ->
            hasUnsavedChanges = true
            updateSaveButtonState()
        }

        binding.actvProfession.setOnItemClickListener { _, _, _, _ ->
            hasUnsavedChanges = true
            updateSaveButtonState()
        }
    }

    private fun setupClickListeners() {
        binding.btnChangePhoto.setOnClickListener {
            showPhotoOptionsDialog()
        }

        binding.btnSaveChanges.setOnClickListener {
            if (validateForm()) {
                saveProfile()
            }
        }

        binding.btnDiscardChanges.setOnClickListener {
            showDiscardChangesDialog()
        }
    }

    private fun showPhotoOptionsDialog() {
        val options = arrayOf("Tomar foto", "Seleccionar de galería", "Cancelar")

        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Cambiar foto de perfil")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> checkCameraPermissionAndOpen()
                    1 -> openGallery()
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun checkCameraPermissionAndOpen() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun openCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireContext().packageManager) != null) {
            cameraLauncher.launch(intent)
        } else {
            Toast.makeText(requireContext(), "No hay aplicación de cámara disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        photoPickerLauncher.launch("image/*")
    }

    private fun validateForm(): Boolean {
        var isValid = true

        // Full name validation
        val fullName = ValidationUtils.sanitizeInput(binding.etFullName.text.toString().trim())
        if (fullName.isEmpty()) {
            binding.tilFullName.error = getString(R.string.field_required)
            isValid = false
        } else if (ValidationUtils.containsSuspiciousPatterns(fullName)) {
            binding.tilFullName.error = getString(R.string.invalid_input)
            isValid = false
        } else {
            binding.tilFullName.error = null
            // Update field with sanitized input
            if (binding.etFullName.text.toString() != fullName) {
                binding.etFullName.setText(fullName)
            }
        }

        // License number validation (if provided)
        val licenseNumber = binding.etLicenseNumber.text.toString().trim()
        if (licenseNumber.isNotEmpty()) {
            val sanitizedLicense = ValidationUtils.sanitizeInput(licenseNumber)
            if (ValidationUtils.containsSuspiciousPatterns(sanitizedLicense)) {
                binding.tilLicenseNumber.error = getString(R.string.invalid_license_number)
                isValid = false
            } else {
                binding.tilLicenseNumber.error = null
                if (binding.etLicenseNumber.text.toString() != sanitizedLicense) {
                    binding.etLicenseNumber.setText(sanitizedLicense)
                }
            }
        }

        // Profession validation
        if (binding.actvProfession.text.toString().isEmpty()) {
            binding.tilProfession.error = getString(R.string.field_required)
            isValid = false
        } else {
            binding.tilProfession.error = null
        }

        return isValid
    }

    private fun clearFieldErrors() {
        binding.tilFullName.error = null
        binding.tilSpecialization.error = null
        binding.tilInstitution.error = null
        binding.tilLicenseNumber.error = null
        binding.tilProfession.error = null
        binding.tilLanguage.error = null
        binding.tilCountry.error = null
    }

    private fun saveProfile() {
        showLoading(true)

        val updatedProfile = viewModel.currentProfile.value?.copy(
            fullName = binding.etFullName.text.toString().trim(),
            profession = binding.actvProfession.text.toString(),
            specialization = binding.etSpecialization.text.toString().trim().takeIf { it.isNotEmpty() },
            institution = binding.etInstitution.text.toString().trim().takeIf { it.isNotEmpty() },
            licenseNumber = binding.etLicenseNumber.text.toString().trim().takeIf { it.isNotEmpty() },
            country = binding.actvCountry.text.toString().takeIf { it.isNotEmpty() },
            language = when (binding.actvLanguage.text.toString()) {
                "English" -> "en"
                "Português" -> "pt"
                else -> "es"
            },
            profileImageUrl = selectedImageUri?.toString()
        )

        updatedProfile?.let {
            viewModel.saveProfile(it)
        }
    }

    private fun showDiscardChangesDialog() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Descartar cambios")
            .setMessage("¿Estás seguro de que quieres descartar todos los cambios?")
            .setPositiveButton("Descartar") { _, _ ->
                hasUnsavedChanges = false
                findNavController().popBackStack()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun handleBackNavigation() {
        if (hasUnsavedChanges) {
            showDiscardChangesDialog()
        } else {
            findNavController().popBackStack()
        }
    }

    private fun updateSaveButtonState() {
        binding.btnSaveChanges.isEnabled = hasUnsavedChanges
        binding.btnDiscardChanges.isEnabled = hasUnsavedChanges
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        binding.btnSaveChanges.isEnabled = !show && hasUnsavedChanges
        binding.btnDiscardChanges.isEnabled = !show && hasUnsavedChanges
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe profile data
                launch {
                    viewModel.currentProfile.collect { profile ->
                        profile?.let { populateForm(it) }
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collect { isLoading ->
                        showLoading(isLoading)
                    }
                }

                // Observe save success
                launch {
                    viewModel.saveSuccess.collect { success ->
                        if (success) {
                            hasUnsavedChanges = false
                            updateSaveButtonState()
                            Toast.makeText(
                                requireContext(),
                                getString(R.string.profile_updated_successfully),
                                Toast.LENGTH_SHORT
                            ).show()
                            findNavController().popBackStack()
                        }
                    }
                }

                // Observe errors
                launch {
                    viewModel.error.collect { errorMessage ->
                        errorMessage?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }
        }
    }

    private fun populateForm(profile: com.example.medicalcalculatorapp.domain.model.UserProfile) {
        binding.etFullName.setText(profile.fullName ?: "")
        binding.etEmail.setText(profile.email)
        binding.actvProfession.setText(profile.profession ?: "", false)
        binding.etSpecialization.setText(profile.specialization ?: "")
        binding.etInstitution.setText(profile.institution ?: "")
        binding.etLicenseNumber.setText(profile.licenseNumber ?: "")
        binding.actvCountry.setText(profile.country ?: "", false)

        // Set language
        val languageText = when (profile.language) {
            "en" -> "English"
            "pt" -> "Português"
            else -> "Español"
        }
        binding.actvLanguage.setText(languageText, false)

        // Load profile image if available
        profile.profileImageUrl?.let { imageUrl ->
            try {
                val uri = Uri.parse(imageUrl)
                binding.ivProfilePicture.setImageURI(uri)
            } catch (e: Exception) {
                // Handle invalid URI
            }
        }

        hasUnsavedChanges = false
        updateSaveButtonState()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_profile_edit, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> {
                if (validateForm()) {
                    saveProfile()
                }
                true
            }
            R.id.action_help -> {
                Toast.makeText(requireContext(), "Ayuda - Próximamente", Toast.LENGTH_SHORT).show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}