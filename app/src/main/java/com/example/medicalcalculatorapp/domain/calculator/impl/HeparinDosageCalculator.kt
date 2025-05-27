package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult
import kotlin.math.round

class HeparinDosageCalculator : Calculator {

    override val calculatorId = "heparin_dosage"

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate weight
        val weightStr = inputs["patient_weight"]
        if (weightStr.isNullOrBlank()) {
            errors.add("El peso del paciente es obligatorio")
        } else {
            val weight = weightStr.toDoubleOrNull()
            if (weight == null) {
                errors.add("El peso debe ser un número válido")
            } else if (weight < 3.0 || weight > 200.0) {
                errors.add("El peso debe estar entre 3 kg y 200 kg")
            }
        }

        // Validate treatment type
        val treatmentType = inputs["treatment_type"]
        if (treatmentType.isNullOrBlank()) {
            errors.add("El tipo de tratamiento es obligatorio")
        } else if (treatmentType !in listOf("Profiláctico", "Terapéutico")) {
            errors.add("Tipo de tratamiento inválido")
        }

        // Validate dosing schedule for therapeutic treatment
        if (treatmentType == "Terapéutico") {
            val dosingSchedule = inputs["dosing_schedule"]
            if (dosingSchedule.isNullOrBlank()) {
                errors.add("El esquema de dosificación es obligatorio para tratamiento terapéutico")
            } else if (dosingSchedule !in listOf("1 mg/kg cada 12h", "1.5 mg/kg cada 24h")) {
                errors.add("Esquema de dosificación inválido")
            }
        }

