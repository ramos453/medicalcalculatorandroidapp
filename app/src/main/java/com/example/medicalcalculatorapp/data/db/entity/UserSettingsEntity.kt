package com.example.medicalcalculatorapp.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
    @PrimaryKey
    val userId: String,
    val theme: String = "SYSTEM", // AppTheme enum as string
    val language: String = "es",
    val notificationsEnabled: Boolean = true,
    val biometricAuthEnabled: Boolean = false,
    val autoSaveCalculations: Boolean = true,
    val defaultUnits: String = "METRIC", // UnitSystem enum as string

    // Privacy settings stored as JSON string
    val privacySettingsJson: String = "{}",

    // Calculator preferences stored as JSON string
    val calculatorPreferencesJson: String = "{}",

    val lastSyncTime: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)