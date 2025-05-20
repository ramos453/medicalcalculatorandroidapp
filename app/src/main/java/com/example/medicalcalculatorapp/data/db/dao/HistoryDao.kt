package com.example.medicalcalculatorapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.medicalcalculatorapp.data.db.entity.HistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getHistoryForUser(userId: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE calculatorId = :calculatorId AND userId = :userId ORDER BY timestamp DESC")
    fun getHistoryForCalculator(calculatorId: String, userId: String): Flow<List<HistoryEntity>>

    @Query("SELECT * FROM history WHERE historyId = :historyId")
    suspend fun getHistoryById(historyId: Long): HistoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: HistoryEntity): Long

    @Query("DELETE FROM history WHERE historyId = :historyId")
    suspend fun deleteHistory(historyId: Long)

    @Query("DELETE FROM history WHERE calculatorId = :calculatorId AND userId = :userId")
    suspend fun deleteHistoryForCalculator(calculatorId: String, userId: String)

    @Query("DELETE FROM history WHERE userId = :userId")
    suspend fun clearHistoryForUser(userId: String)
}