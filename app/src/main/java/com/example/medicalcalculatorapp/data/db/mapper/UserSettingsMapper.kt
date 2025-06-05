package com.example.medicalcalculatorapp.data.db.mapper

import com.example.medicalcalculatorapp.data.db.entity.UserSettingsEntity
import com.example.medicalcalculatorapp.domain.model.*
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

class UserSettingsMapper(private val gson: Gson) {

    fun mapEntityToDomain(entity: UserSettingsEntity): UserSettings {
        return UserSettings(
            userId = entity.userId,
            theme = parseAppTheme(entity.theme),
            language = entity.language,
            notificationsEnabled = entity.notificationsEnabled,
            biometricAuthEnabled = entity.biometricAuthEnabled,
            autoSaveCalculations = entity.autoSaveCalculations,
            defaultUnits = parseUnitSystem(entity.defaultUnits),
            privacySettings = parsePrivacySettings(entity.privacySettingsJson),
            calculatorPreferences = parseCalculatorPreferences(entity.calculatorPreferencesJson),
            lastSyncTime = entity.lastSyncTime
        )
    }

    fun mapDomainToEntity(domain: UserSettings): UserSettingsEntity {
        return UserSettingsEntity(
            userId = domain.userId,
            theme = domain.theme.name,
            language = domain.language,
            notificationsEnabled = domain.notificationsEnabled,
            biometricAuthEnabled = domain.biometricAuthEnabled,
            autoSaveCalculations = domain.autoSaveCalculations,
            defaultUnits = domain.defaultUnits.name,
            privacySettingsJson = gson.toJson(domain.privacySettings),
            calculatorPreferencesJson = gson.toJson(domain.calculatorPreferences),
            lastSyncTime = domain.lastSyncTime,
            updatedAt = System.currentTimeMillis()
        )
    }

    private fun parseAppTheme(themeString: String): AppTheme {
        return try {
            AppTheme.valueOf(themeString)
        } catch (e: IllegalArgumentException) {
            AppTheme.SYSTEM // Default fallback
        }
    }

    private fun parseUnitSystem(unitString: String): UnitSystem {
        return try {
            UnitSystem.valueOf(unitString)
        } catch (e: IllegalArgumentException) {
            UnitSystem.METRIC // Default fallback
        }
    }

    private fun parsePrivacySettings(json: String): PrivacySettings {
        return try {
            if (json.isBlank()) {
                PrivacySettings() // Default
            } else {
                gson.fromJson(json, PrivacySettings::class.java) ?: PrivacySettings()
            }
        } catch (e: JsonSyntaxException) {
            PrivacySettings() // Default fallback on parse error
        }
    }

    private fun parseCalculatorPreferences(json: String): CalculatorPreferences {
        return try {
            if (json.isBlank()) {
                CalculatorPreferences() // Default
            } else {
                gson.fromJson(json, CalculatorPreferences::class.java) ?: CalculatorPreferences()
            }
        } catch (e: JsonSyntaxException) {
            CalculatorPreferences() // Default fallback on parse error
        }
    }
}