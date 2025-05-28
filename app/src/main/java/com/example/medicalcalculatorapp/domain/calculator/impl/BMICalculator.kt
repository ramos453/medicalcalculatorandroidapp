package com.example.medicalcalculatorapp.domain.calculator.impl

import com.example.medicalcalculatorapp.domain.calculator.Calculator
import com.example.medicalcalculatorapp.domain.calculator.Reference
import com.example.medicalcalculatorapp.domain.calculator.ValidationResult
import com.example.medicalcalculatorapp.domain.model.CalculationResult
import kotlin.math.pow

class BMICalculator : Calculator {

    override val calculatorId = "bmi_calculator"

    override fun validate(inputs: Map<String, String>): ValidationResult {
        val errors = mutableListOf<String>()

        // Validate height
        val heightStr = inputs["height"]
        if (heightStr.isNullOrBlank()) {
            errors.add("La estatura es obligatoria")
        } else {
            val height = heightStr.toDoubleOrNull()
            if (height == null) {
                errors.add("La estatura debe ser un número válido")
            } else if (height < 50.0 || height > 250.0) {
                errors.add("La estatura debe estar entre 50-250 cm")
            }
        }

        // Validate weight
        val weightStr = inputs["weight"]
        if (weightStr.isNullOrBlank()) {
            errors.add("El peso es obligatorio")
        } else {
            val weight = weightStr.toDoubleOrNull()
            if (weight == null) {
                errors.add("El peso debe ser un número válido")
            } else if (weight < 3.0 || weight > 300.0) {
                errors.add("El peso debe estar entre 3-300 kg")
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
        val heightCm = inputs["height"]!!.toDouble()
        val weight = inputs["weight"]!!.toDouble()

        // Convert height to meters
        val heightM = heightCm / 100.0

        // Calculate BMI
        val bmi = weight / (heightM.pow(2))

        // Determine category according to IMSS classification
        val category = when {
            bmi < 18.5 -> "Bajo peso"
            bmi < 25.0 -> "Peso normal"
            bmi < 30.0 -> "Sobrepeso"
            bmi < 35.0 -> "Obesidad grado I"
            bmi < 40.0 -> "Obesidad grado II"
            else -> "Obesidad grado III"
        }

        // Generate health recommendations
        val healthRecommendations = generateHealthRecommendations(bmi, category)

        // Calculate healthy weight range
        val weightRange = calculateHealthyWeightRange(heightM)

        // Format results
        val results = mapOf(
            "bmi" to String.format("%.1f", bmi),
            "category" to category,
            "health_recommendations" to healthRecommendations,
            "weight_range" to weightRange
        )

        return CalculationResult(
            calculatorId = calculatorId,
            inputValues = inputs,
            resultValues = results
        )
    }

    private fun generateHealthRecommendations(bmi: Double, category: String): String {
        val recommendations = mutableListOf<String>()

        when (category) {
            "Bajo peso" -> {
                recommendations.add("CONSULTA MÉDICA para evaluación nutricional")
                recommendations.add("Incrementar ingesta calórica saludable")
                recommendations.add("Considerar suplementos nutricionales")
                recommendations.add("Ejercicio de fortalecimiento muscular")
            }
            "Peso normal" -> {
                recommendations.add("MANTENER peso actual con dieta equilibrada")
                recommendations.add("Ejercicio regular 150 min/semana")
                recommendations.add("Controles médicos anuales de rutina")
                recommendations.add("Hidratación adecuada 2-3 L/día")
            }
            "Sobrepeso" -> {
                recommendations.add("REDUCIR peso 5-10% en 6 meses")
                recommendations.add("Dieta hipocalórica supervisada")
                recommendations.add("Ejercicio aeróbico 300 min/semana")
                recommendations.add("Control médico cada 3 meses")
            }
            "Obesidad grado I" -> {
                recommendations.add("CONSULTA NUTRICIONAL urgente")
                recommendations.add("Reducir peso 10-15% gradualmente")
                recommendations.add("Ejercicio supervisado y progresivo")
                recommendations.add("Evaluar factores de riesgo cardiovascular")
            }
            "Obesidad grado II" -> {
                recommendations.add("MANEJO MÉDICO ESPECIALIZADO")
                recommendations.add("Evaluar cirugía bariátrica")
                recommendations.add("Control de diabetes e hipertensión")
                recommendations.add("Seguimiento psicológico")
            }
            "Obesidad grado III" -> {
                recommendations.add("URGENTE: Evaluación bariátrica")
                recommendations.add("Manejo multidisciplinario inmediato")
                recommendations.add("Control metabólico estricto")
                recommendations.add("Monitoreo cardiológico")
            }
        }

        return recommendations.joinToString("\n")
    }

    private fun calculateHealthyWeightRange(heightM: Double): String {
        val minHealthyWeight = 18.5 * (heightM.pow(2))
        val maxHealthyWeight = 24.9 * (heightM.pow(2))

        return "${String.format("%.1f", minHealthyWeight)} - ${String.format("%.1f", maxHealthyWeight)} kg"
    }

    override fun getInterpretation(result: CalculationResult): String {
        val bmi = result.resultValues["bmi"] ?: ""
        val category = result.resultValues["category"] ?: ""
        val weightRange = result.resultValues["weight_range"] ?: ""
        val height = result.inputValues["height"] ?: ""
        val weight = result.inputValues["weight"] ?: ""

        return """
INTERPRETACIÓN CLÍNICA - ÍNDICE DE MASA CORPORAL

IMC CALCULADO: $bmi kg/m²
CATEGORÍA IMSS: $category
PESO ACTUAL: $weight kg
ESTATURA: $height cm
RANGO SALUDABLE: $weightRange

CLASIFICACIÓN IMSS:
• Bajo peso: <18.5 kg/m²
• Peso normal: 18.5-24.9 kg/m²
• Sobrepeso: 25.0-29.9 kg/m²
• Obesidad I: 30.0-34.9 kg/m²
• Obesidad II: 35.0-39.9 kg/m²
• Obesidad III: ≥40.0 kg/m²

FÓRMULA UTILIZADA:
IMC = Peso (kg) ÷ [Estatura (m)]²
IMC = $weight ÷ [${String.format("%.2f", (height.toDouble()/100))}]² = $bmi

EVALUACIÓN CLÍNICA:
El IMC es un indicador de masa corporal que correlaciona con grasa corporal y riesgos de salud. Valores fuera del rango normal requieren evaluación médica y modificaciones del estilo de vida.

LIMITACIONES:
• No distingue entre masa muscular y grasa
• Puede sobreestimar obesidad en atletas
• Subestima riesgo en adultos mayores
• Requiere evaluación clínica complementaria
        """.trimIndent()
    }

    override fun getReferences(): List<Reference> {
        return listOf(
            Reference(
                title = "Clasificación del IMC",
                source = "Instituto Mexicano del Seguro Social (IMSS)",
                year = 2023
            ),
            Reference(
                title = "Evaluación Nutricional en Adultos",
                source = "Norma Oficial Mexicana NOM-043-SSA2-2012",
                year = 2012
            ),
            Reference(
                title = "Obesidad y Factores de Riesgo Cardiovascular",
                source = "SciELO México - Revista Médica",
                year = 2022
            ),
            Reference(
                title = "Body Mass Index Guidelines",
                source = "World Health Organization",
                year = 2023
            ),
            Reference(
                title = "Manejo Integral de la Obesidad",
                source = "Secretaría de Salud México",
                year = 2023
            )
        )
    }
}