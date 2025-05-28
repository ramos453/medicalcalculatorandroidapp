package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult

class MAPCalculator : Calculator {

    override val calculatorId = "map_calculator"

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate systolic BP
        val systolicStr = inputs["systolic_bp"]
        if (systolicStr.isNullOrBlank()) {
            errors.add("La presión sistólica es obligatoria")
        } else {
            val systolic = systolicStr.toDoubleOrNull()
            if (systolic == null) {
                errors.add("La presión sistólica debe ser un número válido")
            } else if (systolic < 50.0 || systolic > 250.0) {
                errors.add("La presión sistólica debe estar entre 50-250 mmHg")
            }
        }

        // Validate diastolic BP
        val diastolicStr = inputs["diastolic_bp"]
        if (diastolicStr.isNullOrBlank()) {
            errors.add("La presión diastólica es obligatoria")
        } else {
            val diastolic = diastolicStr.toDoubleOrNull()
            if (diastolic == null) {
                errors.add("La presión diastólica debe ser un número válido")
            } else if (diastolic < 30.0 || diastolic > 150.0) {
                errors.add("La presión diastólica debe estar entre 30-150 mmHg")
            }
        }

        // Cross-validation: systolic should be higher than diastolic
        val systolic = systolicStr?.toDoubleOrNull()
        val diastolic = diastolicStr?.toDoubleOrNull()
        if (systolic != null && diastolic != null) {
            if (systolic <= diastolic) {
                errors.add("La presión sistólica debe ser mayor que la diastólica")
            }
        }

