package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult
import kotlin.math.round

class IVDripRateCalculator : Calculator {

    override val calculatorId = "iv_drip_rate"

    // Drop factors for different equipment types
    private val dropFactors = mapOf(
        "10 gtt/mL" to 10.0,
        "15 gtt/mL" to 15.0,
        "20 gtt/mL" to 20.0,
        "60 gtt/mL (microgotero)" to 60.0
    )

    // Safe flow rate limits (mL/h) for different patient populations
    private val safeFlowRateLimits = mapOf(
        "adult_normal" to 250.0,
        "adult_cardiac" to 125.0,
        "elderly" to 100.0,
        "pediatric" to 50.0
    )

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate total volume
        val volumeStr = inputs["total_volume"]
        if (volumeStr.isNullOrBlank()) {
            errors.add("El volumen total es obligatorio")
        } else {
            val volume = volumeStr.toDoubleOrNull()
            if (volume == null) {
                errors.add("El volumen debe ser un número válido")
            } else if (volume <= 0) {
                errors.add("El volumen debe ser mayor que cero")
            } else if (volume > 5000.0) {
                errors.add("El volumen excede el límite máximo (5000 mL)")
            }
        }

        // Validate infusion time
        val timeStr = inputs["infusion_time_hours"]
        if (timeStr.isNullOrBlank()) {
            errors.add("El tiempo de infusión es obligatorio")
        } else {
            val time = timeStr.toDoubleOrNull()
            if (time == null) {
                errors.add("El tiempo debe ser un número válido")
            } else if (time <= 0) {
                errors.add("El tiempo debe ser mayor que cero")
            } else if (time > 48.0) {
                errors.add("El tiempo excede el límite máximo (48 horas)")
            }
        }

        // Validate drop factor
        val dropFactor = inputs["drop_factor"]
        if (dropFactor.isNullOrBlank()) {
            errors.add("El factor de goteo es obligatorio")
        } else if (!dropFactors.containsKey(dropFactor)) {
            errors.add("Factor de goteo inválido")
        }

