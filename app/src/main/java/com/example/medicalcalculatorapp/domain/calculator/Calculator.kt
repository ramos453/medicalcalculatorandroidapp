package com.example.medicalcalculatorapp.domain.calculator

import com.example.medicalcalculatorapp.domain.model.CalculationResult

interface Calculator {
    val calculatorId: String
    fun calculate(inputs: Map<String, String>): CalculationResult
    fun validate(inputs: Map<String, String>): ValidationResult
    fun getInterpretation(result: CalculationResult): String
    fun getReferences(): List<Reference>
}