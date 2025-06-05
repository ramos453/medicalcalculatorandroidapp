package com.example.medicalcalculatorapp.data.db.dao

import androidx.room.*
import com.example.medicalcalculatorapp.data.db.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserSettingsDao {
    @Query("SELECT * FROM user_settings WHERE userId = :userId")
    fun getUserSettings(userId: String): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserSettings(userSettings: UserSettingsEntity)

    @Update
    suspend fun updateUserSettings(userSettings: UserSettingsEntity)

    @Query("DELETE FROM user_settings WHERE userId = :userId")
    suspend fun deleteUserSettings(userId: String)

    @Query("UPDATE user_settings SET theme = :theme, updatedAt = :timestamp WHERE userId = :userId")
    suspend fun updateTheme(userId: String, theme: String, timestamp: Long)

    @Query("UPDATE user_settings SET language = :language, updatedAt = :timestamp WHERE userId = :userId")
    suspend fun updateLanguage(userId: String, language: String, timestamp: Long)

    @Query("UPDATE user_settings SET notificationsEnabled = :enabled, updatedAt = :timestamp WHERE userId = :userId")
    suspend fun updateNotificationSettings(userId: String, enabled: Boolean, timestamp: Long)

    @Query("SELECT COUNT(*) FROM user_settings WHERE userId = :userId")
    suspend fun settingsExist(userId: String): Int
}