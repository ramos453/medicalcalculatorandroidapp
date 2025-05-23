package com.example.medicalcalculatorapp.domain.calculator

data class Reference(
    val title: String,
    val source: String,
    val url: String? = null,
    val year: Int? = null
)