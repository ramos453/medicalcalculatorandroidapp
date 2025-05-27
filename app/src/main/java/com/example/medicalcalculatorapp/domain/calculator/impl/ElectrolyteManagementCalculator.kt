package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult
import kotlin.math.min
import kotlin.math.max

class ElectrolyteManagementCalculator : Calculator {

    override val calculatorId = "electrolyte_management"

    // IMSS Safety Constants
    private val sodiumDistributionFactor = 0.6 // Total body water fraction for Na+
    private val maxSodiumCorrectionRate = 12.0 // mEq/L per 24h (IMSS standard)
    private val maxPotassiumIVRate = 20.0 // mEq/h maximum IV rate
    private val maxPotassiumConcentration = 80.0 // mEq/L in peripheral IV
    private val normalSodiumRange = 135.0..145.0
    private val normalPotassiumRange = 3.5..5.0

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate patient weight
        val weightStr = inputs["patient_weight"]
        if (weightStr.isNullOrBlank()) {
            errors.add("El peso del paciente es obligatorio")
        } else {
            val weight = weightStr.toDoubleOrNull()
            if (weight == null || weight <= 0 || weight > 200.0) {
                errors.add("El peso debe estar entre 1-200 kg")
            }
        }

        // Validate patient age
        val ageStr = inputs["patient_age"]
        if (!ageStr.isNullOrBlank()) {
            val age = ageStr.toDoubleOrNull()
            if (age == null || age < 0 || age > 120.0) {
                errors.add("La edad debe estar entre 0-120 años")
            }
        }

        // Validate sodium levels
        val currentNaStr = inputs["current_sodium"]
        val targetNaStr = inputs["target_sodium"]
        if (currentNaStr.isNullOrBlank()) {
            errors.add("El sodio sérico actual es obligatorio")
        } else {
            val currentNa = currentNaStr.toDoubleOrNull()
            if (currentNa == null || currentNa < 100.0 || currentNa > 180.0) {
                errors.add("Sodio actual debe estar entre 100-180 mEq/L")
            }
        }
        if (targetNaStr.isNullOrBlank()) {
            errors.add("El sodio deseado es obligatorio")
        } else {
            val targetNa = targetNaStr.toDoubleOrNull()
            if (targetNa == null || !normalSodiumRange.contains(targetNa)) {
                errors.add("Sodio deseado debe estar entre 135-145 mEq/L")
            }
        }

        // Validate potassium levels
        val currentKStr = inputs["current_potassium"]
        val targetKStr = inputs["target_potassium"]
        if (currentKStr.isNullOrBlank()) {
            errors.add("El potasio sérico actual es obligatorio")
        } else {
            val currentK = currentKStr.toDoubleOrNull()
            if (currentK == null || currentK < 1.5 || currentK > 6.0) {
                errors.add("Potasio actual debe estar entre 1.5-6.0 mEq/L")
            }
        }
        if (targetKStr.isNullOrBlank()) {
            errors.add("El potasio deseado es obligatorio")
        } else {
            val targetK = targetKStr.toDoubleOrNull()
            if (targetK == null || !normalPotassiumRange.contains(targetK)) {
                errors.add("Potasio deseado debe estar entre 3.5-5.0 mEq/L")
            }
        }

