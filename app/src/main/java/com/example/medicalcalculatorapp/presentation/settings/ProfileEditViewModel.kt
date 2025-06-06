package com.example.medicalcalculatorapp.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.model.UserProfile
import com.example.medicalcalculatorapp.domain.repository.IUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileEditViewModel(
    private val userRepository: IUserRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _currentProfile = MutableStateFlow<UserProfile?>(null)
    val currentProfile: StateFlow<UserProfile?> = _currentProfile

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadUserProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                val userId = userManager.getCurrentUserId()
                userRepository.getUserProfile(userId).collectLatest { profile ->
                    if (profile == null) {
                        // Create a default profile if none exists
                        val defaultProfile = UserProfile(
                            id = userId,
                            email = "usuario@ejemplo.com" // This should come from secure storage
                        )
                        _currentProfile.value = defaultProfile
                    } else {
                        _currentProfile.value = profile
                    }
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Error al cargar el perfil: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    fun saveProfile(updatedProfile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _saveSuccess.value = false

            try {
                val success = if (_currentProfile.value == null) {
                    // Create new profile
                    userRepository.createUserProfile(updatedProfile)
                } else {
                    // Update existing profile
                    userRepository.updateUserProfile(updatedProfile)
                }

                if (success) {
                    _currentProfile.value = updatedProfile
                    _saveSuccess.value = true
                } else {
                    _error.value = "Error al guardar el perfil. Inténtalo nuevamente."
                }
            } catch (e: Exception) {
                _error.value = "Error al guardar el perfil: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun clearSaveSuccess() {
        _saveSuccess.value = false
    }

    /**
     * Factory for creating ProfileEditViewModel with dependencies
     */
    class Factory(
        private val userRepository: IUserRepository,
        private val userManager: UserManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProfileEditViewModel::class.java)) {
                return ProfileEditViewModel(userRepository, userManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}