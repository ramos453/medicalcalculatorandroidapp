package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult
import kotlin.math.round

class PediatricDosageCalculator : Calculator {

    override val calculatorId = "pediatric_dosage"

    // Pediatric medication dosages based on Mexican pediatric guidelines
    private val pediatricMedications = mapOf(
        "Amoxicilina" to MedicationInfo(
            standardDose = 50.0, // mg/kg/day
            maxDose = 90.0,
            dosesPerDay = 2,
            minAge = 1, // months
            maxAge = 216,
            route = "Oral",
            conditions = listOf("Infección Respiratoria", "Infección del Oído", "Infección Urinaria")
        ),
        "Paracetamol" to MedicationInfo(
            standardDose = 60.0, // mg/kg/day
            maxDose = 90.0,
            dosesPerDay = 4,
            minAge = 1,
            maxAge = 216,
            route = "Oral/IV",
            conditions = listOf("Fiebre", "Dolor/Inflamación")
        ),
        "Ibuprofeno" to MedicationInfo(
            standardDose = 20.0, // mg/kg/day
            maxDose = 40.0,
            dosesPerDay = 3,
            minAge = 6, // Not recommended under 6 months
            maxAge = 216,
            route = "Oral",
            conditions = listOf("Fiebre", "Dolor/Inflamación")
        ),
        "Azitromicina" to MedicationInfo(
            standardDose = 10.0, // mg/kg/day
            maxDose = 12.0,
            dosesPerDay = 1,
            minAge = 6,
            maxAge = 216,
            route = "Oral",
            conditions = listOf("Infección Respiratoria", "Infección del Oído")
        ),
        "Cefixima" to MedicationInfo(
            standardDose = 8.0, // mg/kg/day
            maxDose = 12.0,
            dosesPerDay = 2,
            minAge = 6,
            maxAge = 216,
            route = "Oral",
            conditions = listOf("Infección Respiratoria", "Infección Urinaria")
        ),
        "Trimetoprim-Sulfametoxazol" to MedicationInfo(
            standardDose = 8.0, // mg/kg/day (based on TMP component)
            maxDose = 12.0,
            dosesPerDay = 2,
            minAge = 2,
            maxAge = 216,
            route = "Oral",
            conditions = listOf("Infección Urinaria", "Infección Gastrointestinal")
        ),
        "Claritromicina" to MedicationInfo(
            standardDose = 15.0, // mg/kg/day
            maxDose = 20.0,
            dosesPerDay = 2,
            minAge = 6,
            maxAge = 216,
            route = "Oral",
            conditions = listOf("Infección Respiratoria")
        ),
        "Dexametasona" to MedicationInfo(
            standardDose = 0.6, // mg/kg/day
            maxDose = 1.0,
            dosesPerDay = 1,
            minAge = 1,
            maxAge = 216,
            route = "Oral/IV",
            conditions = listOf("Asma/Broncoespasmo", "Inflamación")
        ),
        "Salbutamol" to MedicationInfo(
            standardDose = 0.3, // mg/kg/day (oral)
            maxDose = 0.5,
            dosesPerDay = 3,
            minAge = 2,
            maxAge = 216,
            route = "Oral/Inhalado",
            conditions = listOf("Asma/Broncoespasmo")
        ),
        "Loratadina" to MedicationInfo(
            standardDose = 0.2, // mg/kg/day
            maxDose = 0.3,
            dosesPerDay = 1,
            minAge = 12,
            maxAge = 216,
            route = "Oral",
            conditions = listOf("Alergia")
        ),
        "Cetirizina" to MedicationInfo(
            standardDose = 0.25, // mg/kg/day
            maxDose = 0.5,
            dosesPerDay = 1,
            minAge = 6,
            maxAge = 216,
            route = "Oral",
            conditions = listOf("Alergia")
        ),
        "Furosemida" to MedicationInfo(
            standardDose = 2.0, // mg/kg/day
            maxDose = 6.0,
            dosesPerDay = 2,
            minAge = 1,
            maxAge = 216,
            route = "Oral/IV",
            conditions = listOf("Edema", "Insuficiencia Cardíaca")
        )
    )

    data class MedicationInfo(
        val standardDose: Double, // mg/kg/day
        val maxDose: Double,
        val dosesPerDay: Int,
        val minAge: Int, // months
        val maxAge: Int,
        val route: String,
        val conditions: List<String>
    )

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate patient weight
        val weightStr = inputs["patient_weight"]
        if (weightStr.isNullOrBlank()) {
            errors.add("El peso del paciente es obligatorio")
        } else {
            val weight = weightStr.toDoubleOrNull()
            if (weight == null) {
                errors.add("El peso debe ser un número válido")
            } else if (weight < 0.5 || weight > 80.0) {
                errors.add("El peso debe estar entre 0.5-80 kg")
            }
        }

