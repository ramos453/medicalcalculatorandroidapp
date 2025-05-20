package com.example.medicalcalculatorapp.data.repository

import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
import com.example.medicalcalculatorapp.data.db.entity.FavoriteEntity
import com.example.medicalcalculatorapp.data.db.mapper.CalculatorMapper
import com.example.medicalcalculatorapp.data.user.UserManager
import com.example.medicalcalculatorapp.domain.model.MedicalCalculator
import com.example.medicalcalculatorapp.domain.repository.ICalculatorRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalCoroutinesApi::class)
class CalculatorRepository(
    private val database: MedicalCalculatorDatabase,
    private val calculatorMapper: CalculatorMapper,
    private val userManager: UserManager
) : ICalculatorRepository {

    override fun getAllCalculators(): Flow<List<MedicalCalculator>> {
        val userId = userManager.getCurrentUserId()

        // Get all calculators, their fields, and favorite status
        val calculatorsFlow = database.calculatorDao().getAllCalculators()

        return calculatorsFlow.flatMapLatest { calculators ->
            // For each calculator, we need to fetch its fields and favorite status
            if (calculators.isEmpty()) {
                return@flatMapLatest flow { emit(emptyList<MedicalCalculator>()) }
            }

            // Create a flow for each calculator with its fields and favorite status
            val calculatorFlows = calculators.map { calculator ->
                combine(
                    database.fieldDao().getInputFieldsForCalculator(calculator.id),
                    database.fieldDao().getResultFieldsForCalculator(calculator.id),
                    database.favoriteDao().isCalculatorFavorited(calculator.id, userId)
                ) { inputFields, resultFields, isFavorite ->
                    calculatorMapper.mapEntityToDomain(
                        calculator,
                        inputFields,
                        resultFields,
                        isFavorite
                    )
                }
            }

            // Combine all calculator flows into a single list
            combine(calculatorFlows) { calculatorArray ->
                calculatorArray.toList()
            }
        }
    }

    override fun getCalculatorById(calculatorId: String): Flow<MedicalCalculator?> {
        val userId = userManager.getCurrentUserId()

        return flow {
            val calculator = database.calculatorDao().getCalculatorById(calculatorId) ?: return@flow emit(null)
            val inputFields = database.fieldDao().getInputFieldsForCalculator(calculatorId).firstOrNull() ?: emptyList()
            val resultFields = database.fieldDao().getResultFieldsForCalculator(calculatorId).firstOrNull() ?: emptyList()
            val isFavorite = database.favoriteDao().isCalculatorFavorited(calculatorId, userId).firstOrNull() ?: false

            emit(calculatorMapper.mapEntityToDomain(calculator, inputFields, resultFields, isFavorite))
        }
    }

    override fun getCalculatorsByCategory(categoryId: String): Flow<List<MedicalCalculator>> {
        val userId = userManager.getCurrentUserId()

        val calculatorsFlow = database.calculatorDao().getCalculatorsByCategory(categoryId)

        return calculatorsFlow.flatMapLatest { calculators ->
            if (calculators.isEmpty()) {
                return@flatMapLatest flow { emit(emptyList<MedicalCalculator>()) }
            }

            val calculatorFlows = calculators.map { calculator ->
                combine(
                    database.fieldDao().getInputFieldsForCalculator(calculator.id),
                    database.fieldDao().getResultFieldsForCalculator(calculator.id),
                    database.favoriteDao().isCalculatorFavorited(calculator.id, userId)
                ) { inputFields, resultFields, isFavorite ->
                    calculatorMapper.mapEntityToDomain(
                        calculator,
                        inputFields,
                        resultFields,
                        isFavorite
                    )
                }
            }

            combine(calculatorFlows) { calculatorArray ->
                calculatorArray.toList()
            }
        }
    }

    override fun getFavoriteCalculators(userId: String): Flow<List<MedicalCalculator>> {
        return database.calculatorDao().getFavoriteCalculators(userId)
            .flatMapLatest { calculators ->
                if (calculators.isEmpty()) {
                    return@flatMapLatest flow { emit(emptyList<MedicalCalculator>()) }
                }

                val calculatorFlows = calculators.map { calculator ->
                    combine(
                        database.fieldDao().getInputFieldsForCalculator(calculator.id),
                        database.fieldDao().getResultFieldsForCalculator(calculator.id),
                    ) { inputFields, resultFields ->
                        calculatorMapper.mapEntityToDomain(
                            calculator,
                            inputFields,
                            resultFields,
                            true // These are favorite calculators
                        )
                    }
                }

                combine(calculatorFlows) { calculatorArray ->
                    calculatorArray.toList()
                }
            }
    }

    override fun searchCalculators(query: String): Flow<List<MedicalCalculator>> {
        val userId = userManager.getCurrentUserId()

        return database.calculatorDao().searchCalculators(query)
            .flatMapLatest { calculators ->
                if (calculators.isEmpty()) {
                    return@flatMapLatest flow { emit(emptyList<MedicalCalculator>()) }
                }

                val calculatorFlows = calculators.map { calculator ->
                    combine(
                        database.fieldDao().getInputFieldsForCalculator(calculator.id),
                        database.fieldDao().getResultFieldsForCalculator(calculator.id),
                        database.favoriteDao().isCalculatorFavorited(calculator.id, userId)
                    ) { inputFields, resultFields, isFavorite ->
                        calculatorMapper.mapEntityToDomain(
                            calculator,
                            inputFields,
                            resultFields,
                            isFavorite
                        )
                    }
                }

                combine(calculatorFlows) { calculatorArray ->
                    calculatorArray.toList()
                }
            }
    }

    override suspend fun toggleFavorite(calculatorId: String, userId: String): Boolean {
        val isFavorite = database.favoriteDao().isCalculatorFavorited(calculatorId, userId).firstOrNull() ?: false

        if (isFavorite) {
            // Remove from favorites
            database.favoriteDao().deleteFavorite(calculatorId, userId)
            return false
        } else {
            // Add to favorites
            val favorite = FavoriteEntity(
                calculatorId = calculatorId,
                userId = userId
            )
            database.favoriteDao().insertFavorite(favorite)
            return true
        }
    }

    override suspend fun isFavorite(calculatorId: String, userId: String): Flow<Boolean> {
        return database.favoriteDao().isCalculatorFavorited(calculatorId, userId)
    }
}