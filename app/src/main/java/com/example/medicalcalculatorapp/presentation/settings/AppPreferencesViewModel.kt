package com.example.medicalcalculatorapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.model.*
import com.example.medicalcalculatorapp.domain.repository.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppPreferencesViewModel(
    private val userRepository: IUserRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _currentSettings = MutableStateFlow<UserSettings?>(null)
    val currentSettings: StateFlow<UserSettings?> = _currentSettings

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val _resetSuccess = MutableStateFlow(false)
    val resetSuccess: StateFlow<Boolean> = _resetSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUserSettings() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = userManager.getCurrentUserId()
                userRepository.getUserSettings(userId).collectLatest { settings ->
                    if (settings == null) {
                        // Create default settings if none exist
                        val defaultSettings = UserSettings(userId = userId)
                        userRepository.updateUserSettings(defaultSettings)
                        _currentSettings.value = defaultSettings
                    } else {
                        _currentSettings.value = settings
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar configuraciones: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun updateTheme(theme: AppTheme) {
        updateSettings { settings ->
            settings.copy(theme = theme)
        }
    }

    fun updateLanguage(language: String) {
        updateSettings { settings ->
            settings.copy(language = language)
        }
    }

    fun updateNotificationSettings(enabled: Boolean) {
        updateSettings { settings ->
            settings.copy(notificationsEnabled = enabled)
        }
    }

    fun updateAutoSaveSettings(enabled: Boolean) {
        updateSettings { settings ->
            settings.copy(autoSaveCalculations = enabled)
        }
    }

    fun updateShowReferencesSettings(enabled: Boolean) {
        updateSettings { settings ->
            val updatedPreferences = settings.calculatorPreferences.copy(
                showReferences = enabled
            )
            settings.copy(calculatorPreferences = updatedPreferences)
        }
    }

    fun updateAnonymousStatsSettings(enabled: Boolean) {
        updateSettings { settings ->
            val updatedPrivacySettings = settings.privacySettings.copy(
                shareAnonymousStatistics = enabled
            )
            settings.copy(privacySettings = updatedPrivacySettings)
        }
    }

    private fun updateSettings(updateFunction: (UserSettings) -> UserSettings) {
        viewModelScope.launch {
            _error.value = null

            try {
                val currentSettings = _currentSettings.value
                if (currentSettings != null) {
                    val updatedSettings = updateFunction(currentSettings)
                    val success = userRepository.updateUserSettings(updatedSettings)

                    if (success) {
                        _currentSettings.value = updatedSettings
                        _updateSuccess.value = true
                    } else {
                        _error.value = "Error al actualizar configuraciones"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Error al actualizar configuraciones: ${e.message}"
            }
        }
    }

    fun resetSettingsToDefault() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = userManager.getCurrentUserId()
                val success = userRepository.resetSettingsToDefault(userId)

                if (success) {
                    val defaultSettings = UserSettings(userId = userId)
                    _currentSettings.value = defaultSettings
                    _resetSuccess.value = true
                } else {
                    _error.value = "Error al restablecer configuraciones"
                }
            } catch (e: Exception) {
                _error.value = "Error al restablecer configuraciones: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearUpdateSuccess() {
        _updateSuccess.value = false
    }

    fun clearResetSuccess() {
        _resetSuccess.value = false
    }

    /**
     * Factory for creating AppPreferencesViewModel with dependencies
     */
    class Factory(
        private val userRepository: IUserRepository,
        private val userManager: UserManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AppPreferencesViewModel::class.java)) {
                return AppPreferencesViewModel(userRepository, userManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}