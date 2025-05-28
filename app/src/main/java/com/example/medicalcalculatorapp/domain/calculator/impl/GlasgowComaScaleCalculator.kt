package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult

class GlasgowComaScaleCalculator : Calculator {

    override val calculatorId = "glasgow_coma_scale"

    // Glasgow Coma Scale component definitions
    private val eyeResponseScores = mapOf(
        1 to "No abre los ojos",
        2 to "Abre los ojos al dolor",
        3 to "Abre los ojos a la voz",
        4 to "Abre los ojos espontáneamente"
    )

    private val verbalResponseScores = mapOf(
        1 to "No respuesta verbal",
        2 to "Sonidos incomprensibles",
        3 to "Palabras inapropiadas",
        4 to "Confuso",
        5 to "Orientado"
    )

    private val motorResponseScores = mapOf(
        1 to "No respuesta motora",
        2 to "Extensión anormal (descerebración)",
        3 to "Flexión anormal (decorticación)",
        4 to "Flexión de retirada",
        5 to "Localiza el dolor",
        6 to "Obedece órdenes"
    )

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate eye response
        val eyeStr = inputs["eye_response"]
        if (eyeStr.isNullOrBlank()) {
            errors.add("La respuesta ocular es obligatoria")
        } else {
            val eyeScore = eyeStr.toIntOrNull()
            if (eyeScore == null || eyeScore !in 1..4) {
                errors.add("La respuesta ocular debe estar entre 1-4")
            }
        }

        // Validate verbal response
        val verbalStr = inputs["verbal_response"]
        if (verbalStr.isNullOrBlank()) {
            errors.add("La respuesta verbal es obligatoria")
        } else {
            val verbalScore = verbalStr.toIntOrNull()
            if (verbalScore == null || verbalScore !in 1..5) {
                errors.add("La respuesta verbal debe estar entre 1-5")
            }
        }

        // Validate motor response
        val motorStr = inputs["motor_response"]
        if (motorStr.isNullOrBlank()) {
            errors.add("La respuesta motora es obligatoria")
        } else {
            val motorScore = motorStr.toIntOrNull()
            if (motorScore == null || motorScore !in 1..6) {
                errors.add("La respuesta motora debe estar entre 1-6")
            }
        }

