package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult

class MedicationDosageCalculator : Calculator {

    override val calculatorId = "medication_dosage"

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Get and validate weight
        val weightStr = inputs["patient_weight"]
        if (weightStr.isNullOrBlank()) {
            errors.add("El peso del paciente es obligatorio")
        } else {
            val weight = weightStr.toDoubleOrNull()
            if (weight == null) {
                errors.add("El peso debe ser un número válido")
            } else if (weight < 0.5 || weight > 250.0) {
                errors.add("El peso debe estar entre 0.5 kg y 250 kg")
            }
        }

        // Get and validate dose per kg
        val doseStr = inputs["dose_per_kg"]
        if (doseStr.isNullOrBlank()) {
            errors.add("La dosis por kg es obligatoria")
        } else {
            val dose = doseStr.toDoubleOrNull()
            if (dose == null) {
                errors.add("La dosis debe ser un número válido")
            } else if (dose <= 0 || dose > 100.0) {
                errors.add("La dosis debe ser mayor a 0 y típicamente menor a 100 mg/kg")
            }
        }

        // Get and validate concentration
        val concStr = inputs["concentration"]
        if (concStr.isNullOrBlank()) {
            errors.add("La concentración es obligatoria")
        } else {
            val concentration = concStr.toDoubleOrNull()
            if (concentration == null) {
                errors.add("La concentración debe ser un número válido")
            } else if (concentration <= 0) {
                errors.add("La concentración debe ser mayor a 0")
            }
        }

        return ValidationResult(
            isValid = errors.isEmpty(),
            errors = errors
        )
    }

    override fun calculate(inputs: Map<String, String>): CalculationResult {
        val validation = validate(inputs)
        if (!validation.isValid) {
            throw IllegalArgumentException(validation.errors.joinToString("; "))
        }

        // Parse validated inputs
        val weight = inputs["patient_weight"]!!.toDouble()
        val dosePerKg = inputs["dose_per_kg"]!!.toDouble()
        val concentration = inputs["concentration"]!!.toDouble()

        // Perform calculations
        // Formula: Total Dose (mg) = Dose (mg/kg) × Weight (kg)
        val totalDose = dosePerKg * weight

        // Formula: Volume to administer (mL) = Total dose (mg) / Concentration (mg/mL)
        val volumeToAdminister = totalDose / concentration

        // Generate safety check
        val safetyCheck = generateSafetyCheck(totalDose, volumeToAdminister, weight)

        // Format results
        val results = mapOf(
            "total_dose" to String.format("%.2f", totalDose),
            "volume_to_administer" to String.format("%.2f", volumeToAdminister),
            "safety_check" to safetyCheck
        )

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun generateSafetyCheck(totalDose: Double, volume: Double, weight: Double): String {
        return when {
            volume > 20.0 -> "⚠️ Volumen alto - Verificar cálculo"
            volume < 0.1 -> "⚠️ Volumen muy pequeño - Verificar precisión"
            totalDose > weight * 50 -> "⚠️ Dosis alta - Consultar con médico"
            else -> "✅ Cálculo dentro de rangos normales"
        }
    }

    override fun getInterpretation(result: CalculationResult): String {
        val totalDose = result.resultValues["total_dose"] ?: ""
        val volume = result.resultValues["volume_to_administer"] ?: ""
        val safetyCheck = result.resultValues["safety_check"] ?: ""

        return """
        **Interpretación Clínica:**
        
        📋 **Dosis Calculada:** $totalDose mg
        💉 **Volumen a Administrar:** $volume mL
        
        🔍 **Verificación:** $safetyCheck
        
        **⚠️ IMPORTANTE:**
        • Siempre verificar la dosis con un profesional médico
        • Confirmar la concentración del medicamento antes de administrar
        • Considerar factores individuales del paciente (edad, función renal/hepática)
        • Para medicamentos de alto riesgo, usar el principio de doble verificación
        
        **Fórmulas utilizadas:**
        • Dosis total = Dosis (mg/kg) × Peso (kg)
        • Volumen = Dosis total (mg) ÷ Concentración (mg/mL)
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "Cálculo de Dosis de Medicamentos en Enfermería",
                source = "Elsevier - Enfermería Clínica (España)",
                year = 2023
            ),
            Reference(
                title = "Medication Dosage Calculations",
                source = "WTCS Pressbooks",
                url = "https://wtcs.pressbooks.pub/dosagecalculations/"
            ),
            Reference(
                title = "Safe Medication Administration",
                source = "World Health Organization",
                year = 2022
            )
        )
    }
}