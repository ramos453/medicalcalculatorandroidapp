package com.example.medicalcalculatorapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String? = null,
    val iconResId: Int? = null,
    val displayOrder: Int = 0
)