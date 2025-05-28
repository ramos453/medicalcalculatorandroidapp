package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult

class ApgarScoreCalculator : Calculator {

    override val calculatorId = "apgar_score"

    // APGAR scoring criteria definitions
    private val appearanceScores = mapOf(
        0 to "Cianosis generalizada o palidez",
        1 to "Extremidades cianóticas, cuerpo rosado",
        2 to "Rosado completamente"
    )

    private val pulseScores = mapOf(
        0 to "Ausente",
        1 to "Menos de 100 lpm",
        2 to "Más de 100 lpm"
    )

    private val grimaceScores = mapOf(
        0 to "Sin respuesta",
        1 to "Mueca o débil",
        2 to "Llanto vigoroso"
    )

    private val activityScores = mapOf(
        0 to "Flácido",
        1 to "Flexión mínima de extremidades",
        2 to "Movimientos activos"
    )

    private val respiratoryScores = mapOf(
        0 to "Ausente",
        1 to "Débil o irregular",
        2 to "Llanto fuerte"
    )

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate all 5 APGAR criteria
        val requiredFields = listOf(
            "appearance_color" to "Apariencia (color)",
            "pulse_heart_rate" to "Pulso",
            "grimace_reflex" to "Gesticulación",
            "activity_muscle_tone" to "Actividad",
            "respiratory_effort" to "Respiración"
        )

        for ((field, displayName) in requiredFields) {
            val valueStr = inputs[field]
            if (valueStr.isNullOrBlank()) {
                errors.add("$displayName es obligatorio")
            } else {
                val value = extractScoreFromOption(valueStr)
                if (value == null || value !in 0..2) {
                    errors.add("$displayName debe tener una puntuación válida (0-2)")
                }
            }
        }

        // Validate evaluation time
        val evaluationTime = inputs["evaluation_time"]
        if (evaluationTime.isNullOrBlank()) {
            errors.add("El tiempo de evaluación es obligatorio")
        }

        // Validate optional fields if provided
        val gestationalAgeStr = inputs["gestational_age"]
        if (!gestationalAgeStr.isNullOrBlank()) {
            val gestationalAge = gestationalAgeStr.toDoubleOrNull()
            if (gestationalAge == null || gestationalAge < 20 || gestationalAge > 44) {
                errors.add("La edad gestacional debe estar entre 20-44 semanas")
            }
        }

