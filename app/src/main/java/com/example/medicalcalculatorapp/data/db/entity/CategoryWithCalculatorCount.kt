package com.example.medicalcalculatorapp.data.db.entity

data class CategoryWithCalculatorCount(
    val id: String,
    val name: String,
    val description: String?,
    val iconResId: Int?,
    val displayOrder: Int,
    val calculator_count: Int
)