package com.example.medicalcalculatorapp.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "history",
    foreignKeys = [
        ForeignKey(
            entity = CalculatorEntity::class,
            parentColumns = ["id"],
            childColumns = ["calculatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("calculatorId"), Index("timestamp")]
)
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val historyId: Long = 0,
    val calculatorId: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val inputValues: String, // JSON string of inputs
    val resultValues: String // JSON string of outputs
)