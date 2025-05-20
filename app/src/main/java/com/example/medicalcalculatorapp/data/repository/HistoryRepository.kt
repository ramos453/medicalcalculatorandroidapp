package com.example.medicalcalculatorapp.data.repository

import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
import com.example.medicalcalculatorapp.data.db.mapper.HistoryMapper
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.model.CalculationResult
import com.example.medicalcalculatorapp.domain.repository.IHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepository(
    private val database: MedicalCalculatorDatabase,
    private val historyMapper: HistoryMapper,
    private val userManager: UserManager
) : IHistoryRepository {

    override fun getHistoryForUser(userId: String): Flow<List<CalculationResult>> {
        return database.historyDao().getHistoryForUser(userId)
            .map { entities ->
                entities.map { historyMapper.mapEntityToDomain(it) }
            }
    }

    override fun getHistoryForCalculator(calculatorId: String, userId: String): Flow<List<CalculationResult>> {
        return database.historyDao().getHistoryForCalculator(calculatorId, userId)
            .map { entities ->
                entities.map { historyMapper.mapEntityToDomain(it) }
            }
    }

    override suspend fun saveCalculationResult(result: CalculationResult): Long {
        val userId = userManager.getCurrentUserId()
        val entity = historyMapper.mapDomainToEntity(result, userId)
        return database.historyDao().insertHistory(entity)
    }

    override suspend fun deleteHistoryItem(historyId: Long) {
        database.historyDao().deleteHistory(historyId)
    }

    override suspend fun clearHistoryForUser(userId: String) {
        database.historyDao().clearHistoryForUser(userId)
    }
}