        // Validate patient age
        val ageStr = inputs["patient_age_months"]
        if (ageStr.isNullOrBlank()) {
            errors.add("La edad del paciente es obligatoria")
        } else {
            val age = ageStr.toDoubleOrNull()
            if (age == null) {
                errors.add("La edad debe ser un número válido")
            } else if (age < 0 || age > 216) {
                errors.add("La edad debe estar entre 0-216 meses (0-18 años)")
            }
        }

        // Validate medication
        val medication = inputs["medication"]
        if (medication.isNullOrBlank()) {
            errors.add("Debe seleccionar un medicamento")
        }

        // Validate custom dose if "Otro medicamento" is selected
        if (medication == "Otro medicamento") {
            val customDoseStr = inputs["custom_dose_per_kg"]
            if (customDoseStr.isNullOrBlank()) {
                errors.add("Debe especificar la dosis personalizada para 'Otro medicamento'")
            } else {
                val customDose = customDoseStr.toDoubleOrNull()
                if (customDose == null || customDose <= 0 || customDose > 500) {
                    errors.add("La dosis personalizada debe estar entre 0.1-500 mg/kg/día")
                }
            }

            val customDosesStr = inputs["custom_doses_per_day"]
            if (customDosesStr.isNullOrBlank()) {
                errors.add("Debe especificar el número de dosis por día")
            } else {
                val customDoses = customDosesStr.toIntOrNull()
                if (customDoses == null || customDoses < 1 || customDoses > 6) {
                    errors.add("El número de dosis debe estar entre 1-6 por día")
                }
            }
        }

