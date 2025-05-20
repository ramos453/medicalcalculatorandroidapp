package com.example.medicalcalculatorapp.data.db.mapper

import com.example.medicalcalculatorapp.data.db.entity.HistoryEntity
import com.example.medicalcalculatorapp.domain.model.CalculationResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class HistoryMapper(private val gson: Gson) {

    fun mapEntityToDomain(entity: HistoryEntity): CalculationResult {
        val inputType = object : TypeToken<Map<String, String>>() {}.type
        val resultType = object : TypeToken<Map<String, String>>() {}.type

        return CalculationResult(
            calculatorId = entity.calculatorId,
            timestamp = entity.timestamp,
            inputValues = gson.fromJson(entity.inputValues, inputType),
            resultValues = gson.fromJson(entity.resultValues, resultType)
        )
    }

    fun mapDomainToEntity(domain: CalculationResult, userId: String): HistoryEntity {
        return HistoryEntity(
            calculatorId = domain.calculatorId,
            userId = userId,
            timestamp = domain.timestamp,
            inputValues = gson.toJson(domain.inputValues),
            resultValues = gson.toJson(domain.resultValues)
        )
    }
}