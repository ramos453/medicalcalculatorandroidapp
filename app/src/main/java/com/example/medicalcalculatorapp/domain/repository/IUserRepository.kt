package com.example.medicalcalculatorapp.domain.repository

import com.example.medicalcalculatorapp.domain.model.UserProfile
import com.example.medicalcalculatorapp.domain.model.UserSettings
import kotlinx.coroutines.flow.Flow

interface IUserRepository {
    // Profile management
    suspend fun getUserProfile(userId: String): Flow<UserProfile?>
    suspend fun updateUserProfile(userProfile: UserProfile): Boolean
    suspend fun createUserProfile(userProfile: UserProfile): Boolean
    suspend fun deleteUserProfile(userId: String): Boolean

    // Settings management
    suspend fun getUserSettings(userId: String): Flow<UserSettings?>
    suspend fun updateUserSettings(userSettings: UserSettings): Boolean
    suspend fun resetSettingsToDefault(userId: String): Boolean

    // Data export/import for GDPR compliance
    suspend fun exportUserData(userId: String): String? // JSON format
    suspend fun deleteAllUserData(userId: String): Boolean

    // Account management
    suspend fun updateEmail(userId: String, newEmail: String): Boolean
    suspend fun updatePassword(userId: String, oldPassword: String, newPassword: String): Boolean
    suspend fun verifyPassword(userId: String, password: String): Boolean
}