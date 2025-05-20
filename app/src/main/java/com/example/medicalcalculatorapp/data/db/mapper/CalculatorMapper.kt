package com.example.medicalcalculatorapp.data.db.mapper

import com.example.medicalcalculatorapp.data.db.entity.CalculatorEntity
import com.example.medicalcalculatorapp.data.db.entity.FieldEntity
import com.example.medicalcalculatorapp.domain.model.CalculatorField
import com.example.medicalcalculatorapp.domain.model.FieldType
import com.example.medicalcalculatorapp.domain.model.MedicalCalculator
import com.google.gson.Gson

class CalculatorMapper(private val gson: Gson) {

    fun mapEntityToDomain(
        calculatorEntity: CalculatorEntity,
        inputFields: List<FieldEntity>,
        resultFields: List<FieldEntity>,
        isFavorite: Boolean = false
    ): MedicalCalculator {
        return MedicalCalculator(
            id = calculatorEntity.id,
            name = calculatorEntity.name,
            description = calculatorEntity.description,
            category = calculatorEntity.categoryId,
            isFavorite = isFavorite,
            lastUsed = calculatorEntity.lastUpdated,
            inputFields = inputFields.map { mapFieldEntityToDomain(it) },
            resultFields = resultFields.map { mapFieldEntityToDomain(it) }
        )
    }

    private fun mapFieldEntityToDomain(fieldEntity: FieldEntity): CalculatorField {
        return CalculatorField(
            id = fieldEntity.id,
            name = fieldEntity.name,
            type = FieldType.valueOf(fieldEntity.type),
            units = fieldEntity.units,
            minValue = fieldEntity.minValue,
            maxValue = fieldEntity.maxValue,
            defaultValue = fieldEntity.defaultValue,
            options = fieldEntity.options?.let {
                gson.fromJson(it, Array<String>::class.java).toList()
            }
        )
    }

    fun mapDomainToEntity(calculator: MedicalCalculator): CalculatorEntity {
        return CalculatorEntity(
            id = calculator.id,
            name = calculator.name,
            description = calculator.description,
            categoryId = calculator.category,
            lastUpdated = calculator.lastUsed ?: System.currentTimeMillis()
        )
    }

    fun mapDomainToFieldEntities(calculator: MedicalCalculator): List<FieldEntity> {
        val fields = mutableListOf<FieldEntity>()

        // Map input fields
        calculator.inputFields.forEachIndexed { index, field ->
            fields.add(
                FieldEntity(
                    calculatorId = calculator.id,
                    id = field.id,
                    name = field.name,
                    type = field.type.name,
                    isInputField = true,
                    units = field.units,
                    minValue = field.minValue,
                    maxValue = field.maxValue,
                    defaultValue = field.defaultValue,
                    options = field.options?.let { gson.toJson(it) },
                    displayOrder = index
                )
            )
        }

        // Map result fields
        calculator.resultFields.forEachIndexed { index, field ->
            fields.add(
                FieldEntity(
                    calculatorId = calculator.id,
                    id = field.id,
                    name = field.name,
                    type = field.type.name,
                    isInputField = false,
                    units = field.units,
                    minValue = field.minValue,
                    maxValue = field.maxValue,
                    defaultValue = field.defaultValue,
                    options = field.options?.let { gson.toJson(it) },
                    displayOrder = index
                )
            )
        }

        return fields
    }
}