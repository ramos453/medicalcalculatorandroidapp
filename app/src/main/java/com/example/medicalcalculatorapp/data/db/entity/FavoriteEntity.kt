package com.example.medicalcalculatorapp.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "favorites",
    primaryKeys = ["calculatorId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = CalculatorEntity::class,
            parentColumns = ["id"],
            childColumns = ["calculatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("calculatorId")]
)
data class FavoriteEntity(
    val calculatorId: String,
    val userId: String,
    val dateAdded: Long = System.currentTimeMillis()
)

