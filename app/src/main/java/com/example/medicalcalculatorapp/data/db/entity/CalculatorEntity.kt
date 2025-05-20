package com.example.medicalcalculatorapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.medicalcalculatorapp.data.db.converters.DateConverter

@Entity(tableName = "calculators")
data class CalculatorEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val categoryId: String,
    val lastUpdated: Long = System.currentTimeMillis()
)