        // Validate patient age if provided
        val ageStr = inputs["patient_age"]
        if (!ageStr.isNullOrBlank()) {
            val age = ageStr.toDoubleOrNull()
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
        val eyeResponse = inputs["eye_response"]!!.toInt()
        val verbalResponse = inputs["verbal_response"]!!.toInt()
        val motorResponse = inputs["motor_response"]!!.toInt()

        // Optional patient information
        val patientAge = inputs["patient_age"]?.toDoubleOrNull()
        val isIntubated = inputs["is_intubated"] == "true"
        val hasSeizures = inputs["has_seizures"] == "true"
        val traumaticBrainInjury = inputs["traumatic_brain_injury"] == "true"
        val drugsAlcohol = inputs["drugs_alcohol"] == "true"
        val clinicalContext = inputs["clinical_context"] ?: "Evaluación General"

        // Calculate total score
        val totalScore = eyeResponse + verbalResponse + motorResponse

        // Determine consciousness level
        val consciousnessLevel = determineConsciousnessLevel(totalScore)
        val neurologicalInterpretation = interpretNeurologicalStatus(
            totalScore, consciousnessLevel, eyeResponse, verbalResponse, motorResponse
        )

        // Generate detailed assessment
        val detailedAssessment = generateDetailedAssessment(
            eyeResponse, verbalResponse, motorResponse, isIntubated
        )

        // Generate clinical recommendations
        val clinicalRecommendations = generateClinicalRecommendations(
            totalScore, consciousnessLevel, traumaticBrainInjury, hasSeizures, clinicalContext
        )

        // Generate monitoring protocol
        val monitoringProtocol = generateMonitoringProtocol(
            totalScore, consciousnessLevel, traumaticBrainInjury, clinicalContext
        )

        // Generate prognostic indicators
        val prognosticIndicators = generatePrognosticIndicators(
            totalScore, eyeResponse, motorResponse, patientAge, traumaticBrainInjury
        )

        // Generate emergency alerts
        val emergencyAlerts = generateEmergencyAlerts(
            totalScore, eyeResponse, verbalResponse, motorResponse, isIntubated
        )

        // Format results
        val results = mapOf(
            "total_score" to totalScore.toString(),
            "consciousness_level" to consciousnessLevel,
            "neurological_interpretation" to neurologicalInterpretation,
            "detailed_assessment" to detailedAssessment,
            "clinical_recommendations" to clinicalRecommendations,
            "monitoring_protocol" to monitoringProtocol,
            "prognostic_indicators" to prognosticIndicators,
            "emergency_alerts" to emergencyAlerts
        )

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun determineConsciousnessLevel(score: Int): String {
        return when (score) {
            15 -> "Conciencia plena"
            in 13..14 -> "Confusión leve"
            in 9..12 -> "Estado moderado"
            in 3..8 -> "Estado grave / Coma"
            else -> "Puntuación inválida"
        }
    }

    private fun interpretNeurologicalStatus(
        totalScore: Int, level: String, eye: Int, verbal: Int, motor: Int
    ): String {
        return when (level) {
            "Conciencia plena" -> "✅ ESTADO NEUROLÓGICO NORMAL - Paciente completamente alerta y orientado. Funciones neurológicas preservadas."
            "Confusión leve" -> "🟡 ALTERACIÓN LEVE - Paciente confuso pero consciente. Requiere evaluación de causa subyacente."
            "Estado moderado" -> "🟠 ALTERACIÓN MODERADA - Deterioro significativo del estado de conciencia. Monitoreo neurológico intensivo."
            "Estado grave / Coma" -> "🚨 ESTADO CRÍTICO - Coma o estado vegetativo. Requiere manejo en UCI y medidas de soporte vital."
            else -> "Estado no clasificable"
        }
    }

    private fun generateDetailedAssessment(
        eye: Int, verbal: Int, motor: Int, isIntubated: Boolean
    ): String {
        val assessment = mutableListOf<String>()

        assessment.add("📊 EVALUACIÓN DETALLADA POR COMPONENTE:")
        assessment.add("")

        // Eye response assessment
        assessment.add("👁️ RESPUESTA OCULAR ($eye/4):")
        assessment.add("• ${eyeResponseScores[eye] ?: "Valor no válido"}")
        when (eye) {
            1 -> assessment.add("• ⚠️ ALERTA CRÍTICA - No apertura ocular")
            2 -> assessment.add("• ⚠️ RESPUESTA AL DOLOR solamente")
            3 -> assessment.add("• ⚠️ Requiere estímulo verbal")
            4 -> assessment.add("• ✅ Respuesta ocular normal")
        }
        assessment.add("")

        // Verbal response assessment
        assessment.add("🗣️ RESPUESTA VERBAL ($verbal/5):")
        if (isIntubated) {
            assessment.add("• PACIENTE INTUBADO - Evaluación verbal no aplicable")
            assessment.add("• ⚠️ Usar GCS modificado para pacientes intubados")
        } else {
            assessment.add("• ${verbalResponseScores[verbal] ?: "Valor no válido"}")
            when (verbal) {
                1 -> assessment.add("• 🚨 ALERTA CRÍTICA - Sin respuesta verbal")
                2 -> assessment.add("• ⚠️ Solo sonidos, sin palabras reconocibles")
                3 -> assessment.add("• ⚠️ Palabras sin coherencia")
                4 -> assessment.add("• ⚠️ Confusión pero respuesta verbal presente")
                5 -> assessment.add("• ✅ Respuesta verbal normal y orientada")
            }
        }
        assessment.add("")

        // Motor response assessment
        assessment.add("🤲 RESPUESTA MOTORA ($motor/6):")
        assessment.add("• ${motorResponseScores[motor] ?: "Valor no válido"}")
        when (motor) {
            1 -> assessment.add("• 🚨 ALERTA CRÍTICA - Sin respuesta motora")
            2 -> assessment.add("• 🚨 DESCEREBRACIÓN - Lesión del tronco encefálico")
            3 -> assessment.add("• 🚨 DECORTICACIÓN - Lesión cortical/subcortical")
            4 -> assessment.add("• ⚠️ Retirada al dolor - función motora básica")
            5 -> assessment.add("• ⚠️ Localiza dolor - función motora parcial")
            6 -> assessment.add("• ✅ Obedece órdenes - función motora normal")
        }

        return assessment.joinToString("\n")
    }

    private fun generateClinicalRecommendations(
        totalScore: Int, level: String, traumaticBrainInjury: Boolean,
        hasSeizures: Boolean, clinicalContext: String
    ): String {
        val recommendations = mutableListOf<String>()

        recommendations.add("🏥 RECOMENDACIONES CLÍNICAS ESPECÍFICAS:")
        recommendations.add("")

        when (level) {
            "Conciencia plena" -> {
                recommendations.add("✅ MANEJO ESTÁNDAR:")
                recommendations.add("• Observación clínica de rutina")
                recommendations.add("• Evaluación neurológica cada 4 horas")
                recommendations.add("• Investigar causa de consulta neurológica")
                recommendations.add("• Alta médica si no hay otras complicaciones")
            }
            "Confusión leve" -> {
                recommendations.add("🟡 EVALUACIÓN DIRIGIDA:")
                recommendations.add("• Evaluación neurológica cada 2 horas")
                recommendations.add("• Investigar causas metabólicas (glucosa, electrolitos)")
                recommendations.add("• Considerar TAC de cráneo simple")
                recommendations.add("• Evaluar medicamentos y tóxicos")
                recommendations.add("• Monitoreo de signos vitales")
            }
            "Estado moderado" -> {
                recommendations.add("🟠 MANEJO INTENSIVO:")
                recommendations.add("• UCI o área de cuidados intensivos")
                recommendations.add("• TAC de cráneo urgente")
                recommendations.add("• Evaluación neurológica cada hora")
                recommendations.add("• Protección de vía aérea")
                recommendations.add("• Prevención de aspiración")
                recommendations.add("• Consulta neuroquirúrgica")
            }
            "Estado grave / Coma" -> {
                recommendations.add("🚨 MEDIDAS DE EMERGENCIA:")
                recommendations.add("• UCI inmediatamente")
                recommendations.add("• Intubación orotraqueal si indicado")
                recommendations.add("• TAC de cráneo STAT")
                recommendations.add("• Monitoreo de presión intracraneal")
                recommendations.add("• Consulta neuroquirúrgica urgente")
                recommendations.add("• Protocolo de coma")
                recommendations.add("• Considerar traslado a centro especializado")
            }
        }

        // Specific conditions
        if (traumaticBrainInjury) {
            recommendations.add("")
            recommendations.add("🧠 TRAUMATISMO CRANEOENCEFÁLICO:")
            recommendations.add("• Inmovilización cervical hasta descartar lesión")
            recommendations.add("• Protocolo de trauma craneal")
            recommendations.add("• Prevenir hipertensión intracraneal")
            recommendations.add("• Evitar hipotensión e hipoxia")
        }

        if (hasSeizures) {
            recommendations.add("")
            recommendations.add("⚡ ACTIVIDAD CONVULSIVA:")
            recommendations.add("• Protocolo de status epiléptico")
            recommendations.add("• Anticonvulsivantes según protocolo")
            recommendations.add("• EEG si disponible")
            recommendations.add("• Monitoreo continuo")
        }

        // Context-specific recommendations
        when (clinicalContext) {
            "Urgencias" -> {
                recommendations.add("")
                recommendations.add("🚑 PROTOCOLO DE URGENCIAS:")
                recommendations.add("• Evaluación ABCDE completa")
                recommendations.add("• Estabilización hemodinámica")
                recommendations.add("• Descartar otras lesiones")
            }
            "Postoperatorio" -> {
                recommendations.add("")
                recommendations.add("🔬 CUIDADOS POSTOPERATORIOS:")
                recommendations.add("• Evaluar complicaciones quirúrgicas")
                recommendations.add("• Monitoreo de sangrado intracraneal")
                recommendations.add("• Manejo del dolor postoperatorio")
            }
            "UCI" -> {
                recommendations.add("")
                recommendations.add("🏥 MANEJO EN UCI:")
                recommendations.add("• Sedoanalgesia controlada")
                recommendations.add("• Prevención de úlceras por estrés")
                recommendations.add("• Fisioterapia respiratoria")
            }
        }

        return recommendations.joinToString("\n")
    }

    private fun generateMonitoringProtocol(
        totalScore: Int, level: String, traumaticBrainInjury: Boolean, clinicalContext: String
    ): String {
        val protocol = mutableListOf<String>()

        protocol.add("📅 PROTOCOLO DE MONITOREO NEUROLÓGICO:")
        protocol.add("")

        when (level) {
            "Conciencia plena" -> {
                protocol.add("✅ MONITOREO BÁSICO:")
                protocol.add("• Glasgow cada 4 horas")
                protocol.add("• Signos vitales cada 4 horas")
                protocol.add("• Evaluación pupilar cada turno")
                protocol.add("• Documentación en expediente")
            }
            "Confusión leve" -> {
                protocol.add("🟡 MONITOREO ESTRECHO:")
                protocol.add("• Glasgow cada 2 horas")
                protocol.add("• Signos vitales cada 2 horas")
                protocol.add("• Evaluación pupilar cada 2 horas")
                protocol.add("• Función motora focal")
                protocol.add("• Estado de agitación/sedación")
            }
            "Estado moderado" -> {
                protocol.add("🟠 MONITOREO INTENSIVO:")
                protocol.add("• Glasgow cada hora")
                protocol.add("• Signos vitales cada 30 minutos")
                protocol.add("• Evaluación pupilar cada hora")
                protocol.add("• Presión arterial media >80 mmHg")
                protocol.add("• Saturación O2 >95%")
                protocol.add("• Diuresis cada hora")
            }
            "Estado grave / Coma" -> {
                protocol.add("🚨 MONITOREO CRÍTICO:")
                protocol.add("• Glasgow cada 15-30 minutos")
                protocol.add("• Monitoreo hemodinámico continuo")
                protocol.add("• Presión intracraneal si disponible")
                protocol.add("• Gasometría arterial cada 4-6 horas")
                protocol.add("• Balance hídrico estricto")
                protocol.add("• Electrolitos séricos cada 12 horas")
                protocol.add("• Temperatura corporal continua")
            }
        }

        if (traumaticBrainInjury) {
            protocol.add("")
            protocol.add("🧠 MONITOREO ESPECIALIZADO TCE:")
            protocol.add("• Evaluación de heridas externas")
            protocol.add("• Signos de aumento de PIC")
            protocol.add("• Líquido cefalorraquídeo (otorrea/rinorrea)")
            protocol.add("• TAC de control según evolución")
        }

        // Alert parameters
        protocol.add("")
        protocol.add("⚠️ PARÁMETROS DE ALERTA:")
        protocol.add("• Disminución Glasgow ≥2 puntos")
        protocol.add("• Cambios pupilares (anisocoria >1mm)")
        protocol.add("• Deterioro motor unilateral")
        protocol.add("• Signos de herniación cerebral")
        protocol.add("• Vómitos en proyectil")
        protocol.add("• Bradicardia + hipertensión (Cushing)")

        return protocol.joinToString("\n")
    }

    private fun generatePrognosticIndicators(
        totalScore: Int, eyeResponse: Int, motorResponse: Int,
        age: Double?, traumaticBrainInjury: Boolean
    ): String {
        val indicators = mutableListOf<String>()

        indicators.add("📈 INDICADORES PRONÓSTICOS:")
        indicators.add("")

        // Overall prognosis based on score
        when (totalScore) {
            15 -> {
                indicators.add("✅ PRONÓSTICO EXCELENTE")
                indicators.add("• Recuperación completa esperada")
                indicators.add("• Riesgo mínimo de complicaciones")
            }
            in 13..14 -> {
                indicators.add("🟡 PRONÓSTICO BUENO")
                indicators.add("• Recuperación probable con manejo apropiado")
                indicators.add("• Monitoreo para prevenir deterioro")
            }
            in 9..12 -> {
                indicators.add("🟠 PRONÓSTICO RESERVADO")
                indicators.add("• Recuperación variable según causa")
                indicators.add("• Riesgo moderado de complicaciones")
                indicators.add("• Requiere manejo especializado")
            }
            in 6..8 -> {
                indicators.add("🔴 PRONÓSTICO GRAVE")
                indicators.add("• Alta morbimortalidad")
                indicators.add("• Posibles secuelas neurológicas")
                indicators.add("• Requiere cuidados intensivos")
            }
            in 3..5 -> {
                indicators.add("🚨 PRONÓSTICO MUY GRAVE")
                indicators.add("• Mortalidad elevada (>50%)")
                indicators.add("• Alto riesgo de secuelas permanentes")
                indicators.add("• Considerar medidas de soporte vital")
            }
        }

        // Motor response prognostic value
        indicators.add("")
        indicators.add("🤲 VALOR PRONÓSTICO MOTOR:")
        when (motorResponse) {
            6 -> indicators.add("• ✅ Mejor pronóstico - función cortical preservada")
            5 -> indicators.add("• 🟡 Buen pronóstico - localización del dolor")
            4 -> indicators.add("• 🟠 Pronóstico moderado - respuesta de retirada")
            3 -> indicators.add("• 🔴 Mal pronóstico - decorticación")
            2 -> indicators.add("• 🚨 Muy mal pronóstico - descerebración")
            1 -> indicators.add("• 🚨 Pronóstico crítico - sin respuesta motora")
        }

        // Age considerations
        age?.let {
            indicators.add("")
            indicators.add("👤 FACTORES DE EDAD:")
            when {
                it < 40 -> indicators.add("• ✅ Edad joven - mejor capacidad de recuperación")
                it < 65 -> indicators.add("• 🟡 Edad adulta - pronóstico variable")
                it >= 65 -> indicators.add("• 🟠 Edad avanzada - recuperación más lenta")
                it >= 80 -> indicators.add("• 🔴 Edad muy avanzada - pronóstico reservado")
                else -> {}
            }
        }

        // Traumatic brain injury specific
        if (traumaticBrainInjury) {
            indicators.add("")
            indicators.add("🧠 PRONÓSTICO EN TCE:")
            indicators.add("• Depende de mecanismo de lesión")
            indicators.add("• Lesiones difusas vs focales")
            indicators.add("• Tiempo hasta atención médica")
            indicators.add("• Presencia de lesiones secundarias")
        }

        return indicators.joinToString("\n")
    }

    private fun generateEmergencyAlerts(
        totalScore: Int, eye: Int, verbal: Int, motor: Int, isIntubated: Boolean
    ): String {
        val alerts = mutableListOf<String>()

        // Critical score alerts
        if (totalScore <= 8) {
            alerts.add("🚨 ALERTA CRÍTICA: Glasgow ≤8")
            alerts.add("• COMA - Requiere manejo inmediato en UCI")
            alerts.add("• Considerar intubación orotraqueal")
            alerts.add("• Consulta neuroquirúrgica URGENTE")
            alerts.add("")
        }

        if (totalScore <= 5) {
            alerts.add("🚨 ALERTA MÁXIMA: Glasgow ≤5")
            alerts.add("• ESTADO VEGETATIVO/COMA PROFUNDO")
            alerts.add("• Medidas de soporte vital completo")
            alerts.add("• Evaluación pronóstica familiar")
            alerts.add("")
        }

        // Component-specific alerts
        if (eye == 1) {
            alerts.add("👁️ ALERTA OCULAR: Sin apertura de ojos")
            alerts.add("• Posible lesión del tronco cerebral")
            alerts.add("• Evaluar reflejos pupilares inmediatamente")
            alerts.add("")
        }

        if (verbal == 1 && !isIntubated) {
            alerts.add("🗣️ ALERTA VERBAL: Sin respuesta verbal")
            alerts.add("• Descartar afasia vs disminución del nivel de conciencia")
            alerts.add("• Evaluar comprensión de órdenes")
            alerts.add("")
        }

        if (motor <= 2) {
            alerts.add("🤲 ALERTA MOTORA CRÍTICA:")
            if (motor == 1) {
                alerts.add("• Sin respuesta motora - lesión grave del SNC")
            } else {
                alerts.add("• Postura de descerebración - lesión del tronco")
            }
            alerts.add("• TAC de cráneo inmediato")
            alerts.add("• Manejo de presión intracraneal")
            alerts.add("")
        }

        if (motor == 3) {
            alerts.add("🤲 ALERTA MOTORA: Postura de decorticación")
            alerts.add("• Lesión cortical/subcortical")
            alerts.add("• Monitoreo neurológico estrecho")
            alerts.add("")
        }

        // General alerts
        if (totalScore >= 13) {
            alerts.add("✅ SIN ALERTAS CRÍTICAS")
            alerts.add("• Continuar monitoreo de rutina")
            alerts.add("• Investigar causa de alteración si presente")
        }

        if (alerts.isEmpty()) {
            alerts.add("📊 Estado evaluado - Ver recomendaciones específicas")
        }

        return alerts.joinToString("\n")
    }

    override fun getInterpretation(result: CalculationResult): String {
        val totalScore = result.resultValues["total_score"] ?: ""
        val level = result.resultValues["consciousness_level"] ?: ""
        val eyeScore = result.inputValues["eye_response"] ?: ""
        val verbalScore = result.inputValues["verbal_response"] ?: ""
        val motorScore = result.inputValues["motor_response"] ?: ""

        return """
INTERPRETACIÓN CLÍNICA - ESCALA DE COMA DE GLASGOW

PUNTUACIÓN TOTAL: $totalScore/15 puntos
NIVEL DE CONSCIENCIA: $level

COMPONENTES EVALUADOS:
• Respuesta Ocular: $eyeScore/4 puntos
• Respuesta Verbal: $verbalScore/5 puntos  
• Respuesta Motora: $motorScore/6 puntos

RANGOS DE INTERPRETACIÓN:
• 15 puntos: Conciencia plena
• 13-14 puntos: Confusión leve
• 9-12 puntos: Estado moderado
• 3-8 puntos: Estado grave / Coma

VALIDEZ CLÍNICA:
La Escala de Glasgow es el estándar internacional para evaluar el nivel de consciencia y predecir pronóstico neurológico. Desarrollada en 1974, tiene alta confiabilidad inter-observador cuando se aplica correctamente.

CONSIDERACIONES ESPECIALES:
• Pacientes intubados: Usar GCS modificado
• Edema facial: Puede limitar evaluación ocular
• Sedación/analgesia: Puede alterar las respuestas
• Lesiones locales: Evaluar componentes no afectados

APLICACIÓN CLÍNICA:
• Evaluación inicial y seriada en trauma
• Monitoreo neurológico en UCI
• Criterio para intubación (GCS ≤8)
• Predictor pronóstico en coma
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "Manual de Atención Neurológica de Urgencia",
                source = "Instituto Nacional de Neurología y Neurocirugía (INNN)",
                year = 2023
            ),
            Reference(
                title = "Escala de Coma de Glasgow en Urgencias",
                source = "Sociedad Mexicana de Medicina de Emergencia",
                year = 2022
            ),
            Reference(
                title = "Guías de Manejo del Trauma Craneoencefálico",
                source = "Instituto Mexicano del Seguro Social (IMSS)",
                year = 2023
            ),
            Reference(
                title = "Assessment of coma and impaired consciousness",
                source = "Teasdale & Jennett, The Lancet 1974",
                year = 1974
            ),
            Reference(
                title = "Neurological Assessment in Critical Care",
                source = "American Association of Neuroscience Nurses",
                year = 2022
            )
        )
    }
}