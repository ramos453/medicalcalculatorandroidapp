package com.example.medicalcalculatorapp.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.medicalcalculatorapp.data.db.entity.FieldEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FieldDao {
    @Query("SELECT * FROM fields WHERE calculatorId = :calculatorId ORDER BY displayOrder")
    fun getFieldsForCalculator(calculatorId: String): Flow<List<FieldEntity>>

    @Query("SELECT * FROM fields WHERE calculatorId = :calculatorId AND isInputField = 1 ORDER BY displayOrder")
    fun getInputFieldsForCalculator(calculatorId: String): Flow<List<FieldEntity>>

    @Query("SELECT * FROM fields WHERE calculatorId = :calculatorId AND isInputField = 0 ORDER BY displayOrder")
    fun getResultFieldsForCalculator(calculatorId: String): Flow<List<FieldEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertField(field: FieldEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFields(fields: List<FieldEntity>)

    @Update
    suspend fun updateField(field: FieldEntity)

    @Query("DELETE FROM fields WHERE calculatorId = :calculatorId")
    suspend fun deleteFieldsForCalculator(calculatorId: String)
}