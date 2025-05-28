// Add this import to your AppDependencies.kt file:
// import com.example.medicalcalculatorapp.domain.calculator.impl.BradenScaleCalculator

// Then add this line in your createCalculatorService() method:
// service.registerCalculator(BradenScaleCalculator())


package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult

class BradenScaleCalculator : Calculator {

    override val calculatorId = "braden_scale"

    // Braden Scale item definitions with scoring options
    private val bradenItems = mapOf(
        "sensory_perception" to mapOf(
            1 to "Completamente limitada - No responde a estímulos dolorosos",
            2 to "Muy limitada - Responde solo a estímulos dolorosos",
            3 to "Ligeramente limitada - Responde a órdenes verbales",
            4 to "Sin alteraciones - Responde a órdenes verbales"
        ),
        "moisture" to mapOf(
            1 to "Constantemente húmeda - Piel húmeda constantemente",
            2 to "Muy húmeda - Piel húmeda frecuentemente",
            3 to "Ocasionalmente húmeda - Requiere cambio de ropa adicional",
            4 to "Raramente húmeda - Piel generalmente seca"
        ),
        "activity" to mapOf(
            1 to "Encamado - Confinado a la cama",
            2 to "En silla - Capacidad de caminar severamente limitada",
            3 to "Camina ocasionalmente - Camina ocasionalmente durante el día",
            4 to "Camina frecuentemente - Camina fuera de la habitación al menos dos veces al día"
        ),
        "mobility" to mapOf(
            1 to "Completamente inmóvil - No hace cambios de posición",
            2 to "Muy limitada - Ocasionalmente hace cambios leves de posición",
            3 to "Ligeramente limitada - Hace cambios frecuentes pero leves",
            4 to "Sin limitaciones - Hace cambios importantes y frecuentes de posición"
        ),
        "nutrition" to mapOf(
            1 to "Muy pobre - Nunca come una comida completa",
            2 to "Probablemente inadecuada - Raramente come una comida completa",
            3 to "Adecuada - Come más de la mitad de la mayoría de comidas",
            4 to "Excelente - Come la mayoría de cada comida"
        ),
        "friction_shear" to mapOf(
            1 to "Problema - Requiere asistencia moderada a máxima para moverse",
            2 to "Problema potencial - Se mueve débilmente o requiere mínima asistencia",
            3 to "Sin problema aparente - Se mueve en cama y silla independientemente"
        )
    )

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate all required fields are present
        val requiredFields = listOf(
            "sensory_perception", "moisture", "activity",
            "mobility", "nutrition", "friction_shear"
        )

        for (field in requiredFields) {
            val valueStr = inputs[field]
            if (valueStr.isNullOrBlank()) {
                errors.add("${getFieldDisplayName(field)} es obligatorio")
            } else {
                val value = valueStr.toIntOrNull()
                if (value == null) {
                    errors.add("${getFieldDisplayName(field)} debe ser un número válido")
                } else {
                    // Validate score ranges
                    val validRange = if (field == "friction_shear") 1..3 else 1..4
                    if (value !in validRange) {
                        errors.add("${getFieldDisplayName(field)} debe estar entre ${validRange.first}-${validRange.last}")
                    }
                }
            }
        }

