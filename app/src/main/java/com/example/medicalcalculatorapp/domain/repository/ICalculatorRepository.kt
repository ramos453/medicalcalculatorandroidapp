package com.example.medicalcalculatorapp.domain.repository

import com.example.medicalcalculatorapp.domain.model.MedicalCalculator
import kotlinx.coroutines.flow.Flow

interface ICalculatorRepository {
    fun getAllCalculators(): Flow<List<MedicalCalculator>>
    fun getCalculatorById(calculatorId: String): Flow<MedicalCalculator?>
    fun getCalculatorsByCategory(categoryId: String): Flow<List<MedicalCalculator>>
    fun getFavoriteCalculators(userId: String): Flow<List<MedicalCalculator>>
    fun searchCalculators(query: String): Flow<List<MedicalCalculator>>
    suspend fun toggleFavorite(calculatorId: String, userId: String): Boolean
    suspend fun isFavorite(calculatorId: String, userId: String): Flow<Boolean>
}