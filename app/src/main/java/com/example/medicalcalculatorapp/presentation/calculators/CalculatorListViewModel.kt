package com.example.medicalcalculatorapp.presentation.calculators

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.model.MedicalCalculator
import com.example.medicalcalculatorapp.domain.repository.ICalculatorRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorListViewModel(
    private val calculatorRepository: ICalculatorRepository,
    private val userManager: UserManager
) : ViewModel() {

    // UI state
    private val _calculators = MutableStateFlow<List<MedicalCalculator>>(emptyList())
    val calculators: StateFlow<List<MedicalCalculator>> = _calculators

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Current filter mode
    private val _currentFilterMode = MutableStateFlow(FilterMode.ALL)
    val currentFilterMode: StateFlow<FilterMode> = _currentFilterMode

    // Current search query
    private val _searchQuery = MutableStateFlow<String?>(null)
    val searchQuery: StateFlow<String?> = _searchQuery

    init {
        loadCalculators()
    }

    fun setFilterMode(mode: FilterMode) {
        if (_currentFilterMode.value != mode) {
            _currentFilterMode.value = mode
            loadCalculators()
        }
    }

    fun searchCalculators(query: String?) {
        _searchQuery.value = query
        loadCalculators()
    }

    fun toggleFavorite(calculatorId: String) {
        viewModelScope.launch {
            try {
                val userId = userManager.getCurrentUserId()
                calculatorRepository.toggleFavorite(calculatorId, userId)
                // The list will update automatically due to Flow collection
            } catch (e: Exception) {
                _error.value = "Failed to update favorite status: ${e.message}"
            }
        }
    }

    private fun loadCalculators() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                // Handle search query first
                val query = _searchQuery.value
                if (!query.isNullOrBlank()) {
                    calculatorRepository.searchCalculators(query).collectLatest {
                        _calculators.value = it
                        _isLoading.value = false
                    }
                    return@launch
                }

                // Then handle filter mode
                when (_currentFilterMode.value) {
                    FilterMode.ALL -> {
                        calculatorRepository.getAllCalculators().collectLatest {
                            _calculators.value = it
                            _isLoading.value = false
                        }
                    }
                    FilterMode.FAVORITES -> {
                        val userId = userManager.getCurrentUserId()
                        calculatorRepository.getFavoriteCalculators(userId).collectLatest {
                            _calculators.value = it
                            _isLoading.value = false
                        }
                    }
                }
            } catch (e: Exception) {
                _error.value = "Failed to load calculators: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    enum class FilterMode {
        ALL, FAVORITES
    }

    /**
     * Factory for creating CalculatorListViewModel with dependencies
     */
    class Factory(
        private val calculatorRepository: ICalculatorRepository,
        private val userManager: UserManager
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalculatorListViewModel::class.java)) {
                return CalculatorListViewModel(calculatorRepository, userManager) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}