        // Validate age if provided
        val ageStr = inputs["patient_age"]
        if (!ageStr.isNullOrBlank()) {
            val age = ageStr.toDoubleOrNull()
            if (age == null || age < 1.0 || age > 120.0) {
                errors.add("La edad debe estar entre 1-120 años")
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
        val systolic = inputs["systolic_bp"]!!.toDouble()
        val diastolic = inputs["diastolic_bp"]!!.toDouble()
        val age = inputs["patient_age"]?.toDoubleOrNull() ?: 45.0
        val clinicalContext = inputs["clinical_context"] ?: "Paciente Estable"

        // Calculate MAP using standard formula
        val map = (systolic + 2 * diastolic) / 3

        // Generate interpretations
        val mapInterpretation = interpretMAP(map, age, clinicalContext)
        val clinicalRecommendations = generateClinicalRecommendations(map, systolic, diastolic, clinicalContext)
        val perfusionStatus = assessPerfusionStatus(map, clinicalContext)

        // Format results
        val results = mapOf(
            "map" to String.format("%.1f", map),
            "map_interpretation" to mapInterpretation,
            "clinical_recommendations" to clinicalRecommendations,
            "perfusion_status" to perfusionStatus
        )

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun interpretMAP(map: Double, age: Double, context: String): String {
        return when {
            map >= 100 -> "PAM ELEVADA - Riesgo de daño vascular"
            map >= 90 -> "PAM ALTA - Considerar tratamiento antihipertensivo"
            map >= 70 -> "PAM NORMAL - Perfusión orgánica adecuada"
            map >= 60 -> "PAM LÍMITE - Monitoreo estrecho requerido"
            map >= 50 -> "PAM BAJA - Riesgo de hipoperfusión orgánica"
            else -> "PAM CRÍTICA - Hipoperfusión severa"
        }
    }

    private fun assessPerfusionStatus(map: Double, context: String): String {
        val baseAssessment = when {
            map >= 65 -> "PERFUSIÓN ADECUADA para la mayoría de órganos"
            map >= 60 -> "PERFUSIÓN LÍMITE - Vigilar función renal y cerebral"
            else -> "HIPOPERFUSIÓN - Riesgo de falla orgánica"
        }

        val contextualNote = when (context) {
            "Cuidados Intensivos" -> " (UCI: objetivo PAM >65 mmHg)"
            "Choque" -> " (Choque: objetivo PAM >65-70 mmHg)"
            "Postoperatorio" -> " (Post-Qx: mantener PAM >60 mmHg)"
            else -> ""
        }

        return baseAssessment + contextualNote
    }

    private fun generateClinicalRecommendations(map: Double, systolic: Double, diastolic: Double, context: String): String {
        val recommendations = mutableListOf<String>()

        when {
            map < 50 -> {
                recommendations.add("EMERGENCIA: Soporte vasopressor inmediato")
                recommendations.add("Evaluar causa de hipotensión (choque, sangrado)")
                recommendations.add("Monitoreo hemodinámico invasivo")
                recommendations.add("Acceso vascular central")
            }
            map < 60 -> {
                recommendations.add("URGENTE: Reposición de volumen")
                recommendations.add("Considerar vasopresores si no responde")
                recommendations.add("Monitoreo de diuresis cada hora")
                recommendations.add("Evaluar perfusión periférica")
            }
            map < 70 -> {
                recommendations.add("Monitoreo frecuente de signos vitales")
                recommendations.add("Evaluar estado de hidratación")
                recommendations.add("Vigilar función renal")
                recommendations.add("Considerar causas subyacentes")
            }
            map > 100 -> {
                recommendations.add("Evaluar hipertensión arterial")
                recommendations.add("Considerar tratamiento antihipertensivo")
                recommendations.add("Investigar daño a órgano blanco")
                recommendations.add("Control cada 4-6 horas")
            }
            else -> {
                recommendations.add("Mantener monitoreo de rutina")
                recommendations.add("Controles según protocolo institucional")
                recommendations.add("Vigilar tendencias y cambios")
            }
        }

        // Context-specific recommendations
        when (context) {
            "Cuidados Intensivos" -> {
                recommendations.add("Objetivo PAM >65 mmHg en UCI")
                recommendations.add("Considerar noradrenalina si PAM <60")
            }
            "Choque" -> {
                recommendations.add("Protocolo de choque séptico/cardiogénico")
                recommendations.add("Lactato sérico para evaluar perfusión")
            }
            "Postoperatorio" -> {
                recommendations.add("Evaluar pérdidas sanguíneas")
                recommendations.add("Analgesia adecuada para controlar TA")
            }
        }

        return recommendations.joinToString("\n")
    }

    override fun getInterpretation(result: CalculationResult): String {
        val map = result.resultValues["map"] ?: ""
        val systolic = result.inputValues["systolic_bp"] ?: ""
        val diastolic = result.inputValues["diastolic_bp"] ?: ""
        val interpretation = result.resultValues["map_interpretation"] ?: ""

        return """
INTERPRETACIÓN CLÍNICA - PRESIÓN ARTERIAL MEDIA

PAM CALCULADA: $map mmHg
PRESIÓN SISTÓLICA: $systolic mmHg
PRESIÓN DIASTÓLICA: $diastolic mmHg
EVALUACIÓN: $interpretation

FÓRMULA UTILIZADA:
PAM = (PAS + 2×PAD) ÷ 3
PAM = ($systolic + 2×$diastolic) ÷ 3 = $map mmHg

VALORES DE REFERENCIA:
• PAM ≥65 mmHg: Perfusión orgánica adecuada
• PAM 60-64 mmHg: Perfusión límite - monitoreo
• PAM <60 mmHg: Riesgo de hipoperfusión
• PAM <50 mmHg: Hipoperfusión crítica

SIGNIFICADO CLÍNICO:
La PAM representa la presión promedio durante el ciclo cardíaco y es el principal determinante de la perfusión orgánica. Es más confiable que la presión sistólica para evaluar la perfusión renal, cerebral y coronaria.

LIMITACIONES:
Valores pueden verse afectados por arritmias, edad del paciente, medicamentos vasoactivos y estados patológicos específicos.
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "Calculadora de Presión Arterial Media",
                source = "Omni Calculator en Español",
                url = "https://www.omnicalculator.com/es"
            ),
            Reference(
                title = "Manejo de la Hipertensión Arterial",
                source = "Instituto Mexicano del Seguro Social (IMSS)",
                year = 2023
            ),
            Reference(
                title = "Presión Arterial y Perfusión Orgánica",
                source = "SciELO México - Medicina Crítica",
                year = 2022
            ),
            Reference(
                title = "Guías de Hipertensión Arterial",
                source = "Sociedad Mexicana de Cardiología",
                year = 2023
            ),
            Reference(
                title = "Mean Arterial Pressure in Critical Care",
                source = "Revista de Reumatología Clínica",
                year = 2022
            )
        )
    }
}