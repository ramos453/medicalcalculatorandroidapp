package com.example.medicalcalculatorapp.data.db.dao

import androidx.room.*
import com.example.medicalcalculatorapp.data.db.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserProfileDao {
    @Query("SELECT * FROM user_profiles WHERE id = :userId")
    fun getUserProfile(userId: String): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(userProfile: UserProfileEntity)

    @Update
    suspend fun updateUserProfile(userProfile: UserProfileEntity)

    @Query("DELETE FROM user_profiles WHERE id = :userId")
    suspend fun deleteUserProfile(userId: String)

    @Query("UPDATE user_profiles SET email = :email, updatedAt = :timestamp WHERE id = :userId")
    suspend fun updateEmail(userId: String, email: String, timestamp: Long)

    @Query("SELECT COUNT(*) FROM user_profiles WHERE id = :userId")
    suspend fun userExists(userId: String): Int
}