        // Validate correction time
        val correctionTimeStr = inputs["correction_time_hours"]
        if (!correctionTimeStr.isNullOrBlank()) {
            val correctionTime = correctionTimeStr.toDoubleOrNull()
            if (correctionTime == null || correctionTime < 6.0 || correctionTime > 48.0) {
                errors.add("Tiempo de corrección debe estar entre 6-48 horas")
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
        val age = inputs["patient_age"]?.toDoubleOrNull() ?: 45.0
        val currentSodium = inputs["current_sodium"]!!.toDouble()
        val targetSodium = inputs["target_sodium"]!!.toDouble()
        val correctionTimeHours = inputs["correction_time_hours"]?.toDoubleOrNull() ?: 24.0
        val currentPotassium = inputs["current_potassium"]!!.toDouble()
        val targetPotassium = inputs["target_potassium"]!!.toDouble()
        val potassiumRoute = inputs["potassium_route"] ?: "Vía Intravenosa"
        val renalFunction = inputs["renal_function"] ?: "Normal"
        val cardiacStatus = inputs["cardiac_status"] ?: "Normal"
        val diureticUse = inputs["diuretic_use"] == "true"
        val neurologicalSymptoms = inputs["neurological_symptoms"] == "true"

        // Calculate sodium deficit and replacement
        val (sodiumDeficit, sodiumReplacementRate, sodiumSolutionVolume) = calculateSodiumReplacement(
            currentSodium, targetSodium, weight, correctionTimeHours, neurologicalSymptoms
        )

        // Calculate potassium deficit and replacement
        val (potassiumDeficit, potassiumDose, potassiumInfusionRate) = calculatePotassiumReplacement(
            currentPotassium, targetPotassium, weight, potassiumRoute, renalFunction, cardiacStatus
        )

        // Generate safety warnings, monitoring protocol, and solution recommendations
        val safetyWarnings = generateSafetyWarnings(
            currentSodium, currentPotassium, sodiumReplacementRate, potassiumInfusionRate,
            renalFunction, cardiacStatus, neurologicalSymptoms
        )
        val monitoringProtocol = generateMonitoringProtocol(
            currentSodium, currentPotassium, renalFunction, cardiacStatus, correctionTimeHours
        )
        val solutionRecommendations = generateSolutionRecommendations(
            sodiumDeficit, potassiumDose, potassiumRoute, renalFunction
        )

        // Format results
        val results = mapOf(
            "sodium_deficit" to String.format("%.1f", sodiumDeficit),
            "sodium_replacement_rate" to String.format("%.2f", sodiumReplacementRate),
            "sodium_solution_volume" to String.format("%.0f", sodiumSolutionVolume),
            "potassium_deficit" to String.format("%.1f", potassiumDeficit),
            "potassium_dose" to String.format("%.1f", potassiumDose),
            "potassium_infusion_rate" to String.format("%.1f", potassiumInfusionRate),
            "safety_warnings" to safetyWarnings,
            "monitoring_protocol" to monitoringProtocol,
            "solution_recommendations" to solutionRecommendations
        )

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun calculateSodiumReplacement(
        currentNa: Double,
        targetNa: Double,
        weight: Double,
        correctionTimeHours: Double,
        hasNeurologicalSymptoms: Boolean
    ): Triple<Double, Double, Double> {
        // IMSS Formula: Deficit = (Target - Current) × Weight × 0.6
        val sodiumDeficit = (targetNa - currentNa) * weight * sodiumDistributionFactor

        // Safety limit: maximum 12 mEq/L per 24h (IMSS protocol)
        val maxSafeCorrection = (maxSodiumCorrectionRate * weight * sodiumDistributionFactor) *
                (correctionTimeHours / 24.0)

        // For neurological symptoms, allow faster initial correction (first 6 hours)
        val actualDeficitToCorrect = if (hasNeurologicalSymptoms && correctionTimeHours >= 24.0) {
            min(sodiumDeficit, maxSafeCorrection * 1.25) // 25% faster if symptomatic
        } else {
            min(sodiumDeficit, maxSafeCorrection)
        }

        val replacementRate = actualDeficitToCorrect / correctionTimeHours

        // Calculate volume of normal saline (154 mEq Na+/L)
        val normalSalineConcentration = 154.0 // mEq/L
        val solutionVolume = (actualDeficitToCorrect / normalSalineConcentration) * 1000 // mL

        return Triple(sodiumDeficit, replacementRate, solutionVolume)
    }

    private fun calculatePotassiumReplacement(
        currentK: Double,
        targetK: Double,
        weight: Double,
        route: String,
        renalFunction: String,
        cardiacStatus: String
    ): Triple<Double, Double, Double> {
        // Estimate total body potassium deficit
        // Rule of thumb: 1 mEq/L decrease ≈ 200-400 mEq total body deficit
        val potassiumDeficit = (targetK - currentK) * weight * 4.0 // Conservative estimate

        // Determine safe replacement dose
        val maxSingleDose = when (route) {
            "Vía Oral" -> min(80.0, potassiumDeficit) // Oral: up to 80 mEq per dose
            else -> min(40.0, potassiumDeficit) // IV: up to 40 mEq per dose
        }

        // Adjust for renal function
        val renalAdjustedDose = when (renalFunction) {
            "Insuficiencia Severa", "Diálisis" -> maxSingleDose * 0.5
            "Insuficiencia Moderada" -> maxSingleDose * 0.75
            else -> maxSingleDose
        }

        // Calculate infusion rate for IV
        val infusionRate = when (route) {
            "Vía Oral" -> 0.0 // Not applicable for oral
            else -> {
                val maxRate = if (cardiacStatus == "Normal") maxPotassiumIVRate else 10.0
                min(maxRate, renalAdjustedDose / 2.0) // Minimum 2-hour infusion
            }
        }

        return Triple(potassiumDeficit, renalAdjustedDose, infusionRate)
    }

    private fun generateSafetyWarnings(
        currentNa: Double,
        currentK: Double,
        naReplacementRate: Double,
        kInfusionRate: Double,
        renalFunction: String,
        cardiacStatus: String,
        neurologicalSymptoms: Boolean
    ): String {
        val warnings = mutableListOf<String>()

        // Sodium warnings
        when {
            currentNa < 120.0 -> warnings.add("🚨 HIPONATREMIA SEVERA - Riesgo de edema cerebral")
            currentNa < 125.0 -> warnings.add("⚠️ HIPONATREMIA GRAVE - Monitoreo neurológico intensivo")
            currentNa > 150.0 -> warnings.add("⚠️ HIPERNATREMIA - Corrección gradual obligatoria")
        }

        if (naReplacementRate > 0.5 && currentNa < 125.0) {
            warnings.add("⚠️ CORRECCIÓN RÁPIDA Na+ - Riesgo de desmielinización osmótica")
        }

        if (neurologicalSymptoms) {
            warnings.add("🧠 SÍNTOMAS NEUROLÓGICOS - Balance riesgo/beneficio crítico")
        }

        // Potassium warnings
        when {
            currentK < 2.5 -> warnings.add("🚨 HIPOPOTASEMIA SEVERA - Riesgo de arritmias letales")
            currentK < 3.0 -> warnings.add("⚠️ HIPOPOTASEMIA GRAVE - Monitoreo cardíaco continuo")
            currentK > 5.5 -> warnings.add("⚠️ HIPERPOTASEMIA - Verificar función renal")
        }

        if (kInfusionRate > 10.0) {
            warnings.add("⚠️ INFUSIÓN K+ RÁPIDA - Monitoreo cardíaco obligatorio")
        }

        // Renal function warnings
        if (renalFunction in listOf("Insuficiencia Moderada", "Insuficiencia Severa")) {
            warnings.add("🔴 FUNCIÓN RENAL COMPROMETIDA - Ajuste de dosis obligatorio")
        }

        // Cardiac warnings
        if (cardiacStatus != "Normal") {
            warnings.add("❤️ ESTADO CARDÍACO ALTERADO - Infusión lenta de electrolitos")
        }

        if (warnings.isEmpty()) {
            warnings.add("✅ Parámetros dentro de rangos de seguridad")
        }

        warnings.add("⚠️ NUNCA administrar K+ IV en bolo")
        warnings.add("⚠️ Verificar permeabilidad venosa antes de infusión")

        return warnings.joinToString("\n")
    }

    private fun generateMonitoringProtocol(
        currentNa: Double,
        currentK: Double,
        renalFunction: String,
        cardiacStatus: String,
        correctionTimeHours: Double
    ): String {
        val monitoring = mutableListOf<String>()

        // General monitoring
        monitoring.add("📊 MONITOREO OBLIGATORIO:")

        // Electrolyte monitoring frequency
        when {
            currentNa < 125.0 || currentK < 2.5 -> {
                monitoring.add("• Electrolitos séricos cada 2-4 horas")
                monitoring.add("• Monitoreo cardíaco continuo")
            }
            currentNa < 130.0 || currentK < 3.0 -> {
                monitoring.add("• Electrolitos séricos cada 6 horas")
                monitoring.add("• Monitoreo cardíaco cada 2 horas")
            }
            else -> {
                monitoring.add("• Electrolitos séricos cada 8-12 horas")
                monitoring.add("• Signos vitales cada 4 horas")
            }
        }

        // Neurological monitoring
        if (currentNa < 130.0) {
            monitoring.add("• Evaluación neurológica cada 2 horas")
            monitoring.add("• Escala de coma de Glasgow")
            monitoring.add("• Vigilar convulsiones y alteraciones mentales")
        }

        // Cardiac monitoring
        if (currentK < 3.5 || cardiacStatus != "Normal") {
            monitoring.add("• ECG cada 4 horas")
            monitoring.add("• Vigilar arritmias y cambios ST-T")
            monitoring.add("• Monitoreo de QT prolongado")
        }

        // Renal monitoring
        if (renalFunction != "Normal") {
            monitoring.add("• Creatinina sérica diaria")
            monitoring.add("• Balance hídrico estricto")
            monitoring.add("• Diuresis cada hora")
        }

        // Safety checks
        monitoring.add("• Verificar sitio de infusión cada hora")
        monitoring.add("• Documentar volumen y velocidad de infusión")
        monitoring.add("• Tener disponible calcio IV para emergencias K+")

        return monitoring.joinToString("\n")
    }

    private fun generateSolutionRecommendations(
        sodiumDeficit: Double,
        potassiumDose: Double,
        potassiumRoute: String,
        renalFunction: String
    ): String {
        val recommendations = mutableListOf<String>()

        recommendations.add("💊 SOLUCIONES RECOMENDADAS:")

        // Sodium solutions
        if (sodiumDeficit > 0) {
            recommendations.add("• SODIO:")
            recommendations.add("  - Solución Salina 0.9% (154 mEq/L)")
            recommendations.add("  - Solución Salina 3% (513 mEq/L) solo en UCI")
            recommendations.add("  - Lactato de Ringer (130 mEq/L) alternativa")
        }

        // Potassium solutions
        if (potassiumDose > 0) {
            recommendations.add("• POTASIO:")
            when (potassiumRoute) {
                "Vía Oral" -> {
                    recommendations.add("  - Cloruro de Potasio VO: 10-20 mEq por toma")
                    recommendations.add("  - Citrato de Potasio: mejor tolerancia gástrica")
                    recommendations.add("  - Administrar con alimentos")
                }
                else -> {
                    recommendations.add("  - Cloruro de Potasio IV: máximo 80 mEq/L")
                    recommendations.add("  - Fosfato de Potasio: si también déficit de fósforo")
                    recommendations.add("  - Diluir en solución glucosada o salina")
                    recommendations.add("  - NUNCA en bolo directo")
                }
            }
        }

        // Special considerations
        if (renalFunction in listOf("Insuficiencia Moderada", "Insuficiencia Severa")) {
            recommendations.add("• CONSIDERACIONES RENALES:")
            recommendations.add("  - Reducir dosis de mantenimiento")
            recommendations.add("  - Evitar soluciones con fósforo")
            recommendations.add("  - Monitoreo más frecuente")
        }

        recommendations.add("• COMPATIBILIDADES:")
        recommendations.add("  - K+ compatible con glucosa, salina, lactato")
        recommendations.add("  - Evitar mezclar electrolitos concentrados")
        recommendations.add("  - Usar bombas de infusión para precisión")

        return recommendations.joinToString("\n")
    }

    override fun getInterpretation(result: CalculationResult): String {
        val sodiumDeficit = result.resultValues["sodium_deficit"] ?: ""
        val potassiumDeficit = result.resultValues["potassium_deficit"] ?: ""
        val sodiumRate = result.resultValues["sodium_replacement_rate"] ?: ""
        val potassiumRate = result.resultValues["potassium_infusion_rate"] ?: ""
        val currentNa = result.inputValues["current_sodium"] ?: ""
        val currentK = result.inputValues["current_potassium"] ?: ""

        return """
INTERPRETACIÓN CLÍNICA - GESTIÓN DE ELECTROLITOS

⚡ RESULTADOS PRINCIPALES:
• Déficit de Sodio: $sodiumDeficit mEq
• Déficit de Potasio: $potassiumDeficit mEq
• Velocidad reemplazo Na+: $sodiumRate mEq/h
• Velocidad infusión K+: $potassiumRate mEq/h

📊 NIVELES ACTUALES:
• Sodio sérico: $currentNa mEq/L (Normal: 135-145)
• Potasio sérico: $currentK mEq/L (Normal: 3.5-5.0)

🔬 METODOLOGÍA IMSS:
• Déficit Na+: (Deseado - Actual) × Peso × 0.6
• Límite seguridad: 12 mEq/L por 24h
• K+ máximo IV: 20 mEq/h con monitoreo
• Factores de distribución validados

⚠️ LÍMITES DE SEGURIDAD:
• Corrección Na+ máxima: 12 mEq/L/24h
• Infusión K+ máxima: 20 mEq/h (10 mEq/h sin monitoreo)
• Concentración K+ IV: máximo 80 mEq/L periférico
• Monitoreo cardíaco obligatorio para K+ >10 mEq/h

🚨 COMPLICACIONES CRÍTICAS:
• Síndrome de desmielinización osmótica (Na+ rápido)
• Arritmias cardíacas por hipopotasemia
• Edema cerebral por hiponatremia severa
• Hiperpotasemia iatrogénica

🏥 PROTOCOLO MEXICANO IMSS:
• Basado en guías institucionales 2023
• Validado para población mexicana
• Ajustado por función renal
• Incluye factores de comorbilidad
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "Manejo de Trastornos Hidroelectrolíticos",
                source = "Instituto Mexicano del Seguro Social (IMSS)",
                year = 2023
            ),
            Reference(
                title = "Corrección Segura de Hiponatremia",
                source = "Blog Roosevelt Hospital México",
                url = "https://blog.roosevelt.edu.mx"
            ),
            Reference(
                title = "Protocolos de Seguridad del Paciente",
                source = "Aesculap Seguridad del Paciente México",
                url = "https://aesculapseguridaddelpaciente.org.mx"
            ),
            Reference(
                title = "Reemplazo de Electrolitos en Pediatría",
                source = "Salud Infantil México",
                url = "https://saludinfantil.org"
            ),
            Reference(
                title = "Electrolyte Disorders in Critical Care",
                source = "SlideShare Medical Education",
                url = "https://www.slideshare.net"
            )
        )
    }
}