        // Validate concentration if provided
        val concentrationStr = inputs["medication_concentration"]
        if (!concentrationStr.isNullOrBlank()) {
            val concentration = concentrationStr.toDoubleOrNull()
            if (concentration == null || concentration <= 0 || concentration > 1000) {
                errors.add("La concentración debe estar entre 0.1-1000 mg/mL")
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
        val ageMonths = inputs["patient_age_months"]!!.toDouble()
        val medication = inputs["medication"]!!
        val concentration = inputs["medication_concentration"]?.toDoubleOrNull()
        val clinicalCondition = inputs["clinical_condition"] ?: "Otra condición"
        val severity = inputs["severity"] ?: "Moderada"
        val renalFunction = inputs["renal_function"] ?: "Normal"
        val prematureInfant = inputs["premature_infant"] == "true"
        val hasAllergies = inputs["allergies"] == "true"

        // Get medication information
        val (medicationInfo, dosePerKg, dosesPerDay) = if (medication == "Otro medicamento") {
            val customDose = inputs["custom_dose_per_kg"]!!.toDouble()
            val customDoses = inputs["custom_doses_per_day"]!!.toInt()
            Triple(null, customDose, customDoses)
        } else {
            val info = pediatricMedications[medication]
                ?: throw IllegalArgumentException("Medicamento no encontrado: $medication")
            Triple(info, adjustDoseForSeverity(info.standardDose, severity), info.dosesPerDay)
        }

        // Apply age and condition adjustments
        val adjustedDose = applyAgeAdjustments(dosePerKg, ageMonths, prematureInfant, medication)
        val finalDose = applyRenalAdjustments(adjustedDose, renalFunction)

        // Calculate dosages
        val totalDailyDose = finalDose * weight
        val dosePerAdministration = totalDailyDose / dosesPerDay
        val volumePerDose = concentration?.let { dosePerAdministration / it }

        // Generate dosing schedule
        val dosingSchedule = generateDosingSchedule(dosesPerDay)

        // Generate safety warnings
        val safetyWarnings = generateSafetyWarnings(
            medication, medicationInfo, finalDose, ageMonths, weight,
            renalFunction, prematureInfant, hasAllergies
        )

        // Generate administration instructions
        val administrationInstructions = generateAdministrationInstructions(
            medication, medicationInfo, ageMonths, clinicalCondition
        )

        // Generate monitoring recommendations
        val monitoringRecommendations = generateMonitoringRecommendations(
            medication, medicationInfo, ageMonths, renalFunction, severity
        )

        // Generate age-specific warnings
        val ageWarnings = generateAgeSpecificWarnings(medication, ageMonths, prematureInfant)

        // Format results
        val results = mapOf(
            "recommended_dose_per_kg" to String.format("%.1f", finalDose),
            "total_daily_dose" to String.format("%.1f", totalDailyDose),
            "dose_per_administration" to String.format("%.1f", dosePerAdministration),
            "volume_per_dose" to (volumePerDose?.let { String.format("%.2f", it) } ?: "No calculado"),
            "doses_per_day" to dosesPerDay.toString(),
            "dosing_schedule" to dosingSchedule,
            "safety_warnings" to safetyWarnings,
            "administration_instructions" to administrationInstructions,
            "monitoring_recommendations" to monitoringRecommendations,
            "age_appropriate_warnings" to ageWarnings
        )

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun adjustDoseForSeverity(standardDose: Double, severity: String): Double {
        return when (severity) {
            "Leve" -> standardDose * 0.8
            "Severa" -> standardDose * 1.2
            else -> standardDose // Moderada
        }
    }

    private fun applyAgeAdjustments(dose: Double, ageMonths: Double, premature: Boolean, medication: String): Double {
        var adjustedDose = dose

        // Neonatal adjustments (0-1 month)
        if (ageMonths < 1) {
            adjustedDose *= 0.5 // Reduce dose for neonates
        }
        // Infant adjustments (1-12 months)
        else if (ageMonths < 12) {
            adjustedDose *= 0.8 // Slightly reduce for infants
        }

        // Premature infant additional reduction
        if (premature && ageMonths < 3) {
            adjustedDose *= 0.7
        }

        // Medication-specific age adjustments
        when (medication) {
            "Ibuprofeno" -> {
                if (ageMonths < 6) adjustedDose = 0.0 // Contraindicated under 6 months
            }
            "Loratadina" -> {
                if (ageMonths < 12) adjustedDose = 0.0 // Not recommended under 1 year
            }
        }

        return adjustedDose
    }

    private fun applyRenalAdjustments(dose: Double, renalFunction: String): Double {
        return when (renalFunction) {
            "Insuficiencia Leve" -> dose * 0.8
            "Insuficiencia Moderada" -> dose * 0.6
            "Insuficiencia Severa" -> dose * 0.4
            else -> dose
        }
    }

    private fun generateDosingSchedule(dosesPerDay: Int): String {
        return when (dosesPerDay) {
            1 -> "Una vez al día (cada 24 horas)"
            2 -> "Cada 12 horas (8:00 AM y 8:00 PM)"
            3 -> "Cada 8 horas (8:00 AM, 4:00 PM, 12:00 AM)"
            4 -> "Cada 6 horas (6:00 AM, 12:00 PM, 6:00 PM, 12:00 AM)"
            6 -> "Cada 4 horas (6:00 AM, 10:00 AM, 2:00 PM, 6:00 PM, 10:00 PM, 2:00 AM)"
            else -> "Según indicación médica"
        }
    }

    private fun generateSafetyWarnings(
        medication: String, medicationInfo: MedicationInfo?, dose: Double,
        ageMonths: Double, weight: Double, renalFunction: String,
        premature: Boolean, hasAllergies: Boolean
    ): String {
        val warnings = mutableListOf<String>()

        // Dose safety checks
        medicationInfo?.let { info ->
            if (dose > info.maxDose) {
                warnings.add("⚠️ DOSIS ALTA - Excede la dosis máxima recomendada")
            }
            if (ageMonths < info.minAge) {
                warnings.add("⚠️ EDAD MÍNIMA - Medicamento no recomendado para esta edad")
            }
        }

        // Weight-based warnings
        if (weight < 2.5) {
            warnings.add("⚠️ PESO BAJO - Recién nacido de bajo peso, ajustar dosis")
        }

        // Age-specific warnings
        if (ageMonths < 1) {
            warnings.add("⚠️ NEONATO - Dosis reducida aplicada automáticamente")
        }

        if (premature) {
            warnings.add("⚠️ PREMATURO - Dosis ajustada para prematurez")
        }

        // Renal function warnings
        if (renalFunction != "Normal") {
            warnings.add("⚠️ FUNCIÓN RENAL - Dosis ajustada por insuficiencia renal")
        }

        // Medication-specific warnings
        when (medication) {
            "Paracetamol" -> {
                warnings.add("⚠️ No exceder 90 mg/kg/día - Riesgo de hepatotoxicidad")
            }
            "Ibuprofeno" -> {
                warnings.add("⚠️ Administrar con alimentos - Evitar si hay deshidratación")
                if (ageMonths < 6) warnings.add("🚫 CONTRAINDICADO en menores de 6 meses")
            }
            "Amoxicilina" -> {
                warnings.add("⚠️ Verificar alergias a penicilina antes de administrar")
            }
            "Dexametasona" -> {
                warnings.add("⚠️ ESTEROIDE - Uso a corto plazo, monitorear efectos secundarios")
            }
        }

        if (hasAllergies) {
            warnings.add("⚠️ ALERGIAS CONOCIDAS - Verificar compatibilidad medicamentosa")
        }

        // General pediatric warnings
        warnings.add("⚠️ Verificar dosis con otro profesional de salud (doble verificación)")
        warnings.add("⚠️ Usar jeringa o medidor adecuado para la edad")

        return warnings.joinToString("\n")
    }

    private fun generateAdministrationInstructions(
        medication: String, medicationInfo: MedicationInfo?,
        ageMonths: Double, condition: String
    ): String {
        val instructions = mutableListOf<String>()

        instructions.add("💊 INSTRUCCIONES DE ADMINISTRACIÓN:")
        instructions.add("")

        // Age-appropriate administration
        when {
            ageMonths < 6 -> {
                instructions.add("👶 LACTANTE:")
                instructions.add("• Usar jeringa oral de 1-5 mL")
                instructions.add("• Administrar lentamente en la mejilla")
                instructions.add("• Evitar la parte posterior de la lengua")
                instructions.add("• Puede mezclar con pequeña cantidad de leche materna")
            }
            ageMonths < 24 -> {
                instructions.add("🍼 BEBÉ:")
                instructions.add("• Usar jeringa oral o cuchara medidora")
                instructions.add("• Administrar sentado o semi-incorporado")
                instructions.add("• Puede mezclarse con alimento si es necesario")
                instructions.add("• No forzar si rechaza, intentar más tarde")
            }
            ageMonths < 72 -> {
                instructions.add("👦 NIÑO PEQUEÑO:")
                instructions.add("• Usar vaso medidor o cuchara")
                instructions.add("• Explicar que es medicina para sentirse mejor")
                instructions.add("• Ofrecer agua después si acepta")
                instructions.add("• Supervisión de adulto obligatoria")
            }
            else -> {
                instructions.add("🧒 NIÑO MAYOR:")
                instructions.add("• Puede usar vaso medidor")
                instructions.add("• Enseñar la importancia de completar tratamiento")
                instructions.add("• Supervisión de adulto para dosificación")
                instructions.add("• Registrar horarios de administración")
            }
        }

        // Medication-specific instructions
        medicationInfo?.let { info ->
            instructions.add("")
            instructions.add("📋 ESPECÍFICAS PARA ${medication.uppercase()}:")

            when (medication) {
                "Amoxicilina" -> {
                    instructions.add("• Completar todo el tratamiento (7-10 días)")
                    instructions.add("• Puede administrarse con o sin alimentos")
                    instructions.add("• Refrigerar si es suspensión")
                }
                "Paracetamol" -> {
                    instructions.add("• Puede administrarse con o sin alimentos")
                    instructions.add("• No exceder 5 días de uso continuo")
                    instructions.add("• Esperar al menos 4 horas entre dosis")
                }
                "Ibuprofeno" -> {
                    instructions.add("• Administrar SIEMPRE con alimentos")
                    instructions.add("• Asegurar hidratación adecuada")
                    instructions.add("• No usar si hay vómitos o diarrea")
                }
                "Salbutamol" -> {
                    instructions.add("• Enjuagar boca después de inhalación")
                    instructions.add("• Usar cámara espaciadora en menores de 4 años")
                    instructions.add("• Agitar inhalador antes de usar")
                }

                else -> {}
            }
        }

        instructions.add("")
        instructions.add("⏰ HORARIOS:")
        instructions.add("• Mantener horarios regulares")
        instructions.add("• Usar alarmas o recordatorios")
        instructions.add("• Anotar cada dosis administrada")

        return instructions.joinToString("\n")
    }

    private fun generateMonitoringRecommendations(
        medication: String, medicationInfo: MedicationInfo?,
        ageMonths: Double, renalFunction: String, severity: String
    ): String {
        val monitoring = mutableListOf<String>()

        monitoring.add("📊 MONITOREO RECOMENDADO:")
        monitoring.add("")

        // General pediatric monitoring
        monitoring.add("👀 OBSERVACIÓN GENERAL:")
        monitoring.add("• Respuesta clínica a las 24-48 horas")
        monitoring.add("• Signos de mejoría o empeoramiento")
        monitoring.add("• Tolerancia a la medicación")
        monitoring.add("• Efectos secundarios")

        // Age-specific monitoring
        if (ageMonths < 6) {
            monitoring.add("")
            monitoring.add("👶 MONITOREO ESPECIAL LACTANTE:")
            monitoring.add("• Patrón de alimentación")
            monitoring.add("• Irritabilidad o somnolencia")
            monitoring.add("• Vómitos o regurgitación")
            monitoring.add("• Cambios en deposiciones")
        }

        // Medication-specific monitoring
        when (medication) {
            "Paracetamol" -> {
                monitoring.add("")
                monitoring.add("💊 MONITOREO PARACETAMOL:")
                monitoring.add("• Efectividad en reducción de fiebre")
                monitoring.add("• No usar más de 5 días consecutivos")
                monitoring.add("• Vigilar signos de hepatotoxicidad (amarillez)")
            }
            "Ibuprofeno" -> {
                monitoring.add("")
                monitoring.add("💊 MONITOREO IBUPROFENO:")
                monitoring.add("• Hidratación adecuada")
                monitoring.add("• Dolor abdominal o vómitos")
                monitoring.add("• Función renal si uso prolongado")
            }
            "Amoxicilina" -> {
                monitoring.add("")
                monitoring.add("💊 MONITOREO ANTIBIÓTICO:")
                monitoring.add("• Mejoría de síntomas en 48-72 horas")
                monitoring.add("• Erupciones cutáneas (alergia)")
                monitoring.add("• Diarrea (cambio de flora intestinal)")
                monitoring.add("• Completar tratamiento aunque mejore")
            }
            "Dexametasona" -> {
                monitoring.add("")
                monitoring.add("💊 MONITOREO ESTEROIDE:")
                monitoring.add("• Respuesta respiratoria")
                monitoring.add("• Cambios de comportamiento")
                monitoring.add("• Aumento de apetito/sed")
                monitoring.add("• Uso por tiempo limitado")
            }
        }

        // Renal function monitoring
        if (renalFunction != "Normal") {
            monitoring.add("")
            monitoring.add("🔬 MONITOREO FUNCIÓN RENAL:")
            monitoring.add("• Diuresis adecuada")
            monitoring.add("• Signos de retención de líquidos")
            monitoring.add("• Consulta nefrológica si empeora")
        }

        // When to contact healthcare provider
        monitoring.add("")
        monitoring.add("🚨 CONTACTAR AL MÉDICO SI:")
        monitoring.add("• Vómitos persistentes (no retiene medicación)")
        monitoring.add("• Fiebre que no cede después de 48 horas")
        monitoring.add("• Erupciones cutáneas o hinchazón")
        monitoring.add("• Dificultad respiratoria")
        monitoring.add("• Cambios significativos en comportamiento")
        monitoring.add("• Empeoramiento de síntomas")

        return monitoring.joinToString("\n")
    }

    private fun generateAgeSpecificWarnings(medication: String, ageMonths: Double, premature: Boolean): String {
        val warnings = mutableListOf<String>()

        warnings.add("👶 CONSIDERACIONES POR EDAD:")
        warnings.add("")

        when {
            ageMonths < 1 -> {
                warnings.add("🍼 RECIÉN NACIDO (0-1 mes):")
                warnings.add("• Metabolismo hepático inmaduro")
                warnings.add("• Función renal reducida")
                warnings.add("• Mayor riesgo de efectos secundarios")
                warnings.add("• Monitoreo hospitalario recomendado")
            }
            ageMonths < 6 -> {
                warnings.add("👶 LACTANTE (1-6 meses):")
                warnings.add("• Sistema inmune en desarrollo")
                warnings.add("• Cuidado con medicamentos que afecten GI")
                warnings.add("• Preferir formulaciones líquidas")
                warnings.add("• Evitar miel como excipiente")
            }
            ageMonths < 24 -> {
                warnings.add("🍼 BEBÉ (6-24 meses):")
                warnings.add("• Fase de mayor crecimiento")
                warnings.add("• Ajustes frecuentes de dosis por peso")
                warnings.add("• Cuidado con saborizantes artificiales")
                warnings.add("• Supervisión constante de administración")
            }
            ageMonths < 72 -> {
                warnings.add("👦 PREESCOLAR (2-6 años):")
                warnings.add("• Puede rechazar medicación por sabor")
                warnings.add("• Explicaciones simples sobre el tratamiento")
                warnings.add("• Usar técnicas de distracción si es necesario")
                warnings.add("• Comenzar educación sobre medicamentos")
            }
            else -> {
                warnings.add("🧒 ESCOLAR (6+ años):")
                warnings.add("• Puede participar en su tratamiento")
                warnings.add("• Enseñar importancia de adherencia")
                warnings.add("• Supervisión adulta aún necesaria")
                warnings.add("• Preparar para transición a adolescencia")
            }
        }

        if (premature) {
            warnings.add("")
            warnings.add("⚠️ PREMATUREZ:")
            warnings.add("• Órganos menos maduros")
            warnings.add("• Mayor susceptibilidad a efectos adversos")
            warnings.add("• Posible necesidad de ajustes adicionales")
            warnings.add("• Seguimiento especializado")
        }

        // Medication-specific age warnings
        when (medication) {
            "Ibuprofeno" -> {
                if (ageMonths < 6) {
                    warnings.add("")
                    warnings.add("🚫 IBUPROFENO:")
                    warnings.add("• CONTRAINDICADO en menores de 6 meses")
                    warnings.add("• Usar paracetamol como alternativa")
                }
            }
            "Loratadina" -> {
                if (ageMonths < 12) {
                    warnings.add("")
                    warnings.add("⚠️ LORATADINA:")
                    warnings.add("• No recomendado en menores de 1 año")
                    warnings.add("• Considerar antihistamínicos alternativos")
                }
            }
        }

        return warnings.joinToString("\n")
    }

    override fun getInterpretation(result: CalculationResult): String {
        val medication = result.inputValues["medication"] ?: ""
        val weight = result.inputValues["patient_weight"] ?: ""
        val ageMonths = result.inputValues["patient_age_months"] ?: ""
        val totalDose = result.resultValues["total_daily_dose"] ?: ""
        val dosePerAdmin = result.resultValues["dose_per_administration"] ?: ""
        val dosesPerDay = result.resultValues["doses_per_day"] ?: ""

        val ageYears = (ageMonths.toDoubleOrNull() ?: 0.0) / 12

        return """
INTERPRETACIÓN CLÍNICA - DOSIFICACIÓN PEDIÁTRICA

MEDICAMENTO: $medication
PACIENTE: ${String.format("%.1f", ageYears)} años (${ageMonths} meses), $weight kg

DOSIFICACIÓN CALCULADA:
• Dosis total diaria: $totalDose mg/día
• Dosis por administración: $dosePerAdmin mg
• Frecuencia: $dosesPerDay veces al día

FÓRMULA UTILIZADA:
Dosis total = Dosis recomendada (mg/kg/día) × Peso (kg)
Dosis por toma = Dosis total ÷ Número de dosis por día

PRINCIPIOS DE DOSIFICACIÓN PEDIÁTRICA:
• Ajuste por peso corporal (mg/kg)
• Consideración de madurez orgánica
• Factores de seguridad adicionales
• Formulaciones apropiadas para la edad

FUENTES MEXICANAS:
• Guía de Práctica Clínica: Farmacología en Pediatría
• Secretaría de Salud México (gpc.salud.gob.mx)
• Instituto Nacional de Pediatría
• Normas farmacológicas pediátricas mexicanas

CONSIDERACIONES ESPECIALES:
• Verificación obligatoria por doble personal
• Uso de jeringas/medidores apropiados para edad
• Supervisión parental en administración
• Monitoreo de efectividad y efectos adversos

LIMITACIONES:
• Dosis calculadas son orientativas
• Requieren validación médica profesional
• Factores individuales pueden requerir ajustes
• Seguimiento clínico obligatorio
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "Guía de Práctica Clínica: Farmacología en Pediatría",
                source = "Secretaría de Salud México",
                url = "http://gpc.salud.gob.mx"
            ),
            Reference(
                title = "Manual de Dosificación Pediátrica",
                source = "Instituto Nacional de Pediatría",
                year = 2023
            ),
            Reference(
                title = "Farmacología Pediátrica Clínica",
                source = "Hospital Infantil de México Federico Gómez",
                year = 2022
            ),
            Reference(
                title = "Guías de Prescripción Segura en Pediatría",
                source = "Instituto Mexicano del Seguro Social (IMSS)",
                year = 2023
            ),
            Reference(
                title = "Pediatric Drug Dosing Guidelines",
                source = "American Academy of Pediatrics",
                year = 2022
            )
        )
    }
}