package com.example.medicalcalculatorapp.data.model

/**
 * Represents a category for organizing medical calculators
 */
data class Category(
    val id: String,
    val name: String,
    val description: String? = null,
    val iconResId: Int? = null
)