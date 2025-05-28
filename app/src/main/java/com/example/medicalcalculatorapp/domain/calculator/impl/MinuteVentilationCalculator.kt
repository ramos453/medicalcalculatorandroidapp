package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult

class MinuteVentilationCalculator : Calculator {

    override val calculatorId = "minute_ventilation"

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate respiratory rate
        val rrStr = inputs["respiratory_rate"]
        if (rrStr.isNullOrBlank()) {
            errors.add("La frecuencia respiratoria es obligatoria")
        } else {
            val rr = rrStr.toDoubleOrNull()
            if (rr == null) {
                errors.add("La frecuencia respiratoria debe ser un número válido")
            } else if (rr < 5.0 || rr > 60.0) {
                errors.add("La frecuencia respiratoria debe estar entre 5-60 resp/min")
            }
        }

        // Validate tidal volume
        val tvStr = inputs["tidal_volume"]
        if (tvStr.isNullOrBlank()) {
            errors.add("El volumen corriente es obligatorio")
        } else {
            val tv = tvStr.toDoubleOrNull()
            if (tv == null) {
                errors.add("El volumen corriente debe ser un número válido")
            } else if (tv < 200.0 || tv > 1000.0) {
                errors.add("El volumen corriente debe estar entre 200-1000 mL")
            }
        }

