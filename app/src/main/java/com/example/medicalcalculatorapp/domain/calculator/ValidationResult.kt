package com.example.medicalcalculatorapp.domain.calculator

data class ValidationResult(
    val isValid: Boolean,
    val errors: List<String> = emptyList()
)