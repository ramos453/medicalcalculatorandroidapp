package com.example.medicalcalculatorapp.data.model

/**
 * Represents a medical calculator with its properties and calculation fields
 */
data class MedicalCalculator(
    val id: String,
    val name: String,
    val description: String,
    val category: String,
    val isFavorite: Boolean = false,
    val lastUsed: Long? = null,
    val inputFields: List<CalculatorField>,
    val resultFields: List<CalculatorField>
)

/**
 * Represents a field in a medical calculator (input or result)
 */
data class CalculatorField(
    val id: String,
    val name: String,
    val type: FieldType,
    val units: String? = null,
    val minValue: Double? = null,
    val maxValue: Double? = null,
    val defaultValue: String? = null,
    val options: List<String>? = null
)

/**
 * Defines the types of fields that can be used in calculators
 */
enum class FieldType {
    NUMBER,
    TEXT,
    DROPDOWN,
    CHECKBOX,
    RADIO
}