package com.example.medicalcalculatorapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.medicalcalculatorapp.data.db.entity.CalculatorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculatorDao {
    @Query("SELECT * FROM calculators")
    fun getAllCalculators(): Flow<List<CalculatorEntity>>

    @Query("SELECT * FROM calculators WHERE categoryId = :categoryId")
    fun getCalculatorsByCategory(categoryId: String): Flow<List<CalculatorEntity>>

    @Query("SELECT * FROM calculators WHERE id = :calculatorId")
    suspend fun getCalculatorById(calculatorId: String): CalculatorEntity?

    @Query("SELECT * FROM calculators WHERE id IN (SELECT calculatorId FROM favorites WHERE userId = :userId)")
    fun getFavoriteCalculators(userId: String): Flow<List<CalculatorEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculator(calculator: CalculatorEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculators(calculators: List<CalculatorEntity>)

    @Update
    suspend fun updateCalculator(calculator: CalculatorEntity)

    @Query("DELETE FROM calculators WHERE id = :calculatorId")
    suspend fun deleteCalculator(calculatorId: String)

    @Query("SELECT * FROM calculators WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun searchCalculators(query: String): Flow<List<CalculatorEntity>>
}