        // Validate patient weight if provided
        val weightStr = inputs["patient_weight"]
        if (!weightStr.isNullOrBlank()) {
            val weight = weightStr.toDoubleOrNull()
            if (weight == null || weight <= 0 || weight > 200.0) {
                errors.add("El peso debe ser un número válido entre 1-200 kg")
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
        val volume = inputs["total_volume"]!!.toDouble()
        val timeHours = inputs["infusion_time_hours"]!!.toDouble()
        val dropFactorKey = inputs["drop_factor"]!!
        val dropFactorValue = dropFactors[dropFactorKey]!!
        val fluidType = inputs["fluid_type"] ?: "Solución Salina 0.9%"
        val patientWeight = inputs["patient_weight"]?.toDoubleOrNull()

        // Calculate flow rate (mL/h)
        val flowRate = volume / timeHours

        // Calculate time in minutes
        val timeMinutes = timeHours * 60

        // Calculate drip rate (gtt/min)
        val dripRate = (volume * dropFactorValue) / timeMinutes

        // Calculate drops in 15 seconds (practical for nurses)
        val dropsIn15Seconds = dripRate / 4.0

        // Format duration
        val duration = formatDuration(timeHours)

        // Generate safety warnings and monitoring guidelines
        val safetyWarnings = generateSafetyWarnings(flowRate, dripRate, fluidType, patientWeight)
        val monitoringGuidelines = generateMonitoringGuidelines(fluidType, flowRate, timeHours)

        // Format results
        val results = mapOf(
            "drip_rate" to String.format("%.1f", dripRate),
            "flow_rate" to String.format("%.1f", flowRate),
            "drops_per_15_seconds" to String.format("%.1f", dropsIn15Seconds),
            "infusion_duration" to duration,
            "safety_warnings" to safetyWarnings,
            "monitoring_guidelines" to monitoringGuidelines
        )

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun formatDuration(hours: Double): String {
        val wholeHours = hours.toInt()
        val minutes = ((hours - wholeHours) * 60).toInt()

        return when {
            wholeHours == 0 -> "${minutes} minutos"
            minutes == 0 -> "${wholeHours} hora${if (wholeHours > 1) "s" else ""}"
            else -> "${wholeHours}h ${minutes}min"
        }
    }

    private fun generateSafetyWarnings(
        flowRate: Double,
        dripRate: Double,
        fluidType: String,
        patientWeight: Double?
    ): String {
        val warnings = mutableListOf<String>()

        // Flow rate warnings
        when {
            flowRate > 300.0 -> warnings.add("⚠️ VELOCIDAD MUY ALTA - Riesgo de sobrecarga circulatoria")
            flowRate > 200.0 -> warnings.add("⚠️ VELOCIDAD ALTA - Monitoreo cardiopulmonar estrecho")
            flowRate < 10.0 -> warnings.add("⚠️ VELOCIDAD MUY LENTA - Verificar permeabilidad")
        }

        // Drip rate practical warnings
        when {
            dripRate > 60.0 -> warnings.add("⚠️ GOTEO MUY RÁPIDO - Difícil de contar manualmente")
            dripRate < 5.0 -> warnings.add("⚠️ GOTEO MUY LENTO - Riesgo de coagulación")
        }

        // Fluid-specific warnings
        when {
            fluidType.contains("Dextrosa") && flowRate > 150.0 ->
                warnings.add("⚠️ DEXTROSA RÁPIDA - Monitorear glucemia")
            fluidType.contains("Sangre") && flowRate > 100.0 ->
                warnings.add("⚠️ HEMODERIVADOS - Velocidad máxima excedida")
            fluidType.contains("Medicamento") ->
                warnings.add("⚠️ MEDICAMENTO DILUIDO - Verificar compatibilidad")
        }

        // Weight-based warnings
        patientWeight?.let { weight ->
            val mlPerKgPerHour = flowRate / weight
            when {
                weight < 20.0 && flowRate > 50.0 ->
                    warnings.add("⚠️ PACIENTE PEDIÁTRICO - Velocidad alta para el peso")
                weight > 80.0 && mlPerKgPerHour > 3.0 ->
                    warnings.add("⚠️ ALTA VELOCIDAD POR PESO - Monitoreo intensivo")

                else -> {}
            }
        }

        if (warnings.isEmpty()) {
            warnings.add("✅ Parámetros dentro de rangos seguros")
        }

        warnings.add("⚠️ Verificar permeabilidad de catéter antes de iniciar")
        warnings.add("⚠️ Confirmar indicación médica y velocidad prescrita")

        return warnings.joinToString("\n")
    }

    private fun generateMonitoringGuidelines(
        fluidType: String,
        flowRate: Double,
        timeHours: Double
    ): String {
        val guidelines = mutableListOf<String>()

        // General monitoring
        guidelines.add("📊 SIGNOS VITALES cada 2-4 horas")
        guidelines.add("📊 BALANCE HÍDRICO estricto")
        guidelines.add("📊 SITIO DE PUNCIÓN cada hora")

        // Flow rate specific monitoring
        when {
            flowRate > 200.0 -> {
                guidelines.add("📊 MONITOREO CARDIACO continuo")
                guidelines.add("📊 SATURACIÓN DE OXÍGENO continua")
                guidelines.add("📊 SIGNOS DE SOBRECARGA cada 30 min")
            }
            flowRate > 100.0 -> {
                guidelines.add("📊 SIGNOS DE SOBRECARGA cada hora")
                guidelines.add("📊 AUSCULTACIÓN PULMONAR cada 2h")
            }
        }

        // Fluid-specific monitoring
        when {
            fluidType.contains("Dextrosa") -> {
                guidelines.add("📊 GLUCEMIA cada 4-6 horas")
                guidelines.add("📊 SIGNOS DE HIPERGLUCEMIA")
            }
            fluidType.contains("Salina") -> {
                guidelines.add("📊 ELECTROLITOS séricos diarios")
                guidelines.add("📊 SIGNOS DE HIPERNATREMIA")
            }
            fluidType.contains("Lactato") -> {
                guidelines.add("📊 ESTADO ÁCIDO-BASE")
                guidelines.add("📊 FUNCIÓN RENAL")
            }
            fluidType.contains("Sangre") -> {
                guidelines.add("📊 REACCIONES TRANSFUSIONALES")
                guidelines.add("📊 TEMPERATURA cada 15 min primera hora")
                guidelines.add("📊 HEMOGLOBINA post-transfusión")
            }
        }

        // Time-based monitoring
        if (timeHours > 24.0) {
            guidelines.add("📊 EVALUACIÓN NUTRICIONAL diaria")
            guidelines.add("📊 FUNCIÓN RENAL cada 24h")
        }

        guidelines.add("📊 DOCUMENTAR volumen administrado cada turno")
        guidelines.add("📊 VERIFICAR bomba de infusión si disponible")

        return guidelines.joinToString("\n")
    }

    override fun getInterpretation(result: CalculationResult): String {
        val dripRate = result.resultValues["drip_rate"] ?: ""
        val flowRate = result.resultValues["flow_rate"] ?: ""
        val drops15Sec = result.resultValues["drops_per_15_seconds"] ?: ""
        val duration = result.resultValues["infusion_duration"] ?: ""
        val volume = result.inputValues["total_volume"] ?: ""
        val dropFactor = result.inputValues["drop_factor"] ?: ""

        return """
INTERPRETACIÓN CLÍNICA - VELOCIDAD DE GOTEO IV

💧 RESULTADOS PRINCIPALES:
• Velocidad de goteo: $dripRate gtt/min
• Velocidad de flujo: $flowRate mL/h
• Gotas en 15 segundos: $drops15Sec gtt
• Duración total: $duration

📋 PARÁMETROS DE CÁLCULO:
• Volumen total: $volume mL
• Factor de goteo: $dropFactor
• Duración programada: $duration

🔬 FÓRMULAS UTILIZADAS:
• Goteo (gtt/min) = (Volumen × Factor) ÷ Tiempo(min)
• Flujo (mL/h) = Volumen ÷ Tiempo(h)
• Conteo 15 seg = Goteo ÷ 4

⚠️ VERIFICACIONES OBLIGATORIAS:
• Confirmar PRESCRIPCIÓN MÉDICA exacta
• Verificar FACTOR DE GOTEO del equipo
• Comprobar PERMEABILIDAD del catéter
• Ajustar bomba de infusión si disponible

📊 TÉCNICA DE CONTEO:
• Contar gotas durante 15 segundos
• Multiplicar por 4 para obtener gtt/min
• Ajustar manualmente la llave de paso
• Verificar cada 30-60 minutos

🏥 PROTOCOLOS MEXICANOS:
• Basado en estándares Roosevelt Hospital
• Factores de goteo validados clínicamente
• Límites de seguridad por población
• Monitoreo según tipo de fluido
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "Cálculo de Goteo Intravenoso",
                source = "Blog Roosevelt Hospital México",
                url = "https://blog.roosevelt.edu.mx"
            ),
            Reference(
                title = "Administración de Fluidos Intravenosos",
                source = "Sociedad Mexicana de Enfermería",
                year = 2023
            ),
            Reference(
                title = "IV Flow Rate Calculations",
                source = "Nursing Drug Calculations",
                year = 2022
            ),
            Reference(
                title = "Factores de Goteo Estandarizados",
                source = "Manual de Procedimientos de Enfermería",
                year = 2023
            ),
            Reference(
                title = "Seguridad en Terapia Intravenosa",
                source = "Instituto Mexicano del Seguro Social",
                year = 2022
            )
        )
    }
}