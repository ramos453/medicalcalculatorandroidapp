package com.example.medicalcalculatorapp.domain.service

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult

class CalculatorService {

    private val calculators = mutableMapOf<String, Calculator>()

    fun registerCalculator(calculator: Calculator) {
        calculators[calculator.calculatorId] = calculator
    }

    fun getCalculator(calculatorId: String): Calculator? {
        return calculators[calculatorId]
    }

    fun getAllCalculatorIds(): Set<String> {
        return calculators.keys
    }

    fun validateInputs(calculatorId: String, inputs: Map<String, String>): ValidationResult {
        val calculator = getCalculator(calculatorId)
            ?: return ValidationResult(false, listOf("Calculator not found: $calculatorId"))

        return calculator.validate(inputs)
    }

    fun performCalculation(calculatorId: String, inputs: Map<String, String>): CalculationResult {
        val calculator = getCalculator(calculatorId)
            ?: throw IllegalArgumentException("Calculator not found: $calculatorId")

        return calculator.calculate(inputs)
    }

    fun getInterpretation(calculatorId: String, result: CalculationResult): String {
        val calculator = getCalculator(calculatorId)
            ?: throw IllegalArgumentException("Calculator not found: $calculatorId")

        return calculator.getInterpretation(result)
    }

    fun getReferences(calculatorId: String): List<Reference> {
        val calculator = getCalculator(calculatorId)
            ?: throw IllegalArgumentException("Calculator not found: $calculatorId")

        return calculator.getReferences()
    }
}