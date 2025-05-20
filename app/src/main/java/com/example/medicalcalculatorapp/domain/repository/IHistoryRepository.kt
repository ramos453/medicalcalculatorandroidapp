package com.example.medicalcalculatorapp.domain.repository

import com.example.medicalcalculatorapp.domain.model.CalculationResult
import kotlinx.coroutines.flow.Flow

interface IHistoryRepository {
    fun getHistoryForUser(userId: String): Flow<List<CalculationResult>>
    fun getHistoryForCalculator(calculatorId: String, userId: String): Flow<List<CalculationResult>>
    suspend fun saveCalculationResult(result: CalculationResult): Long
    suspend fun deleteHistoryItem(historyId: Long)
    suspend fun clearHistoryForUser(userId: String)
}