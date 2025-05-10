package com.example.medicalcalculatorapp.domain.model

/**
 * Represents the result of a medical calculation
 */
data class CalculationResult(
    val calculatorId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val inputValues: Map<String, String>,
    val resultValues: Map<String, String>
)