        val birthWeightStr = inputs["birth_weight"]
        if (!birthWeightStr.isNullOrBlank()) {
            val birthWeight = birthWeightStr.toDoubleOrNull()
            if (birthWeight == null || birthWeight < 500 || birthWeight > 6000) {
                errors.add("El peso al nacer debe estar entre 500-6000 gramos")
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

        // Parse APGAR scores
        val appearance = extractScoreFromOption(inputs["appearance_color"]!!)!!
        val pulse = extractScoreFromOption(inputs["pulse_heart_rate"]!!)!!
        val grimace = extractScoreFromOption(inputs["grimace_reflex"]!!)!!
        val activity = extractScoreFromOption(inputs["activity_muscle_tone"]!!)!!
        val respiratory = extractScoreFromOption(inputs["respiratory_effort"]!!)!!

        // Calculate total score
        val totalScore = appearance + pulse + grimace + activity + respiratory

        // Parse additional information
        val evaluationTime = inputs["evaluation_time"] ?: "1 minuto"
        val gestationalAge = inputs["gestational_age"]?.toDoubleOrNull()
        val birthWeight = inputs["birth_weight"]?.toDoubleOrNull()
        val deliveryType = inputs["delivery_type"] ?: "Vaginal espontáneo"
        val maternalComplications = inputs["maternal_complications"] == "true"
        val multipleBirth = inputs["multiple_birth"] == "true"
        val resuscitationNeeded = inputs["resuscitation_needed"] == "true"

        // Determine clinical status
        val clinicalStatus = determineClinicalStatus(totalScore)
        val clinicalInterpretation = interpretClinicalStatus(
            totalScore, clinicalStatus, evaluationTime, gestationalAge, birthWeight
        )

        // Generate detailed assessment
        val detailedAssessment = generateDetailedAssessment(
            appearance, pulse, grimace, activity, respiratory, evaluationTime
        )

        // Generate immediate actions
        val immediateActions = generateImmediateActions(
            totalScore, clinicalStatus, evaluationTime, resuscitationNeeded
        )

        // Generate monitoring protocol
        val monitoringProtocol = generateMonitoringProtocol(
            totalScore, evaluationTime, gestationalAge, birthWeight, deliveryType
        )

        // Generate prognostic indicators
        val prognosticIndicators = generatePrognosticIndicators(
            totalScore, gestationalAge, birthWeight, maternalComplications, multipleBirth
        )

        // Generate follow-up recommendations
        val followUpRecommendations = generateFollowUpRecommendations(
            totalScore, gestationalAge, birthWeight, deliveryType, resuscitationNeeded
        )

        // Format results
        val results = mapOf(
            "total_score" to totalScore.toString(),
            "clinical_status" to clinicalStatus,
            "clinical_interpretation" to clinicalInterpretation,
            "detailed_assessment" to detailedAssessment,
            "immediate_actions" to immediateActions,
            "monitoring_protocol" to monitoringProtocol,
            "prognostic_indicators" to prognosticIndicators,
            "follow_up_recommendations" to followUpRecommendations
        )

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun extractScoreFromOption(option: String): Int? {
        return option.split(" - ").firstOrNull()?.toIntOrNull()
    }

    private fun determineClinicalStatus(score: Int): String {
        return when (score) {
            in 7..10 -> "Buen estado"
            in 4..6 -> "Asistencia moderada"
            in 0..3 -> "Asistencia inmediata"
            else -> "Puntuación inválida"
        }
    }

    private fun interpretClinicalStatus(
        score: Int, status: String, evaluationTime: String,
        gestationalAge: Double?, birthWeight: Double?
    ): String {
        val baseInterpretation = when (status) {
            "Buen estado" -> "✅ ESTADO ÓPTIMO - Recién nacido con excelente adaptación extrauterina. Signos vitales estables y respuesta neurológica adecuada."
            "Asistencia moderada" -> "🟡 ASISTENCIA REQUERIDA - Recién nacido con adaptación comprometida. Requiere intervenciones de soporte y monitoreo estrecho."
            "Asistencia inmediata" -> "🚨 EMERGENCIA NEONATAL - Recién nacido en estado crítico. Requiere reanimación inmediata y cuidados intensivos."
            else -> "Estado no clasificable"
        }

        val timeContext = when (evaluationTime) {
            "1 minuto" -> " Evaluación inicial al primer minuto de vida."
            "5 minutos" -> " Evaluación a los 5 minutos - indicador pronóstico importante."
            "10 minutos" -> " Evaluación tardía a los 10 minutos - seguimiento post-reanimación."
            else -> ""
        }

        // Add gestational age context
        val gestationalContext = gestationalAge?.let { age ->
            when {
                age < 28 -> " (Extremadamente prematuro - ajustar expectativas)"
                age < 32 -> " (Muy prematuro - considerar inmadurez orgánica)"
                age < 37 -> " (Prematuro - vigilar adaptación respiratoria)"
                age > 42 -> " (Postérmino - evaluar complicaciones asociadas)"
                else -> " (A término - expectativas normales)"
            }
        } ?: ""

        return baseInterpretation + timeContext + gestationalContext
    }

    private fun generateDetailedAssessment(
        appearance: Int, pulse: Int, grimace: Int,
        activity: Int, respiratory: Int, evaluationTime: String
    ): String {
        val assessment = mutableListOf<String>()

        assessment.add("📊 EVALUACIÓN DETALLADA APGAR ($evaluationTime):")
        assessment.add("")

        // Appearance assessment
        assessment.add("🎨 APARIENCIA - COLOR ($appearance/2):")
        assessment.add("• ${appearanceScores[appearance] ?: "Valor no válido"}")
        when (appearance) {
            0 -> assessment.add("• 🚨 CIANOSIS CENTRAL - Hipoxia severa, requiere O2 inmediato")
            1 -> assessment.add("• ⚠️ CIANOSIS PERIFÉRICA - Adaptación circulatoria en proceso")
            2 -> assessment.add("• ✅ COLORACIÓN NORMAL - Buena oxigenación tisular")
        }
        assessment.add("")

        // Pulse assessment
        assessment.add("💓 PULSO - FRECUENCIA CARDÍACA ($pulse/2):")
        assessment.add("• ${pulseScores[pulse] ?: "Valor no válido"}")
        when (pulse) {
            0 -> assessment.add("• 🚨 ASISTOLIA - Reanimación cardiopulmonar inmediata")
            1 -> assessment.add("• ⚠️ BRADICARDIA - Estimulación y oxigenación urgente")
            2 -> assessment.add("• ✅ FRECUENCIA ADECUADA - Función cardíaca estable")
        }
        assessment.add("")

        // Grimace assessment
        assessment.add("😤 GESTICULACIÓN - IRRITABILIDAD REFLEJA ($grimace/2):")
        assessment.add("• ${grimaceScores[grimace] ?: "Valor no válido"}")
        when (grimace) {
            0 -> assessment.add("• 🚨 SIN REFLEJOS - Depresión neurológica severa")
            1 -> assessment.add("• ⚠️ RESPUESTA DÉBIL - Depresión neurológica leve-moderada")
            2 -> assessment.add("• ✅ RESPUESTA VIGOROSA - Función neurológica adecuada")
        }
        assessment.add("")

        // Activity assessment
        assessment.add("💪 ACTIVIDAD - TONO MUSCULAR ($activity/2):")
        assessment.add("• ${activityScores[activity] ?: "Valor no válido"}")
        when (activity) {
            0 -> assessment.add("• 🚨 HIPOTONÍA SEVERA - Depresión del sistema nervioso central")
            1 -> assessment.add("• ⚠️ HIPOTONÍA LEVE - Adaptación neurológica en proceso")
            2 -> assessment.add("• ✅ TONO NORMAL - Desarrollo neuromuscular adecuado")
        }
        assessment.add("")

        // Respiratory assessment
        assessment.add("🫁 RESPIRACIÓN - ESFUERZO RESPIRATORIO ($respiratory/2):")
        assessment.add("• ${respiratoryScores[respiratory] ?: "Valor no válido"}")
        when (respiratory) {
            0 -> assessment.add("• 🚨 APNEA - Ventilación asistida inmediata")
            1 -> assessment.add("• ⚠️ RESPIRACIÓN IRREGULAR - Estimulación y oxígeno suplementario")
            2 -> assessment.add("• ✅ RESPIRACIÓN VIGOROSA - Función pulmonar establecida")
        }

        return assessment.joinToString("\n")
    }

    private fun generateImmediateActions(
        score: Int, status: String, evaluationTime: String, resuscitationNeeded: Boolean
    ): String {
        val actions = mutableListOf<String>()

        actions.add("🚨 ACCIONES INMEDIATAS REQUERIDAS:")
        actions.add("")

        when (status) {
            "Buen estado" -> {
                actions.add("✅ CUIDADOS DE RUTINA:")
                actions.add("• Secar y abrigar al recién nacido")
                actions.add("• Contacto piel a piel con la madre")
                actions.add("• Pinzamiento tardío del cordón (1-3 minutos)")
                actions.add("• Iniciar lactancia materna en la primera hora")
                actions.add("• Aplicar vitamina K intramuscular")
                actions.add("• Profilaxis ocular (eritromicina)")
                actions.add("• Identificación y registro del neonato")
            }

            "Asistencia moderada" -> {
                actions.add("🟡 INTERVENCIONES DE SOPORTE:")
                actions.add("• Secar vigorosamente y proporcionar calor")
                actions.add("• Aspiración suave de secreciones si es necesario")
                actions.add("• Estimulación táctil suave")
                actions.add("• Oxigenoterapia a flujo libre si cianosis persiste")
                actions.add("• Monitoreo continuo de signos vitales")
                actions.add("• Reevaluar APGAR a los 5 minutos")
                actions.add("• Considerar CPAP nasal si dificultad respiratoria")
                actions.add("• Diferir procedimientos no urgentes")
            }

            "Asistencia inmediata" -> {
                actions.add("🚨 REANIMACIÓN NEONATAL - PROTOCOLO ABC:")
                actions.add("• A - AIRWAY: Posición, aspiración, permeabilidad")
                actions.add("• B - BREATHING: Ventilación con presión positiva")
                actions.add("• C - CIRCULATION: Compresiones torácicas si FC <60")
                actions.add("• Intubación endotraqueal si ventilación inefectiva")
                actions.add("• Acceso vascular umbilical de emergencia")
                actions.add("• Epinefrina IV/ET si bradicardia persistente")
                actions.add("• Expansión de volumen si shock hipovolémico")
                actions.add("• Traslado inmediato a UCIN")
                actions.add("• Documentación exhaustiva de la reanimación")
            }
        }

        // Time-specific actions
        if (evaluationTime == "5 minutos" && score < 7) {
            actions.add("")
            actions.add("⏰ CONSIDERACIONES A LOS 5 MINUTOS:")
            actions.add("• Continuar reanimación si APGAR <7")
            actions.add("• Evaluar efectividad de intervenciones")
            actions.add("• Considerar causas reversibles")
            actions.add("• Documentar respuesta a reanimación")
            actions.add("• Planificar cuidados intensivos neonatales")
        }

        if (resuscitationNeeded) {
            actions.add("")
            actions.add("📋 PROTOCOLO POST-REANIMACIÓN:")
            actions.add("• Monitoreo hemodinámico continuo")
            actions.add("• Gasometría arterial")
            actions.add("• Glucemia y electrolitos")
            actions.add("• Radiografía de tórax")
            actions.add("• Evaluación neurológica seriada")
        }

        return actions.joinToString("\n")
    }

    private fun generateMonitoringProtocol(
        score: Int, evaluationTime: String, gestationalAge: Double?,
        birthWeight: Double?, deliveryType: String
    ): String {
        val protocol = mutableListOf<String>()

        protocol.add("📊 PROTOCOLO DE MONITOREO NEONATAL:")
        protocol.add("")

        when (score) {
            in 7..10 -> {
                protocol.add("✅ MONITOREO ESTÁNDAR:")
                protocol.add("• Signos vitales cada 4 horas las primeras 24h")
                protocol.add("• Temperatura, respiración, coloración")
                protocol.add("• Alimentación y eliminación")
                protocol.add("• Peso diario")
                protocol.add("• Evaluación neurológica básica")
            }

            in 4..6 -> {
                protocol.add("🟡 MONITOREO INTENSIVO:")
                protocol.add("• Signos vitales cada 2 horas")
                protocol.add("• Monitoreo cardiorrespiratorio continuo")
                protocol.add("• Saturación de oxígeno continua")
                protocol.add("• Glucemia cada 6 horas")
                protocol.add("• Balance hídrico estricto")
                protocol.add("• Evaluación neurológica cada 8 horas")
                protocol.add("• APGAR de seguimiento a los 10 minutos")
            }

            in 0..3 -> {
                protocol.add("🚨 MONITOREO CRÍTICO:")
                protocol.add("• Monitoreo hemodinámico invasivo")
                protocol.add("• Gasometrías arteriales seriadas")
                protocol.add("• Presión arterial continua")
                protocol.add("• Diuresis horaria")
                protocol.add("• Electrolitos cada 6 horas")
                protocol.add("• Evaluación neurológica continua")
                protocol.add("• Ecocardiograma funcional")
                protocol.add("• EEG si convulsiones o encefalopatía")
            }
        }

        // Gestational age specific monitoring
        gestationalAge?.let { age ->
            protocol.add("")
            when {
                age < 32 -> {
                    protocol.add("👶 MONITOREO GRAN PREMATURO:")
                    protocol.add("• Apneas y bradicardias")
                    protocol.add("• Síndrome de dificultad respiratoria")
                    protocol.add("• Hemorragia intraventricular")
                    protocol.add("• Enterocolitis necrotizante")
                    protocol.add("• Retinopatía del prematuro")
                }

                age < 37 -> {
                    protocol.add("👶 MONITOREO PREMATURO:")
                    protocol.add("• Dificultad respiratoria transitoria")
                    protocol.add("• Hipoglucemia")
                    protocol.add("• Ictericia patológica")
                    protocol.add("• Problemas de termorregulación")
                }

                age > 42 -> {
                    protocol.add("👶 MONITOREO POSTÉRMINO:")
                    protocol.add("• Síndrome de aspiración meconial")
                    protocol.add("• Hipoglucemia")
                    protocol.add("• Policitemia")
                    protocol.add("• Insuficiencia placentaria")
                }

                else -> {}
            }
        }

        // Birth weight specific monitoring
        birthWeight?.let { weight ->
            protocol.add("")
            when {
                weight < 1500 -> {
                    protocol.add("⚖️ MONITOREO MUY BAJO PESO:")
                    protocol.add("• Hipotermia")
                    protocol.add("• Hipoglucemia severa")
                    protocol.add("• Síndrome de dificultad respiratoria")
                    protocol.add("• Conducto arterioso persistente")
                }

                weight < 2500 -> {
                    protocol.add("⚖️ MONITOREO BAJO PESO:")
                    protocol.add("• Hipoglucemia")
                    protocol.add("• Dificultades de alimentación")
                    protocol.add("• Pérdida de calor")
                }

                weight > 4000 -> {
                    protocol.add("⚖️ MONITOREO MACROSÓMICO:")
                    protocol.add("• Hipoglucemia")
                    protocol.add("• Traumatismo del parto")
                    protocol.add("• Policitemia")
                }

                else -> {}
            }
        }

        return protocol.joinToString("\n")
    }

    private fun generatePrognosticIndicators(
        score: Int, gestationalAge: Double?, birthWeight: Double?,
        maternalComplications: Boolean, multipleBirth: Boolean
    ): String {
        val indicators = mutableListOf<String>()

        indicators.add("📈 INDICADORES PRONÓSTICOS:")
        indicators.add("")

        // APGAR score prognosis
        when (score) {
            in 7..10 -> {
                indicators.add("✅ PRONÓSTICO EXCELENTE:")
                indicators.add("• Adaptación extrauterina óptima")
                indicators.add("• Bajo riesgo de complicaciones")
                indicators.add("• Desarrollo neurológico normal esperado")
                indicators.add("• Mortalidad neonatal mínima (<1%)")
            }

            in 4..6 -> {
                indicators.add("🟡 PRONÓSTICO MODERADO:")
                indicators.add("• Requiere vigilancia estrecha")
                indicators.add("• Riesgo moderado de complicaciones")
                indicators.add("• Posibles secuelas neurológicas leves")
                indicators.add("• Mortalidad neonatal baja (2-5%)")
            }

            in 0..3 -> {
                indicators.add("🔴 PRONÓSTICO RESERVADO:")
                indicators.add("• Alto riesgo de morbimortalidad")
                indicators.add("• Posibles secuelas neurológicas graves")
                indicators.add("• Requiere cuidados intensivos prolongados")
                indicators.add("• Mortalidad neonatal significativa (15-30%)")
            }
        }

        // Gestational age impact
        gestationalAge?.let { age ->
            indicators.add("")
            indicators.add("📅 IMPACTO DE EDAD GESTACIONAL:")
            when {
                age < 28 -> indicators.add("• Extremadamente prematuro - Supervivencia 50-80%")
                age < 32 -> indicators.add("• Muy prematuro - Supervivencia 85-95%")
                age < 37 -> indicators.add("• Prematuro - Supervivencia >95%")
                age > 42 -> indicators.add("• Postérmino - Riesgo de complicaciones aumentado")
                else -> indicators.add("• A término - Pronóstico óptimo esperado")
            }
        }

        // Birth weight impact
        birthWeight?.let { weight ->
            indicators.add("")
            indicators.add("⚖️ IMPACTO DEL PESO AL NACER:")
            when {
                weight < 1000 -> indicators.add("• Peso extremadamente bajo - Alto riesgo")
                weight < 1500 -> indicators.add("• Muy bajo peso - Riesgo moderado-alto")
                weight < 2500 -> indicators.add("• Bajo peso - Vigilancia aumentada")
                weight > 4500 -> indicators.add("• Macrosomía - Riesgo de complicaciones metabólicas")
                else -> indicators.add("• Peso adecuado - Pronóstico favorable")
            }
        }

        // Additional risk factors
        if (maternalComplications) {
            indicators.add("")
            indicators.add("⚠️ COMPLICACIONES MATERNAS:")
            indicators.add("• Aumentan riesgo de adaptación deficiente")
            indicators.add("• Requieren monitoreo más intensivo")
            indicators.add("• Posible necesidad de intervenciones adicionales")
        }

        if (multipleBirth) {
            indicators.add("")
            indicators.add("👥 EMBARAZO MÚLTIPLE:")
            indicators.add("• Mayor riesgo de prematurez")
            indicators.add("• Posible síndrome transfusor-transfundido")
            indicators.add("• Competencia intrauterina por nutrientes")
        }

        return indicators.joinToString("\n")
    }

    private fun generateFollowUpRecommendations(
        score: Int, gestationalAge: Double?, birthWeight: Double?,
        deliveryType: String, resuscitationNeeded: Boolean
    ): String {
        val recommendations = mutableListOf<String>()

        recommendations.add("📋 RECOMENDACIONES DE SEGUIMIENTO:")
        recommendations.add("")

        // Score-based follow-up
        when (score) {
            in 7..10 -> {
                recommendations.add("✅ SEGUIMIENTO ESTÁNDAR:")
                recommendations.add("• Control pediátrico a los 3-5 días")
                recommendations.add("• Tamiz neonatal ampliado")
                recommendations.add("• Vacunación según esquema nacional")
                recommendations.add("• Promoción de lactancia materna exclusiva")
                recommendations.add("• Evaluación del desarrollo a los 2 meses")
            }

            in 4..6 -> {
                recommendations.add("🟡 SEGUIMIENTO INTENSIFICADO:")
                recommendations.add("• Control pediátrico en 24-48 horas")
                recommendations.add("• Evaluación neurológica a las 2 semanas")
                recommendations.add("• Audiometría antes del alta")
                recommendations.add("• Ecocardiograma si indicado")
                recommendations.add("• Seguimiento del desarrollo mensual")
                recommendations.add("• Intervención temprana si necesario")
            }

            in 0..3 -> {
                recommendations.add("🚨 SEGUIMIENTO ESPECIALIZADO:")
                recommendations.add("• Neurología pediátrica urgente")
                recommendations.add("• Cardiología pediátrica")
                recommendations.add("• Programa de alto riesgo neurológico")
                recommendations.add("• Resonancia magnética cerebral")
                recommendations.add("• Evaluación oftalmológica")
                recommendations.add("• Fisioterapia y terapia ocupacional")
                recommendations.add("• Seguimiento multidisciplinario")
            }
        }

        // Gestational age specific follow-up
        gestationalAge?.let { age ->
            if (age < 37) {
                recommendations.add("")
                recommendations.add("👶 SEGUIMIENTO PREMATUREZ:")
                recommendations.add("• Programa de seguimiento de prematuros")
                recommendations.add("• Evaluación oftalmológica (retinopatía)")
                recommendations.add("• Audiometría (potenciales evocados)")
                recommendations.add("• Evaluación del desarrollo corregida por edad")
                recommendations.add("• Inmunizaciones según peso y edad gestacional")
            }
        }

        // Birth weight specific follow-up
        birthWeight?.let { weight ->
            if (weight < 2500) {
                recommendations.add("")
                recommendations.add("⚖️ SEGUIMIENTO BAJO PESO:")
                recommendations.add("• Monitoreo estrecho del crecimiento")
                recommendations.add("• Suplementación nutricional si necesario")
                recommendations.add("• Evaluación del neurodesarrollo")
                recommendations.add("• Prevención de infecciones")
            }
        }

        // Resuscitation follow-up
        if (resuscitationNeeded) {
            recommendations.add("")
            recommendations.add("🚨 SEGUIMIENTO POST-REANIMACIÓN:")
            recommendations.add("• Evaluación neurológica especializada")
            recommendations.add("• EEG y neuroimagen si indicado")
            recommendations.add("• Programa de estimulación temprana")
            recommendations.add("• Evaluación cardiológica")
            recommendations.add("• Seguimiento pulmonar si ventilación prolongada")
        }

        // Family support
        recommendations.add("")
        recommendations.add("👨‍👩‍👧‍👦 APOYO FAMILIAR:")
        recommendations.add("• Educación sobre cuidados neonatales")
        recommendations.add("• Signos de alarma para consulta inmediata")
        recommendations.add("• Promoción del vínculo materno-filial")
        recommendations.add("• Apoyo psicológico si trauma del parto")
        recommendations.add("• Grupos de apoyo para padres")

        return recommendations.joinToString("\n")
    }

    override fun getInterpretation(result: CalculationResult): String {
        val totalScore = result.resultValues["total_score"] ?: ""
        val clinicalStatus = result.resultValues["clinical_status"] ?: ""
        val evaluationTime = result.inputValues["evaluation_time"] ?: ""

        // Get individual scores
        val appearance = extractScoreFromOption(result.inputValues["appearance_color"] ?: "")
        val pulse = extractScoreFromOption(result.inputValues["pulse_heart_rate"] ?: "")
        val grimace = extractScoreFromOption(result.inputValues["grimace_reflex"] ?: "")
        val activity = extractScoreFromOption(result.inputValues["activity_muscle_tone"] ?: "")
        val respiratory = extractScoreFromOption(result.inputValues["respiratory_effort"] ?: "")

        return """
INTERPRETACIÓN CLÍNICA - PUNTUACIÓN APGAR

PUNTUACIÓN TOTAL: $totalScore/10 puntos
TIEMPO DE EVALUACIÓN: $evaluationTime
ESTADO CLÍNICO: $clinicalStatus

COMPONENTES EVALUADOS:
- Apariencia (Color): $appearance/2 puntos
- Pulso (Frecuencia Cardíaca): $pulse/2 puntos
- Gesticulación (Irritabilidad): $grimace/2 puntos
- Actividad (Tono Muscular): $activity/2 puntos
- Respiración (Esfuerzo): $respiratory/2 puntos

RANGOS DE INTERPRETACIÓN:
- 7-10 puntos: Buen estado
- 4-6 puntos: Asistencia moderada
- 0-3 puntos: Asistencia inmediata

SIGNIFICADO CLÍNICO:
La puntuación APGAR evalúa la adaptación del recién nacido a la vida extrauterina. Desarrollada por la Dra. Virginia Apgar en 1952, es un predictor confiable de la necesidad de intervención médica inmediata.

EVALUACIÓN TEMPORAL:
- 1 minuto: Refleja tolerancia al proceso del parto
- 5 minutos: Predictor de pronóstico neurológico
- 10 minutos: Evaluación post-reanimación

VALIDEZ CLÍNICA:
- Sensibilidad del 99% para identificar neonatos que requieren reanimación
- Especificidad del 95% para descartar depresión neonatal
- Correlación significativa con pH de cordón umbilical

LIMITACIONES:
- No predice desarrollo neurológico a largo plazo por sí solo
- Puede estar influenciado por medicamentos maternos
- Prematurez puede afectar algunos componentes
- Debe interpretarse en contexto clínico completo

MARCO LEGAL MEXICANO:
Basado en NOM-007-SSA2-2016 para la atención del embarazo, parto y puerperio. Evaluación obligatoria en todos los nacimientos en México.
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "NOM-007-SSA2-2016 para la atención del embarazo, parto y puerperio",
                source = "Diario Oficial de la Federación (DOF)",
                url = "https://dof.gob.mx"
            ),
            Reference(
                title = "Guías de Reanimación Neonatal",
                source = "Academia Mexicana de Pediatría",
                year = 2023
            ),
            Reference(
                title = "Manual de Neonatología",
                source = "Instituto Nacional de Perinatología",
                year = 2022
            ),
            Reference(
                title = "A proposal for a new method of evaluation of the newborn infant",
                source = "Virginia Apgar, 1953 - Artículo original",
                year = 1953
            ),
            Reference(
                title = "Protocolo de Atención del Recién Nacido",
                source = "Secretaría de Salud México",
                year = 2023
            ),
            Reference(
                title = "Guía de Práctica Clínica: Prevención, Diagnóstico y Tratamiento del Recién Nacido con Trastorno del Ritmo y Frecuencia Respiratoria",
                source = "Instituto Mexicano del Seguro Social (IMSS)",
                year = 2022
            ),
            Reference(
                title = "Manual de Procedimientos en Sala de Partos",
                source = "Hospital General de México",
                year = 2023
            )
        )
    }
}