        // Validate weight if provided
        val weightStr = inputs["patient_weight"]
        if (!weightStr.isNullOrBlank()) {
            val weight = weightStr.toDoubleOrNull()
            if (weight == null || weight < 10.0 || weight > 200.0) {
                errors.add("El peso debe estar entre 10-200 kg")
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
        val respiratoryRate = inputs["respiratory_rate"]!!.toDouble()
        val tidalVolumeML = inputs["tidal_volume"]!!.toDouble()
        val weight = inputs["patient_weight"]?.toDoubleOrNull() ?: 70.0
        val clinicalSetting = inputs["clinical_setting"] ?: "Reposo"

        // Convert tidal volume to liters
        val tidalVolumeL = tidalVolumeML / 1000.0

        // Calculate minute ventilation: VE = FR × VT
        val minuteVentilation = respiratoryRate * tidalVolumeL

        // Calculate ventilation per kg
        val ventilationPerKg = (minuteVentilation * 1000) / weight // mL/kg/min

        // Generate assessments and recommendations
        val ventilationAssessment = assessVentilation(minuteVentilation, respiratoryRate, tidalVolumeL, clinicalSetting)
        val clinicalRecommendations = generateClinicalRecommendations(
            minuteVentilation, respiratoryRate, tidalVolumeL, ventilationPerKg, clinicalSetting
        )
        val alarmParameters = generateAlarmParameters(respiratoryRate, tidalVolumeL, minuteVentilation)

        // Format results
        val results = mapOf(
            "minute_ventilation" to String.format("%.2f", minuteVentilation),
            "ventilation_per_kg" to String.format("%.1f", ventilationPerKg),
            "ventilation_assessment" to ventilationAssessment,
            "clinical_recommendations" to clinicalRecommendations,
            "alarm_parameters" to alarmParameters
        )

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun assessVentilation(ve: Double, rr: Double, tv: Double, setting: String): String {
        val baseAssessment = when {
            ve < 4.0 -> "HIPOVENTILACIÓN - Ventilación inadecuada"
            ve <= 5.0 -> "VENTILACIÓN BAJA - Monitoreo estrecho"
            ve <= 8.0 -> "VENTILACIÓN NORMAL - Parámetros adecuados"
            ve <= 10.0 -> "VENTILACIÓN ELEVADA - Evaluar causa"
            else -> "HIPERVENTILACIÓN - Intervención requerida"
        }

        val contextualNote = when (setting) {
            "Ventilación Mecánica" -> " (VM: ajustar parámetros)"
            "Cuidados Intensivos" -> " (UCI: objetivo 6-8 L/min)"
            "Postoperatorio" -> " (Post-Qx: vigilar depresión respiratoria)"
            "Ejercicio" -> " (esperado aumento durante actividad)"
            else -> ""
        }

        return baseAssessment + contextualNote
    }

    private fun generateClinicalRecommendations(
        ve: Double, rr: Double, tv: Double, vePerKg: Double, setting: String
    ): String {
        val recommendations = mutableListOf<String>()

        // Minute ventilation based recommendations
        when {
            ve < 4.0 -> {
                recommendations.add("URGENTE: Evaluar insuficiencia respiratoria")
                recommendations.add("Considerar ventilación mecánica")
                recommendations.add("Gasometría arterial inmediata")
                recommendations.add("Monitoreo continuo de saturación")
            }
            ve < 5.0 -> {
                recommendations.add("Aumentar frecuencia de monitoreo")
                recommendations.add("Evaluar función pulmonar")
                recommendations.add("Considerar oxigenoterapia")
                recommendations.add("Vigilar signos de fatiga respiratoria")
            }
            ve > 10.0 -> {
                recommendations.add("Evaluar causa de hiperventilación")
                recommendations.add("Descartar dolor, ansiedad, acidosis")
                recommendations.add("Considerar sedación si apropiado")
                recommendations.add("Monitorear pH y CO2")
            }
            else -> {
                recommendations.add("Mantener monitoreo de rutina")
                recommendations.add("Controles según protocolo")
            }
        }

        // Component-specific recommendations
        if (rr < 12) {
            recommendations.add("BRADIAPNEA: Evaluar depresión del SNC")
        } else if (rr > 20) {
            recommendations.add("TAQUIPNEA: Investigar causa subyacente")
        }

        if (tv < 0.4) {
            recommendations.add("VOLUMEN BAJO: Riesgo de atelectasias")
        } else if (tv > 0.6) {
            recommendations.add("VOLUMEN ALTO: Riesgo de barotrauma")
        }

        // Setting-specific recommendations
        when (setting) {
            "Ventilación Mecánica" -> {
                recommendations.add("Ajustar parámetros del ventilador")
                recommendations.add("Objetivo: 6-8 mL/kg peso ideal")
            }
            "Cuidados Intensivos" -> {
                recommendations.add("Protocolo de destete si apropiado")
                recommendations.add("Evaluación diaria de sedación")
            }
            "Postoperatorio" -> {
                recommendations.add("Vigilar efectos de anestesia")
                recommendations.add("Fisioterapia respiratoria")
            }
        }

        return recommendations.joinToString("\n")
    }

    private fun generateAlarmParameters(rr: Double, tv: Double, ve: Double): String {
        val alarms = mutableListOf<String>()

        alarms.add("PARÁMETROS DE ALARMA SUGERIDOS:")

        // Respiratory rate alarms
        when {
            rr < 8 -> alarms.add("⚠️ FR CRÍTICA: <8 resp/min")
            rr < 12 -> alarms.add("⚠️ BRADIAPNEA: <12 resp/min")
            rr > 30 -> alarms.add("⚠️ TAQUIPNEA SEVERA: >30 resp/min")
            rr > 24 -> alarms.add("⚠️ TAQUIPNEA: >24 resp/min")
        }

        // Tidal volume alarms (in mL)
        val tvML = tv * 1000
        when {
            tvML < 300 -> alarms.add("⚠️ VT BAJO: <300 mL")
            tvML > 800 -> alarms.add("⚠️ VT ALTO: >800 mL")
        }

        // Minute ventilation alarms
        when {
            ve < 4.0 -> alarms.add("⚠️ VE CRÍTICA: <4 L/min")
            ve > 12.0 -> alarms.add("⚠️ VE ALTA: >12 L/min")
        }

        // Alarm limits summary
        alarms.add("")
        alarms.add("LÍMITES RECOMENDADOS:")
        alarms.add("• FR: 8-30 resp/min")
        alarms.add("• VT: 300-800 mL")
        alarms.add("• VE: 4-12 L/min")

        return alarms.joinToString("\n")
    }

    override fun getInterpretation(result: CalculationResult): String {
        val ve = result.resultValues["minute_ventilation"] ?: ""
        val vePerKg = result.resultValues["ventilation_per_kg"] ?: ""
        val rr = result.inputValues["respiratory_rate"] ?: ""
        val tv = result.inputValues["tidal_volume"] ?: ""
        val assessment = result.resultValues["ventilation_assessment"] ?: ""

        return """
INTERPRETACIÓN CLÍNICA - VENTILACIÓN MINUTO

VENTILACIÓN MINUTO: $ve L/min
VENTILACIÓN/PESO: $vePerKg mL/kg/min
FRECUENCIA RESPIRATORIA: $rr resp/min
VOLUMEN CORRIENTE: $tv mL
EVALUACIÓN: $assessment

FÓRMULA UTILIZADA:
VE = FR × VT
VE = $rr × ${String.format("%.3f", (tv.toDouble()/1000))} = $ve L/min

VALORES DE REFERENCIA:
• VE normal en reposo: 5-8 L/min
• FR normal adultos: 12-20 resp/min
• VT normal adultos: 400-600 mL
• VE/kg normal: 80-120 mL/kg/min

SIGNIFICADO CLÍNICO:
La ventilación minuto representa el volumen total de aire movilizado por los pulmones en un minuto. Es fundamental para evaluar la eficacia ventilatoria y guiar ajustes en ventilación mecánica.

APLICACIONES:
• Monitoreo de pacientes críticos
• Ajuste de parámetros ventilatorios
• Evaluación de función pulmonar
• Detección de fatiga respiratoria
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "MediCalculator - Ventilación Minuto",
                source = "ScyMed Medical Calculators",
                url = "https://scymed.com"
            ),
            Reference(
                title = "Parámetros Ventilatorios",
                source = "Philips Healthcare México",
                url = "https://philips.com.mx"
            ),
            Reference(
                title = "Ventilación Mecánica en UCI",
                source = "Educación en Salud IMSS",
                year = 2023
            ),
            Reference(
                title = "Calculadora de Ventilación",
                source = "Omni Calculator en Español",
                url = "https://www.omnicalculator.com/es"
            ),
            Reference(
                title = "Fisiología Respiratoria Aplicada",
                source = "Universidad de Los Lagos Chile",
                year = 2022
            )
        )
    }
}