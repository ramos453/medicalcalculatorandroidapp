package com.example.medicalcalculatorapp.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.medicalcalculatorapp.domain.model.FieldType

@Entity(
    tableName = "fields",
    foreignKeys = [
        ForeignKey(
            entity = CalculatorEntity::class,
            parentColumns = ["id"],
            childColumns = ["calculatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("calculatorId")] // For faster foreign key lookups
)
data class FieldEntity(
    @PrimaryKey(autoGenerate = true)
    val fieldId: Long = 0,
    val calculatorId: String,
    val id: String, // Field identifier within the calculator
    val name: String,
    val type: String, // Will use a type converter
    val isInputField: Boolean, // True for input, false for result field
    val units: String? = null,
    val minValue: Double? = null,
    val maxValue: Double? = null,
    val defaultValue: String? = null,
    val options: String? = null, // JSON string for options, will use type converter
    val displayOrder: Int = 0 // For controlling the order of fields
)