package com.example.medicalcalculatorapp.data.repository

import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
import com.example.medicalcalculatorapp.data.db.mapper.UserProfileMapper
import com.example.medicalcalculatorapp.data.db.mapper.UserSettingsMapper
import com.example.medicalcalculatorapp.domain.model.UserProfile
import com.example.medicalcalculatorapp.domain.model.UserSettings
import com.example.medicalcalculatorapp.domain.repository.IUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val database: MedicalCalculatorDatabase,
    private val userProfileMapper: UserProfileMapper,
    private val userSettingsMapper: UserSettingsMapper
) : IUserRepository {

    // Basic Profile Operations
    override suspend fun getUserProfile(userId: String): Flow<UserProfile?> {
        return database.userProfileDao().getUserProfile(userId)
            .map { entity ->
                entity?.let { userProfileMapper.mapEntityToDomain(it) }
            }
    }

    override suspend fun createUserProfile(userProfile: UserProfile): Boolean {
        return try {
            val entity = userProfileMapper.mapDomainToEntity(userProfile)
            database.userProfileDao().insertUserProfile(entity)

            // Also create default settings for the user
            val defaultSettings = UserSettings(userId = userProfile.id)
            createDefaultUserSettings(defaultSettings)

            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun updateUserProfile(userProfile: UserProfile): Boolean {
        return try {
            val updatedProfile = userProfile.copy(updatedAt = System.currentTimeMillis())
            val entity = userProfileMapper.mapDomainToEntity(updatedProfile)
            database.userProfileDao().updateUserProfile(entity)
            true
        } catch (e: Exception) {
            false
        }
    }

    override suspend fun deleteUserProfile(userId: String): Boolean {
        return try {
            database.userProfileDao().deleteUserProfile(userId)
            // Also delete associated settings
            database.userSettingsDao().deleteUserSettings(userId)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Basic Settings Operations
    override suspend fun getUserSettings(userId: String): Flow<UserSettings?> {
        return database.userSettingsDao().getUserSettings(userId)
            .map { entity ->
                entity?.let { userSettingsMapper.mapEntityToDomain(it) }
            }
    }

    override suspend fun updateUserSettings(userSettings: UserSettings): Boolean {
        return try {
            val updatedSettings = userSettings.copy(lastSyncTime = System.currentTimeMillis())
            val entity = userSettingsMapper.mapDomainToEntity(updatedSettings)
            database.userSettingsDao().updateUserSettings(entity)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Helper method to create default settings
    private suspend fun createDefaultUserSettings(userSettings: UserSettings): Boolean {
        return try {
            val entity = userSettingsMapper.mapDomainToEntity(userSettings)
            database.userSettingsDao().insertUserSettings(entity)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Check if user exists
    suspend fun userExists(userId: String): Boolean {
        return database.userProfileDao().userExists(userId) > 0
    }

    // Check if settings exist
    suspend fun settingsExist(userId: String): Boolean {
        return database.userSettingsDao().settingsExist(userId) > 0
    }

    // Reset Settings to Default
    override suspend fun resetSettingsToDefault(userId: String): Boolean {
        return try {
            val defaultSettings = UserSettings(userId = userId)
            val entity = userSettingsMapper.mapDomainToEntity(defaultSettings)
            database.userSettingsDao().updateUserSettings(entity)
            true
        } catch (e: Exception) {
            false
        }
    }

    // GDPR Compliance - Data Export
    override suspend fun exportUserData(userId: String): String? {
        return try {
            val profile = database.userProfileDao().getUserProfile(userId)
            val settings = database.userSettingsDao().getUserSettings(userId)

            // Note: This is a simplified version. In real implementation,
            // you would collect profile and settings data and format as JSON
            val exportData = mapOf(
                "userId" to userId,
                "exportDate" to System.currentTimeMillis(),
                "note" to "User data export for GDPR compliance"
                // Add actual profile and settings data here
            )

            // Convert to JSON string (you'd use Gson here)
            "User data export placeholder for user: $userId"
        } catch (e: Exception) {
            null
        }
    }

    // GDPR Compliance - Complete Data Deletion
    override suspend fun deleteAllUserData(userId: String): Boolean {
        return try {
            // Delete from all user-related tables
            database.userProfileDao().deleteUserProfile(userId)
            database.userSettingsDao().deleteUserSettings(userId)

            // Also delete user's favorites, history, etc.
            database.favoriteDao().deleteFavorite("", userId) // This needs to be updated
            database.historyDao().clearHistoryForUser(userId)

            true
        } catch (e: Exception) {
            false
        }
    }

    // Account Management - Email Update
    override suspend fun updateEmail(userId: String, newEmail: String): Boolean {
        return try {
            val timestamp = System.currentTimeMillis()
            database.userProfileDao().updateEmail(userId, newEmail, timestamp)
            true
        } catch (e: Exception) {
            false
        }
    }

    // Account Management - Password Update (Placeholder)
    override suspend fun updatePassword(userId: String, oldPassword: String, newPassword: String): Boolean {
        // Note: This is a placeholder implementation
        // In a real app, you would:
        // 1. Verify the old password
        // 2. Hash the new password
        // 3. Store the hashed password securely
        return try {
            // Placeholder - always returns true for now
            // TODO: Implement actual password management
            true
        } catch (e: Exception) {
            false
        }
    }

    // Account Management - Password Verification (Placeholder)
    override suspend fun verifyPassword(userId: String, password: String): Boolean {
        // Note: This is a placeholder implementation
        // In a real app, you would:
        // 1. Retrieve the stored password hash
        // 2. Hash the provided password
        // 3. Compare the hashes
        return try {
            // Placeholder - always returns true for now
            // TODO: Implement actual password verification
            true
        } catch (e: Exception) {
            false
        }
    }
}