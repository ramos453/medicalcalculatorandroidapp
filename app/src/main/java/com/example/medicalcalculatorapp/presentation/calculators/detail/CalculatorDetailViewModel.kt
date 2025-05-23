package com.example.medicalcalculatorapp.presentation.calculators.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medicalcalculatorapp.domain.model.CalculationResult
import com.example.medicalcalculatorapp.domain.model.MedicalCalculator
import com.example.medicalcalculatorapp.domain.repository.ICalculatorRepository
import com.example.medicalcalculatorapp.domain.repository.IHistoryRepository
import com.example.medicalcalculatorapp.domain.service.CalculatorService
import com.example.medicalcalculatorapp.domain.calculator.Reference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CalculatorDetailViewModel(
    private val calculatorId: String,
    private val calculatorRepository: ICalculatorRepository,
    private val historyRepository: IHistoryRepository,
    private val calculatorService: CalculatorService // NEW: Inject the service
) : ViewModel() {

    // UI state for the calculator
    private val _calculator = MutableStateFlow<MedicalCalculator?>(null)
    val calculator: StateFlow<MedicalCalculator?> = _calculator

    // Input values entered by the user (field id -> value)
    private val _inputValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val inputValues: StateFlow<Map<String, String>> = _inputValues

    // Calculation results (field id -> value)
    private val _resultValues = MutableStateFlow<Map<String, String>>(emptyMap())
    val resultValues: StateFlow<Map<String, String>> = _resultValues

    // Clinical interpretation
    private val _interpretation = MutableStateFlow<String?>(null)
    val interpretation: StateFlow<String?> = _interpretation

    // Scientific references
    private val _references = MutableStateFlow<List<Reference>>(emptyList())
    val references: StateFlow<List<Reference>> = _references

    // UI state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _calculationPerformed = MutableStateFlow(false)
    val calculationPerformed: StateFlow<Boolean> = _calculationPerformed

    init {
        loadCalculator()
        loadReferences()
    }

    private fun loadCalculator() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                calculatorRepository.getCalculatorById(calculatorId).collectLatest { calculator ->
                    _calculator.value = calculator

                    // Initialize input values with defaults if available
                    val initialInputs = calculator?.inputFields?.associate { field ->
                        field.id to (field.defaultValue ?: "")
                    } ?: emptyMap()
                    _inputValues.value = initialInputs

                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "Failed to load calculator: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    private fun loadReferences() {
        viewModelScope.launch {
            try {
                val refs = calculatorService.getReferences(calculatorId)
                _references.value = refs
            } catch (e: Exception) {
                // References are optional, don't show error to user
                println("Could not load references for $calculatorId: ${e.message}")
            }
        }
    }

    fun updateInputValue(fieldId: String, value: String) {
        val currentInputs = _inputValues.value.toMutableMap()
        currentInputs[fieldId] = value
        _inputValues.value = currentInputs
    }

    fun resetInputs() {
        _calculator.value?.let { calculator ->
            val initialInputs = calculator.inputFields.associate { field ->
                field.id to (field.defaultValue ?: "")
            }
            _inputValues.value = initialInputs
        }
        _calculationPerformed.value = false
        _resultValues.value = emptyMap()
        _interpretation.value = null
        _error.value = null
    }

    fun performCalculation() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                // Use the Calculator Service instead of massive when statements!
                val result = calculatorService.performCalculation(calculatorId, _inputValues.value)
                val interpretation = calculatorService.getInterpretation(calculatorId, result)

                _resultValues.value = result.resultValues
                _interpretation.value = interpretation
                _calculationPerformed.value = true

                // Save calculation to history
                historyRepository.saveCalculationResult(result)

            } catch (e: Exception) {
                _error.value = "Error de cálculo: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun validateInputs(): Boolean {
        return try {
            val validation = calculatorService.validateInputs(calculatorId, _inputValues.value)
            if (!validation.isValid) {
                _error.value = validation.errors.joinToString("\n")
            }
            validation.isValid
        } catch (e: Exception) {
            _error.value = "Error de validación: ${e.message}"
            false
        }
    }

    // REMOVED: All the old calculateBMI, calculateMedicationDosage, getInterpretation methods
    // They are now handled by the CalculatorService!

    /**
     * Factory for creating CalculatorDetailViewModel with dependencies
     */
    class Factory(
        private val calculatorId: String,
        private val calculatorRepository: ICalculatorRepository,
        private val historyRepository: IHistoryRepository,
        private val calculatorService: CalculatorService // NEW: Add service dependency
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalculatorDetailViewModel::class.java)) {
                return CalculatorDetailViewModel(
                    calculatorId,
                    calculatorRepository,
                    historyRepository,
                    calculatorService // NEW: Inject the service
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}