        // Validate patient information if provided
        val ageStr = inputs["patient_age"]
        if (!ageStr.isNullOrBlank()) {
            val age = ageStr.toIntOrNull()
            if (age == null || age < 0 || age > 120) {
                errors.add("La edad debe estar entre 0-120 años")
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
        val sensoryPerception = inputs["sensory_perception"]!!.toInt()
        val moisture = inputs["moisture"]!!.toInt()
        val activity = inputs["activity"]!!.toInt()
        val mobility = inputs["mobility"]!!.toInt()
        val nutrition = inputs["nutrition"]!!.toInt()
        val frictionShear = inputs["friction_shear"]!!.toInt()

        // Patient information (optional)
        val patientAge = inputs["patient_age"]?.toIntOrNull()
        val hasChronicConditions = inputs["chronic_conditions"] == "true"
        val onBedRest = inputs["bed_rest"] == "true"
        val criticalIllness = inputs["critical_illness"] == "true"

        // Calculate total score
        val totalScore = sensoryPerception + moisture + activity + mobility + nutrition + frictionShear

        // Determine risk level
        val riskLevel = determineRiskLevel(totalScore)
        val riskInterpretation = interpretRisk(totalScore, riskLevel)

        // Generate detailed assessment
        val detailedAssessment = generateDetailedAssessment(
            sensoryPerception, moisture, activity, mobility, nutrition, frictionShear
        )

        // Generate prevention recommendations
        val preventionRecommendations = generatePreventionRecommendations(
            totalScore, riskLevel, hasChronicConditions, onBedRest, criticalIllness
        )

        // Generate monitoring schedule
        val monitoringSchedule = generateMonitoringSchedule(riskLevel, criticalIllness)

        // Generate risk factors analysis
        val riskFactorsAnalysis = analyzeRiskFactors(
            sensoryPerception, moisture, activity, mobility, nutrition, frictionShear, patientAge
        )

        // Format results
        val results = mapOf(
            "total_score" to totalScore.toString(),
            "risk_level" to riskLevel,
            "risk_interpretation" to riskInterpretation,
            "detailed_assessment" to detailedAssessment,
            "prevention_recommendations" to preventionRecommendations,
            "monitoring_schedule" to monitoringSchedule,
            "risk_factors_analysis" to riskFactorsAnalysis
        )

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun getFieldDisplayName(field: String): String {
        return when (field) {
            "sensory_perception" -> "Percepción sensorial"
            "moisture" -> "Exposición a la humedad"
            "activity" -> "Actividad"
            "mobility" -> "Movilidad"
            "nutrition" -> "Nutrición"
            "friction_shear" -> "Fricción y deslizamiento"
            else -> field
        }
    }

    private fun determineRiskLevel(score: Int): String {
        return when (score) {
            in 19..23 -> "Sin riesgo"
            in 15..18 -> "Riesgo leve"
            in 13..14 -> "Riesgo moderado"
            in 10..12 -> "Riesgo alto"
            else -> "Riesgo muy alto" // ≤9
        }
    }

    private fun interpretRisk(score: Int, riskLevel: String): String {
        return when (riskLevel) {
            "Sin riesgo" -> "✅ RIESGO MÍNIMO - Paciente con bajo riesgo de desarrollar úlceras por presión. Mantener cuidados preventivos básicos."
            "Riesgo leve" -> "⚠️ RIESGO LEVE - Iniciar medidas preventivas. Evaluación diaria y cambios posturales regulares."
            "Riesgo moderado" -> "🟡 RIESGO MODERADO - Implementar protocolo de prevención. Cambios posturales cada 2 horas y superficies de apoyo."
            "Riesgo alto" -> "🔶 RIESGO ALTO - Protocol intensivo requerido. Cambios posturales cada 1-2 horas, colchón especializado."
            "Riesgo muy alto" -> "🚨 RIESGO MUY ALTO - Medidas preventivas máximas. Supervisión constante, colchón de presión alterna."
            else -> "Evaluación requerida"
        }
    }

    private fun generateDetailedAssessment(
        sensory: Int, moisture: Int, activity: Int,
        mobility: Int, nutrition: Int, friction: Int
    ): String {
        val assessment = mutableListOf<String>()

        assessment.add("📊 EVALUACIÓN DETALLADA POR ÁREA:")
        assessment.add("")

        // Analyze each component
        assessment.add("🧠 PERCEPCIÓN SENSORIAL ($sensory/4):")
        assessment.add("• ${bradenItems["sensory_perception"]?.get(sensory) ?: "Valor no válido"}")
        if (sensory <= 2) assessment.add("• ⚠️ FACTOR DE ALTO RIESGO - Percepción limitada")
        assessment.add("")

        assessment.add("💧 EXPOSICIÓN A HUMEDAD ($moisture/4):")
        assessment.add("• ${bradenItems["moisture"]?.get(moisture) ?: "Valor no válido"}")
        if (moisture <= 2) assessment.add("• ⚠️ FACTOR DE RIESGO - Exposición excesiva a humedad")
        assessment.add("")

        assessment.add("🚶 ACTIVIDAD ($activity/4):")
        assessment.add("• ${bradenItems["activity"]?.get(activity) ?: "Valor no válido"}")
        if (activity <= 2) assessment.add("• ⚠️ FACTOR DE ALTO RIESGO - Actividad muy limitada")
        assessment.add("")

        assessment.add("🔄 MOVILIDAD ($mobility/4):")
        assessment.add("• ${bradenItems["mobility"]?.get(mobility) ?: "Valor no válido"}")
        if (mobility <= 2) assessment.add("• ⚠️ FACTOR DE ALTO RIESGO - Movilidad severamente limitada")
        assessment.add("")

        assessment.add("🍽️ NUTRICIÓN ($nutrition/4):")
        assessment.add("• ${bradenItems["nutrition"]?.get(nutrition) ?: "Valor no válido"}")
        if (nutrition <= 2) assessment.add("• ⚠️ FACTOR DE RIESGO - Estado nutricional comprometido")
        assessment.add("")

        assessment.add("⚡ FRICCIÓN Y DESLIZAMIENTO ($friction/3):")
        assessment.add("• ${bradenItems["friction_shear"]?.get(friction) ?: "Valor no válido"}")
        if (friction <= 2) assessment.add("• ⚠️ FACTOR DE RIESGO - Problemas de fricción/deslizamiento")

        return assessment.joinToString("\n")
    }

    private fun generatePreventionRecommendations(
        score: Int, riskLevel: String, hasChronicConditions: Boolean,
        onBedRest: Boolean, criticalIllness: Boolean
    ): String {
        val recommendations = mutableListOf<String>()

        recommendations.add("🛡️ MEDIDAS PREVENTIVAS ESPECÍFICAS:")
        recommendations.add("")

        when (riskLevel) {
            "Sin riesgo" -> {
                recommendations.add("✅ CUIDADOS BÁSICOS:")
                recommendations.add("• Inspección de piel diaria")
                recommendations.add("• Mantener piel limpia y seca")
                recommendations.add("• Cambios posturales cada 4 horas")
                recommendations.add("• Educación al paciente y familia")
            }
            "Riesgo leve" -> {
                recommendations.add("⚠️ PREVENCIÓN ACTIVA:")
                recommendations.add("• Inspección de piel cada turno (8 horas)")
                recommendations.add("• Cambios posturales cada 3 horas")
                recommendations.add("• Uso de almohadas para alivio de presión")
                recommendations.add("• Mantener nutrición e hidratación adecuada")
                recommendations.add("• Protección de prominencias óseas")
            }
            "Riesgo moderado" -> {
                recommendations.add("🟡 PROTOCOLO INTENSIVO:")
                recommendations.add("• Inspección de piel cada 4 horas")
                recommendations.add("• Cambios posturales cada 2 horas")
                recommendations.add("• Colchón de espuma de alta densidad")
                recommendations.add("• Cojines de alivio de presión")
                recommendations.add("• Evaluación nutricional especializada")
                recommendations.add("• Mantener cabecera <30° cuando sea posible")
            }
            "Riesgo alto" -> {
                recommendations.add("🔶 PREVENCIÓN MÁXIMA:")
                recommendations.add("• Inspección de piel cada 2 horas")
                recommendations.add("• Cambios posturales cada 1-2 horas")
                recommendations.add("• Colchón de presión alterna o aire")
                recommendations.add("• Superficies de apoyo especializadas")
                recommendations.add("• Suplementación nutricional si indicado")
                recommendations.add("• Evitar fricción durante movilización")
                recommendations.add("• Usar dispositivos de elevación")
            }
            "Riesgo muy alto" -> {
                recommendations.add("🚨 MEDIDAS MÁXIMAS:")
                recommendations.add("• Inspección continua de la piel")
                recommendations.add("• Cambios posturales cada hora")
                recommendations.add("• Colchón de aire de presión baja")
                recommendations.add("• Cama especializada si disponible")
                recommendations.add("• Supervisión nutricional diaria")
                recommendations.add("• Equipo multidisciplinario")
                recommendations.add("• Documentación exhaustiva")
                recommendations.add("• Consulta especializada en heridas")
            }
        }

        // Additional considerations
        if (hasChronicConditions) {
            recommendations.add("")
            recommendations.add("🏥 CONSIDERACIONES ESPECIALES - CONDICIONES CRÓNICAS:")
            recommendations.add("• Manejo optimizado de diabetes")
            recommendations.add("• Control de enfermedades vasculares")
            recommendations.add("• Evaluación de medicamentos")
        }

        if (onBedRest) {
            recommendations.add("")
            recommendations.add("🛏️ PROTOCOLO ESPECIAL - REPOSO EN CAMA:")
            recommendations.add("• Programa de movilización pasiva")
            recommendations.add("• Ejercicios de rango de movimiento")
            recommendations.add("• Fisioterapia respiratoria")
        }

        if (criticalIllness) {
            recommendations.add("")
            recommendations.add("🚨 CUIDADOS CRÍTICOS:")
            recommendations.add("• Monitoreo hemodinámico")
            recommendations.add("• Manejo de sedación y analgesia")
            recommendations.add("• Prevención de complicaciones")
        }

        return recommendations.joinToString("\n")
    }

    private fun generateMonitoringSchedule(riskLevel: String, criticalIllness: Boolean): String {
        val schedule = mutableListOf<String>()

        schedule.add("📅 CRONOGRAMA DE MONITOREO:")
        schedule.add("")

        when (riskLevel) {
            "Sin riesgo" -> {
                schedule.add("• Evaluación Braden: Semanal")
                schedule.add("• Inspección de piel: Diaria")
                schedule.add("• Documentación: Semanal")
            }
            "Riesgo leve" -> {
                schedule.add("• Evaluación Braden: Cada 3 días")
                schedule.add("• Inspección de piel: Cada turno (8h)")
                schedule.add("• Documentación: Cada 3 días")
                schedule.add("• Revisión de medidas: Semanal")
            }
            "Riesgo moderado" -> {
                schedule.add("• Evaluación Braden: Cada 48 horas")
                schedule.add("• Inspección de piel: Cada 4 horas")
                schedule.add("• Documentación: Diaria")
                schedule.add("• Revisión del plan: Cada 3 días")
            }
            "Riesgo alto" -> {
                schedule.add("• Evaluación Braden: Diaria")
                schedule.add("• Inspección de piel: Cada 2 horas")
                schedule.add("• Documentación: Cada turno")
                schedule.add("• Revisión del plan: Diaria")
                schedule.add("• Evaluación nutricional: Semanal")
            }
            "Riesgo muy alto" -> {
                schedule.add("• Evaluación Braden: Cada 12 horas")
                schedule.add("• Inspección de piel: Continua")
                schedule.add("• Documentación: Cada 2 horas")
                schedule.add("• Revisión del plan: Cada 12 horas")
                schedule.add("• Consulta especializada: Inmediata")
            }
        }

        if (criticalIllness) {
            schedule.add("")
            schedule.add("🚨 MONITOREO INTENSIVO (UCI):")
            schedule.add("• Evaluación continua durante procedures")
            schedule.add("• Documentación cada hora")
            schedule.add("• Comunicación con equipo médico")
        }

        return schedule.joinToString("\n")
    }

    private fun analyzeRiskFactors(
        sensory: Int, moisture: Int, activity: Int,
        mobility: Int, nutrition: Int, friction: Int, age: Int?
    ): String {
        val analysis = mutableListOf<String>()
        val highRiskFactors = mutableListOf<String>()
        val moderateRiskFactors = mutableListOf<String>()

        // Analyze each factor
        if (sensory <= 2) highRiskFactors.add("Percepción sensorial muy limitada")
        else if (sensory == 3) moderateRiskFactors.add("Percepción sensorial ligeramente limitada")

        if (moisture <= 2) highRiskFactors.add("Exposición excesiva a humedad")
        else if (moisture == 3) moderateRiskFactors.add("Humedad ocasional")

        if (activity <= 2) highRiskFactors.add("Actividad muy limitada")
        else if (activity == 3) moderateRiskFactors.add("Actividad limitada")

        if (mobility <= 2) highRiskFactors.add("Movilidad muy limitada")
        else if (mobility == 3) moderateRiskFactors.add("Movilidad ligeramente limitada")

        if (nutrition <= 2) highRiskFactors.add("Estado nutricional comprometido")
        else if (nutrition == 3) moderateRiskFactors.add("Nutrición adecuada pero mejorable")

        if (friction <= 1) highRiskFactors.add("Problemas significativos de fricción")
        else if (friction == 2) moderateRiskFactors.add("Problemas potenciales de fricción")

        // Age factor
        age?.let {
            when {
                it >= 85 -> highRiskFactors.add("Edad muy avanzada (≥85 años)")
                it >= 75 -> moderateRiskFactors.add("Edad avanzada (75-84 años)")
                it >= 65 -> moderateRiskFactors.add("Adulto mayor (65-74 años)")
                else -> {}
            }
        }

        analysis.add("🔍 ANÁLISIS DE FACTORES DE RIESGO:")
        analysis.add("")

        if (highRiskFactors.isNotEmpty()) {
            analysis.add("🚨 FACTORES DE ALTO RIESGO:")
            highRiskFactors.forEach { analysis.add("• $it") }
            analysis.add("")
        }

        if (moderateRiskFactors.isNotEmpty()) {
            analysis.add("⚠️ FACTORES DE RIESGO MODERADO:")
            moderateRiskFactors.forEach { analysis.add("• $it") }
            analysis.add("")
        }

        if (highRiskFactors.isEmpty() && moderateRiskFactors.isEmpty()) {
            analysis.add("✅ Sin factores de riesgo significativos identificados")
            analysis.add("")
        }

        // Priority interventions based on risk factors
        analysis.add("🎯 INTERVENCIONES PRIORITARIAS:")
        if (sensory <= 2 || mobility <= 2) {
            analysis.add("• PRIORIDAD ALTA: Cambios posturales frecuentes")
        }
        if (moisture <= 2) {
            analysis.add("• PRIORIDAD ALTA: Control de humedad")
        }
        if (nutrition <= 2) {
            analysis.add("• PRIORIDAD ALTA: Evaluación nutricional")
        }
        if (friction <= 1) {
            analysis.add("• PRIORIDAD ALTA: Técnicas de movilización segura")
        }

        return analysis.joinToString("\n")
    }

    override fun getInterpretation(result: CalculationResult): String {
        val totalScore = result.resultValues["total_score"] ?: ""
        val riskLevel = result.resultValues["risk_level"] ?: ""
        val riskInterpretation = result.resultValues["risk_interpretation"] ?: ""

        return """
INTERPRETACIÓN CLÍNICA - ESCALA DE BRADEN

PUNTUACIÓN TOTAL: $totalScore/23 puntos
NIVEL DE RIESGO: $riskLevel
EVALUACIÓN: $riskInterpretation

RANGOS DE PUNTUACIÓN:
• 19-23 puntos: Sin riesgo
• 15-18 puntos: Riesgo leve  
• 13-14 puntos: Riesgo moderado
• 10-12 puntos: Riesgo alto
• ≤9 puntos: Riesgo muy alto

COMPONENTES EVALUADOS:
• Percepción sensorial (1-4 puntos)
• Exposición a humedad (1-4 puntos)  
• Actividad (1-4 puntos)
• Movilidad (1-4 puntos)
• Nutrición (1-4 puntos)
• Fricción y deslizamiento (1-3 puntos)

VALIDEZ CLÍNICA:
La Escala de Braden es el instrumento más utilizado mundialmente para predecir el riesgo de desarrollar úlceras por presión. Ha demostrado alta sensibilidad (83-100%) y especificidad (64-90%) en diversos estudios.

LIMITACIONES:
• No considera factores como medicamentos, comorbilidades específicas
• Requiere evaluación clínica complementaria
• Debe combinarse con juicio clínico profesional
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "Guía de Práctica Clínica para la Prevención y Tratamiento de Úlceras por Presión",
                source = "Secretaría de Salud México",
                url = "http://gpc.salud.gob.mx"
            ),
            Reference(
                title = "Escala de Braden para Evaluación de Riesgo",
                source = "Instituto Mexicano del Seguro Social (IMSS)",
                year = 2023
            ),
            Reference(
                title = "Prevención de Úlceras por Presión en Hospitalización",
                source = "Revista Mexicana de Enfermería",
                year = 2022
            ),
            Reference(
                title = "Braden Scale for Predicting Pressure Sore Risk",
                source = "Braden & Bergstrom, 1987 - Validated tool",
                year = 1987
            ),
            Reference(
                title = "Protocolo de Prevención de UPP",
                source = "Hospital General de México",
                year = 2023
            )
        )
    }
}