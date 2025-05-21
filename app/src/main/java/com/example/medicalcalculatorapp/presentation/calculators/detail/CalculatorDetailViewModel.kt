package com.example.medicalcalculatorapp.presentation.calculators.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.medicalcalculatorapp.domain.model.CalculationResult
import com.example.medicalcalculatorapp.domain.model.CalculatorField
import com.example.medicalcalculatorapp.domain.model.MedicalCalculator
import com.example.medicalcalculatorapp.domain.repository.ICalculatorRepository
import com.example.medicalcalculatorapp.domain.repository.IHistoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CalculatorDetailViewModel(
    private val calculatorId: String,
    private val calculatorRepository: ICalculatorRepository,
    private val historyRepository: IHistoryRepository
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

    // UI state
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _calculationPerformed = MutableStateFlow(false)
    val calculationPerformed: StateFlow<Boolean> = _calculationPerformed

    init {
        loadCalculator()
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
    }

    fun performCalculation() {
        _calculator.value?.let { calculator ->
            try {
                // For now, we'll just implement BMI calculation
                // In a real app, you'd have a calculation service with multiple formulas
                val results = when (calculator.id) {
                    "bmi_calc" -> calculateBMI(calculator.inputFields)
                    // Add more calculator types as needed
                    else -> emptyMap()
                }

                _resultValues.value = results
                _interpretation.value = getInterpretation(calculator.id, results)
                _calculationPerformed.value = true

                // Save calculation to history
                saveCalculationToHistory(calculator.id, _inputValues.value, results)

            } catch (e: Exception) {
                _error.value = "Calculation error: ${e.message}"
            }
        }
    }

    private fun calculateBMI(inputFields: List<CalculatorField>): Map<String, String> {
        // Get input values
        val heightStr = _inputValues.value["height"] ?: return emptyMap()
        val weightStr = _inputValues.value["weight"] ?: return emptyMap()

        // Validate inputs
        if (heightStr.isBlank() || weightStr.isBlank()) {
            throw IllegalArgumentException("Height and weight must be provided")
        }

        // Parse inputs
        val height = heightStr.toDoubleOrNull() ?:
        throw NumberFormatException("Invalid height value")
        val weight = weightStr.toDoubleOrNull() ?:
        throw NumberFormatException("Invalid weight value")

        // Check ranges
        if (height <= 0 || weight <= 0) {
            throw IllegalArgumentException("Height and weight must be positive values")
        }

        // Calculate BMI: weight (kg) / (height (m))²
        val heightInMeters = height / 100.0 // Convert cm to m
        val bmi = weight / (heightInMeters * heightInMeters)
        val formattedBmi = String.format("%.1f", bmi)

        // Determine BMI category
        val category = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal weight"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }

        return mapOf(
            "bmi" to formattedBmi,
            "category" to category
        )
    }

    private fun getInterpretation(calculatorId: String, results: Map<String, String>): String {
        return when (calculatorId) {
            "bmi_calc" -> {
                val bmi = results["bmi"] ?: return ""
                val category = results["category"] ?: return ""

                // Clinical interpretation for BMI
                when (category) {
                    "Underweight" -> "BMI $bmi - Underweight (BMI < 18.5)\nPossible nutritional deficiency and osteoporosis risks."
                    "Normal weight" -> "BMI $bmi - Normal weight (BMI 18.5-24.9)\nLowest risk of health problems related to weight."
                    "Overweight" -> "BMI $bmi - Overweight (BMI 25-29.9)\nIncreased risk of cardiovascular disease and diabetes."
                    "Obese" -> "BMI $bmi - Obese (BMI ≥ 30)\nHigher risk of heart disease, stroke, type 2 diabetes and certain cancers."
                    else -> ""
                }
            }
            // Add more calculator types as needed
            else -> ""
        }
    }

    private fun saveCalculationToHistory(
        calculatorId: String,
        inputs: Map<String, String>,
        results: Map<String, String>
    ) {
        viewModelScope.launch {
            try {
                val calculationResult = CalculationResult(
                    calculatorId = calculatorId,
                    inputValues = inputs,
                    resultValues = results
                )
                historyRepository.saveCalculationResult(calculationResult)
            } catch (e: Exception) {
                // Just log the error, don't show to user
                println("Failed to save calculation history: ${e.message}")
            }
        }
    }

    class Factory(
        private val calculatorId: String,
        private val calculatorRepository: ICalculatorRepository,
        private val historyRepository: IHistoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CalculatorDetailViewModel::class.java)) {
                return CalculatorDetailViewModel(
                    calculatorId,
                    calculatorRepository,
                    historyRepository
                ) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}