        // Validate drug concentration if provided
        val concentrationStr = inputs["drug_concentration"]
        if (!concentrationStr.isNullOrBlank()) {
            val concentration = concentrationStr.toDoubleOrNull()
            if (concentration == null || concentration <= 0) {
                errors.add("La concentración debe ser un número válido mayor a 0")
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

        // Parse inputs
        val weight = inputs["patient_weight"]!!.toDouble()
        val treatmentType = inputs["treatment_type"]!!
        val dosingSchedule = inputs["dosing_schedule"] ?: ""
        val highBleedingRisk = inputs["high_bleeding_risk"] == "true"
        val renalInsufficiency = inputs["renal_insufficiency"] == "true"
        val elderlyPatient = inputs["elderly_patient"] == "true"
        val concentration = inputs["drug_concentration"]?.toDoubleOrNull()

        // Calculate dose based on treatment type
        val (dose, frequency) = when (treatmentType) {
            "Profiláctico" -> calculateProphylacticDose(highBleedingRisk, renalInsufficiency, elderlyPatient)
            "Terapéutico" -> calculateTherapeuticDose(weight, dosingSchedule, renalInsufficiency, elderlyPatient)
            else -> throw IllegalArgumentException("Invalid treatment type")
        }

        // Calculate volume if concentration is provided
        val volume = concentration?.let { dose / it }

        // Generate safety warnings and monitoring recommendations
        val safetyWarnings = generateSafetyWarnings(
            treatmentType, highBleedingRisk, renalInsufficiency, elderlyPatient, dose
        )
        val monitoringRecommendations = generateMonitoringRecommendations(
            treatmentType, renalInsufficiency, elderlyPatient
        )

        // Format results
        val results = mutableMapOf(
            "recommended_dose" to String.format("%.1f", dose),
            "administration_frequency" to frequency,
            "safety_warnings" to safetyWarnings,
            "monitoring_recommendations" to monitoringRecommendations
        )

        // Add volume if calculated
        volume?.let {
            results["volume_to_administer"] = String.format("%.2f", it)
        } ?: run {
            results["volume_to_administer"] = "No calculado (concentración no proporcionada)"
        }

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun calculateProphylacticDose(
        highBleedingRisk: Boolean,
        renalInsufficiency: Boolean,
        elderlyPatient: Boolean
    ): Pair<Double, String> {
        var dose = 40.0 // Standard prophylactic dose
        val frequency = "cada 24 horas"

        // Adjust for high bleeding risk
        if (highBleedingRisk) {
            dose = 20.0
        }

        // Adjust for renal insufficiency
        if (renalInsufficiency) {
            dose = 20.0 // Reduce dose for ClCr < 30 mL/min
        }

        // Consider elderly patient (>75 years)
        if (elderlyPatient && !highBleedingRisk && !renalInsufficiency) {
            dose = 30.0 // Intermediate dose for elderly
        }

        return Pair(dose, frequency)
    }

    private fun calculateTherapeuticDose(
        weight: Double,
        dosingSchedule: String,
        renalInsufficiency: Boolean,
        elderlyPatient: Boolean
    ): Pair<Double, String> {
        val (dosePerKg, frequency) = when (dosingSchedule) {
            "1 mg/kg cada 12h" -> Pair(1.0, "cada 12 horas")
            "1.5 mg/kg cada 24h" -> Pair(1.5, "cada 24 horas")
            else -> Pair(1.0, "cada 12 horas") // Default
        }

        var dose = dosePerKg * weight

        // Adjust for renal insufficiency (reduce by 25-50%)
        if (renalInsufficiency) {
            dose *= 0.75 // 25% reduction
        }

        // Round to nearest 2.5 mg (common syringe graduations)
        dose = round(dose / 2.5) * 2.5

        return Pair(dose, frequency)
    }

    private fun generateSafetyWarnings(
        treatmentType: String,
        highBleedingRisk: Boolean,
        renalInsufficiency: Boolean,
        elderlyPatient: Boolean,
        dose: Double
    ): String {
        val warnings = mutableListOf<String>()

        if (highBleedingRisk) {
            warnings.add("⚠️ ALTO RIESGO HEMORRÁGICO - Monitoreo estrecho")
        }

        if (renalInsufficiency) {
            warnings.add("⚠️ INSUFICIENCIA RENAL - Dosis ajustada")
        }

        if (elderlyPatient) {
            warnings.add("⚠️ PACIENTE GERIÁTRICO - Considerar factores adicionales")
        }

        if (treatmentType == "Terapéutico" && dose > 150.0) {
            warnings.add("⚠️ DOSIS ALTA - Verificar peso y esquema")
        }

        warnings.add("⚠️ Verificar contraindicaciones antes de administrar")
        warnings.add("⚠️ Monitorear signos de sangrado")

        return warnings.joinToString("\n• ", prefix = "• ")
    }

    private fun generateMonitoringRecommendations(
        treatmentType: String,
        renalInsufficiency: Boolean,
        elderlyPatient: Boolean
    ): String {
        val recommendations = mutableListOf<String>()

        when (treatmentType) {
            "Profiláctico" -> {
                recommendations.add("📊 Conteo plaquetario cada 2-3 días")
                recommendations.add("📊 Vigilancia de signos de sangrado")
            }
            "Terapéutico" -> {
                recommendations.add("📊 Anti-Xa a las 4h post-dosis (objetivo: 0.5-1.0 U/mL)")
                recommendations.add("📊 Conteo plaquetario cada 2-3 días")
                recommendations.add("📊 Creatinina sérica periódica")
            }
        }

        if (renalInsufficiency) {
            recommendations.add("📊 Monitoreo de función renal más frecuente")
            recommendations.add("📊 Considerar anti-Xa si disponible")
        }

        if (elderlyPatient) {
            recommendations.add("📊 Evaluación de caídas y sangrado")
        }

        recommendations.add("📊 Educar al paciente sobre signos de sangrado")

        return recommendations.joinToString("\n• ", prefix = "• ")
    }

    override fun getInterpretation(result: CalculationResult): String {
        val dose = result.resultValues["recommended_dose"] ?: ""
        val frequency = result.resultValues["administration_frequency"] ?: ""
        val volume = result.resultValues["volume_to_administer"] ?: ""
        val treatmentType = result.inputValues["treatment_type"] ?: ""

        return """
        **Interpretación Clínica - Heparina de Bajo Peso Molecular:**
        
        💉 **Dosis Recomendada:** $dose mg $frequency
        📏 **Volumen:** $volume
        🎯 **Tipo:** $treatmentType
        
        **📋 Protocolo Basado en:**
        • Hospital Universitario de Navarra (España)
        • Guías Europeas de Anticoagulación
        • Ajustes por función renal y factores de riesgo
        
        **⚠️ RECORDATORIO CRÍTICO:**
        • Esta calculadora es para ENOXAPARINA (Clexane®)
        • Verificar contraindicaciones antes de administrar
        • Monitoreo obligatorio según tipo de tratamiento
        • Ajustar dosis según respuesta clínica
        • En caso de sangrado, suspender inmediatamente
        
        **🔬 Fórmulas Utilizadas:**
        • Profiláctico: 40 mg/24h (20 mg si alto riesgo)
        • Terapéutico: 1 mg/kg/12h o 1.5 mg/kg/24h
        • Ajustes: -25% en insuficiencia renal
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "Protocolo de Anticoagulación con HBPM",
                source = "Hospital Universitario de Navarra, España",
                year = 2023
            ),
            Reference(
                title = "Guía ESC para el Diagnóstico y Manejo del Tromboembolismo Pulmonar",
                source = "European Society of Cardiology",
                year = 2022
            ),
            Reference(
                title = "Low-Molecular-Weight Heparin Dosing Guidelines",
                source = "American College of Chest Physicians",
                year = 2021
            ),
            Reference(
                title = "Anticoagulación en Insuficiencia Renal",
                source = "Sociedad Española de Nefrología",
                year = 2023
            )
        )
    }
}