package com.example.medicalcalculatorapp.data.db.util

import android.content.Context
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
import com.example.medicalcalculatorapp.data.db.entity.CalculatorEntity
import com.example.medicalcalculatorapp.data.db.entity.CategoryEntity
import com.example.medicalcalculatorapp.data.db.entity.FieldEntity
import com.example.medicalcalculatorapp.domain.model.FieldType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first  // Add this import instead of firstOrNull


object DatabasePrepopulateUtil {

    suspend fun prepopulateDatabase(context: Context, database: MedicalCalculatorDatabase) {
        withContext(Dispatchers.IO) {
            // First, insert categories
            val categories = createInitialCategories()
            database.categoryDao().insertCategories(categories)

            // Then, insert calculators
            val calculators = createInitialCalculators()
            database.calculatorDao().insertCalculators(calculators)

            // Finally, insert fields for each calculator
            val allFields = createInitialFields()
            database.fieldDao().insertFields(allFields)
        }
    }

    suspend fun prepopulateIfNeeded(context: Context, database: MedicalCalculatorDatabase) {
        withContext(Dispatchers.IO) {
            try {
                // Check if database is already populated
                val existingCalculators = database.calculatorDao().getAllCalculators().first()

                if (existingCalculators.isNullOrEmpty()) {
                    // Database is empty, populate it
                    prepopulateDatabase(context, database)
                    println("✅ Database populated successfully with ${existingCalculators?.size ?: 0} calculators")
                } else {
                    println("✅ Database already populated with ${existingCalculators.size} calculators")
                }
            } catch (e: Exception) {
                println("❌ Error checking/populating database: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun createInitialCategories(): List<CategoryEntity> {
        return listOf(
            CategoryEntity(
                id = "dosing_medication",
                name = "Dosificación y Medicación",
                description = "Administración segura y precisa de fármacos",
                iconResId = R.drawable.ic_launcher_foreground,
                displayOrder = 0
            ),
            CategoryEntity(
                id = "fluids_electrolytes",
                name = "Fluidos y Electrolitos",
                description = "Terapia de fluidos y balance electrolítico",
                iconResId = R.drawable.ic_launcher_foreground,
                displayOrder = 1
            ),
            CategoryEntity(
                id = "vital_signs",
                name = "Monitoreo de Signos Vitales",
                description = "Estado hemodinámico y nutricional del paciente",
                iconResId = R.drawable.ic_launcher_foreground,
                displayOrder = 2
            ),
            CategoryEntity(
                id = "clinical_scales",
                name = "Escalas de Valoración Clínica",
                description = "Evaluación de riesgos y niveles de atención",
                iconResId = R.drawable.ic_launcher_foreground,
                displayOrder = 3
            ),
            CategoryEntity(
                id = "pediatric_neonatal",
                name = "Pediatría y Neonatología",
                description = "Pacientes pediátricos y neonatos",
                iconResId = R.drawable.ic_launcher_foreground,
                displayOrder = 4
            )
        )
    }

    private fun createInitialCalculators(): List<CalculatorEntity> {
        return listOf(
            // 1. MEDICATION DOSAGE CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "medication_dosage",
                name = "Calculadora de Dosis de Medicamentos",
                description = "Cálculos de dosis (mg/kg), unidades y concentraciones",
                categoryId = "dosing_medication"
            ),

            // 2. HEPARIN DOSAGE CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "heparin_dosage",
                name = "Calculadora de Dosis de Heparina",
                description = "HBPM y aPTT - Anticoagulación especializada",
                categoryId = "dosing_medication"
            ),

            // 3. UNIT CONVERTER CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "unit_converter",
                name = "Conversor de Unidades",
                description = "mg⇄mL, mEq⇄mg y otras conversiones médicas",
                categoryId = "dosing_medication"
            ),

            // 4. IV DRIP RATE CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "iv_drip_rate",
                name = "Velocidad de Goteo IV",
                description = "Cálculo de gtt/min y mL/h para terapia intravenosa",
                categoryId = "fluids_electrolytes"
            ),

            // 5. FLUID BALANCE CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "fluid_balance",
                name = "Balance Hídrico 24h",
                description = "Ingresos y egresos en 24 horas",
                categoryId = "fluids_electrolytes"
            ),

            // 6. ELECTROLYTE MANAGEMENT CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "electrolyte_management",
                name = "Gestión de Fluidos y Electrolitos",
                description = "Reemplazo de potasio, sodio y otros electrolitos",
                categoryId = "fluids_electrolytes"
            ),

            // 7. BMI CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "bmi_calculator",
                name = "Índice de Masa Corporal (IMC)",
                description = "Evaluación nutricional: peso ÷ talla²",
                categoryId = "vital_signs"
            ),

            // 8. MAP CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "map_calculator",
                name = "Presión Arterial Media (PAM)",
                description = "Cálculo hemodinámico: (PAS + 2×PAD)/3",
                categoryId = "vital_signs"
            ),

            // 9. MINUTE VENTILATION CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "minute_ventilation",
                name = "Calculadora de Ventilación Minuto",
                description = "Cálculo de VE = FR × VT para evaluación ventilatoria",
                categoryId = "vital_signs"
            ),
            // 10. BRADEN SCALE CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "braden_scale",
                name = "Escala de Braden",
                description = "Evaluación de riesgo de úlceras por presión",
                categoryId = "clinical_scales"
            ),
            // 11. GLASGOW SCALE CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "glasgow_coma_scale",
                name = "Escala de Coma de Glasgow",
                description = "Valoración del nivel de conciencia y función neurológica",
                categoryId = "clinical_scales"
            ),
            // 12. PEDIATRIC DOSAGE CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "pediatric_dosage",
                name = "Dosis Pediátrica",
                description = "Dosificación (mg/kg) según peso y edad",
                categoryId = "pediatric_neonatal"
            ),
            // 13. APGAR SCORE CALCULATOR ✅ IMPLEMENTED
            CalculatorEntity(
                id = "apgar_score",
                name = "Puntuación APGAR",
                description = "Evaluación inmediata del recién nacido",
                categoryId = "pediatric_neonatal"
            )
        )
    }


    private fun createInitialFields(): List<FieldEntity> {
        val fields = mutableListOf<FieldEntity>()

        // ==============================================
        // 1. MEDICATION DOSAGE CALCULATOR
        // ==============================================
        fields.addAll(listOf(
            // Input fields
            FieldEntity(
                calculatorId = "medication_dosage",
                id = "patient_weight",
                name = "Peso del Paciente",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "kg",
                minValue = 0.5,
                maxValue = 250.0,
                defaultValue = "70",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "medication_dosage",
                id = "dose_per_kg",
                name = "Dosis por kg",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mg/kg",
                minValue = 0.01,
                maxValue = 100.0,
                defaultValue = "10",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "medication_dosage",
                id = "concentration",
                name = "Concentración",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mg/mL",
                minValue = 0.01,
                maxValue = 1000.0,
                defaultValue = "50",
                displayOrder = 2
            ),
            // Output fields
            FieldEntity(
                calculatorId = "medication_dosage",
                id = "total_dose",
                name = "Dosis Total",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mg",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "medication_dosage",
                id = "volume_to_administer",
                name = "Volumen a Administrar",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mL",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "medication_dosage",
                id = "safety_check",
                name = "Verificación de Seguridad",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 2
            )
        ))


// ==============================================
// 2. HEPARIN DOSAGE CALCULATOR (Updated)
// ==============================================
        fields.addAll(listOf(
            // Input fields
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "patient_weight",
                name = "Peso del Paciente",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "kg",
                minValue = 3.0,
                maxValue = 200.0,
                defaultValue = "70",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "treatment_type",
                name = "Tipo de Tratamiento",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["Profiláctico", "Terapéutico"]""",
                defaultValue = "Profiláctico",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "dosing_schedule",
                name = "Esquema de Dosificación (solo terapéutico)",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 mg/kg cada 12h", "1.5 mg/kg cada 24h"]""",
                defaultValue = "1 mg/kg cada 12h",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "high_bleeding_risk",
                name = "Alto Riesgo Hemorrágico",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "renal_insufficiency",
                name = "Insuficiencia Renal (ClCr <30 mL/min)",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "elderly_patient",
                name = "Paciente >75 años",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "drug_concentration",
                name = "Concentración del Medicamento (opcional)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mg/mL",
                minValue = 1.0,
                maxValue = 200.0,
                defaultValue = "100", // Common enoxaparin concentration
                displayOrder = 6
            ),

            // Output fields
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "recommended_dose",
                name = "Dosis Recomendada",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mg",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "administration_frequency",
                name = "Frecuencia de Administración",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "volume_to_administer",
                name = "Volumen a Administrar",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mL",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "safety_warnings",
                name = "Advertencias de Seguridad",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "heparin_dosage",
                id = "monitoring_recommendations",
                name = "Recomendaciones de Monitoreo",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 4
            )
        ))



// ==============================================
// 3. UNIT CONVERTER CALCULATOR
// ==============================================
        fields.addAll(listOf(
            // Input fields
            FieldEntity(
                calculatorId = "unit_converter",
                id = "conversion_type",
                name = "Tipo de Conversión",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["mg → mL", "mL → mg", "mEq → mg", "mg → mEq", "mcg → mg", "mg → mcg", "Unidades → mL"]""",
                defaultValue = "mg → mL",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "unit_converter",
                id = "input_value",
                name = "Valor a Convertir",
                type = FieldType.NUMBER.name,
                isInputField = true,
                minValue = 0.001,
                maxValue = 999999.0,
                defaultValue = "100",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "unit_converter",
                id = "concentration",
                name = "Concentración (mg/mL o U/mL)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mg/mL",
                minValue = 0.001,
                maxValue = 10000.0,
                defaultValue = "50",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "unit_converter",
                id = "substance_for_meq",
                name = "Sustancia (solo para mEq)",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["KCl (Cloruro de Potasio)", "NaCl (Cloruro de Sodio)", "CaCl2 (Cloruro de Calcio)", "MgSO4 (Sulfato de Magnesio)", "NaHCO3 (Bicarbonato de Sodio)"]""",
                defaultValue = "KCl (Cloruro de Potasio)",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "unit_converter",
                id = "insulin_type",
                name = "Tipo de Insulina (solo para Unidades)",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Insulina Regular (100 U/mL)", "Insulina NPH (100 U/mL)", "Insulina Rápida (100 U/mL)", "Insulina Lenta (40 U/mL)"]""",
                defaultValue = "Insulina Regular (100 U/mL)",
                displayOrder = 4
            ),

            // Output fields
            FieldEntity(
                calculatorId = "unit_converter",
                id = "converted_value",
                name = "Valor Convertido",
                type = FieldType.NUMBER.name,
                isInputField = false,
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "unit_converter",
                id = "output_unit",
                name = "Unidad de Salida",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "unit_converter",
                id = "conversion_formula",
                name = "Fórmula Utilizada",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "unit_converter",
                id = "clinical_notes",
                name = "Notas Clínicas",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "unit_converter",
                id = "equivalent_weight_info",
                name = "Información de Peso Equivalente",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 4
            )
        ))


// ==============================================
// 4. IV DRIP RATE CALCULATOR (Updated)
// ==============================================
        fields.addAll(listOf(
            // Input fields
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "total_volume",
                name = "Volumen Total a Administrar",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 1.0,
                maxValue = 5000.0,
                defaultValue = "1000",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "infusion_time_hours",
                name = "Tiempo de Infusión",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "horas",
                minValue = 0.1,
                maxValue = 48.0,
                defaultValue = "8",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "drop_factor_type",
                name = "Tipo de Equipo de Goteo",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["Macrogotero", "Microgotero"]""",
                defaultValue = "Macrogotero",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "drop_factor",
                name = "Factor de Goteo",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["10 gtt/mL", "15 gtt/mL", "20 gtt/mL", "60 gtt/mL (microgotero)"]""",
                defaultValue = "20 gtt/mL",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "fluid_type",
                name = "Tipo de Fluido (opcional)",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Solución Salina 0.9%", "Dextrosa 5%", "Lactato de Ringer", "Solución Mixta", "Medicamento Diluido", "Sangre/Hemoderivados", "Otro"]""",
                defaultValue = "Solución Salina 0.9%",
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "patient_weight",
                name = "Peso del Paciente (opcional)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "kg",
                minValue = 1.0,
                maxValue = 200.0,
                defaultValue = "70",
                displayOrder = 5
            ),

            // Output fields
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "drip_rate",
                name = "Velocidad de Goteo",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "gtt/min",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "flow_rate",
                name = "Velocidad de Flujo",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mL/h",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "drops_per_15_seconds",
                name = "Gotas en 15 segundos",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "gtt",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "infusion_duration",
                name = "Duración Total",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "safety_warnings",
                name = "Advertencias de Seguridad",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "monitoring_guidelines",
                name = "Guías de Monitoreo",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 5
            )
        ))


// ==============================================
// 5. FLUID BALANCE CALCULATOR (24 HOURS)
// ==============================================
        fields.addAll(listOf(
            // INTAKE INPUT FIELDS
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "oral_intake",
                name = "Ingesta Oral",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 0.0,
                maxValue = 10000.0,
                defaultValue = "1500",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "iv_fluids",
                name = "Fluidos Intravenosos",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 0.0,
                maxValue = 10000.0,
                defaultValue = "2000",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "enteral_feeding",
                name = "Alimentación Enteral",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 0.0,
                maxValue = 5000.0,
                defaultValue = "0",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "medications_fluids",
                name = "Fluidos con Medicamentos",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 0.0,
                maxValue = 2000.0,
                defaultValue = "200",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "other_intake",
                name = "Otros Ingresos",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 0.0,
                maxValue = 3000.0,
                defaultValue = "0",
                displayOrder = 4
            ),

            // OUTPUT INPUT FIELDS
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "urine_output",
                name = "Diuresis",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 0.0,
                maxValue = 8000.0,
                defaultValue = "1500",
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "vomit",
                name = "Vómitos",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 0.0,
                maxValue = 3000.0,
                defaultValue = "0",
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "drainage",
                name = "Drenajes",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 0.0,
                maxValue = 5000.0,
                defaultValue = "0",
                displayOrder = 7
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "diarrhea",
                name = "Diarrea",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 0.0,
                maxValue = 3000.0,
                defaultValue = "0",
                displayOrder = 8
            ),

            // PATIENT FACTORS FOR INSENSIBLE LOSSES
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "patient_weight",
                name = "Peso del Paciente",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "kg",
                minValue = 1.0,
                maxValue = 200.0,
                defaultValue = "70",
                displayOrder = 9
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "has_fever",
                name = "Fiebre (>37.5°C)",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 10
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "temperature",
                name = "Temperatura Corporal",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "°C",
                minValue = 35.0,
                maxValue = 42.0,
                defaultValue = "36.5",
                displayOrder = 11
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "on_mechanical_ventilation",
                name = "Ventilación Mecánica",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 12
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "hyperventilation",
                name = "Hiperventilación",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 13
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "environmental_factors",
                name = "Factores Ambientales",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Normal", "Calor Extremo", "Fototerapia", "Incubadora", "Ambiente Seco"]""",
                defaultValue = "Normal",
                displayOrder = 14
            ),

            // OUTPUT FIELDS
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "total_intake",
                name = "Ingresos Totales",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mL",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "total_output",
                name = "Egresos Totales",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mL",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "insensible_losses",
                name = "Pérdidas Insensibles Calculadas",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mL",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "fluid_balance",
                name = "Balance Hídrico Neto",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mL",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "balance_interpretation",
                name = "Interpretación del Balance",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "intake_breakdown",
                name = "Desglose de Ingresos",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "output_breakdown",
                name = "Desglose de Egresos",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "fluid_balance",
                id = "clinical_recommendations",
                name = "Recomendaciones Clínicas",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 7
            )
        ))

        // Add this to the createInitialFields() function in DatabasePrepopulateUtil.kt
// Replace or add the electrolyte_management fields:

// ==============================================
// 6. ELECTROLYTE MANAGEMENT CALCULATOR
// ==============================================
        fields.addAll(listOf(
            // PATIENT INFORMATION
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "patient_weight",
                name = "Peso del Paciente",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "kg",
                minValue = 1.0,
                maxValue = 200.0,
                defaultValue = "70",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "patient_age",
                name = "Edad del Paciente",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "años",
                minValue = 0.1,
                maxValue = 120.0,
                defaultValue = "45",
                displayOrder = 1
            ),

            // SODIUM MANAGEMENT
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "current_sodium",
                name = "Sodio Sérico Actual",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mEq/L",
                minValue = 100.0,
                maxValue = 180.0,
                defaultValue = "130",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "target_sodium",
                name = "Sodio Deseado",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mEq/L",
                minValue = 135.0,
                maxValue = 145.0,
                defaultValue = "140",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "correction_time_hours",
                name = "Tiempo de Corrección",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "horas",
                minValue = 6.0,
                maxValue = 48.0,
                defaultValue = "24",
                displayOrder = 4
            ),

            // POTASSIUM MANAGEMENT
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "current_potassium",
                name = "Potasio Sérico Actual",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mEq/L",
                minValue = 1.5,
                maxValue = 6.0,
                defaultValue = "3.0",
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "target_potassium",
                name = "Potasio Deseado",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mEq/L",
                minValue = 3.5,
                maxValue = 5.0,
                defaultValue = "4.0",
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "potassium_route",
                name = "Vía de Administración K+",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["Vía Oral", "Vía Intravenosa"]""",
                defaultValue = "Vía Intravenosa",
                displayOrder = 7
            ),

            // CLINICAL FACTORS
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "renal_function",
                name = "Función Renal",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Normal", "Insuficiencia Leve", "Insuficiencia Moderada", "Insuficiencia Severa", "Diálisis"]""",
                defaultValue = "Normal",
                displayOrder = 8
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "cardiac_status",
                name = "Estado Cardíaco",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Normal", "Arritmias", "Insuficiencia Cardíaca", "Monitoreo Cardíaco"]""",
                defaultValue = "Normal",
                displayOrder = 9
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "diuretic_use",
                name = "Uso de Diuréticos",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 10
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "neurological_symptoms",
                name = "Síntomas Neurológicos",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 11
            ),

            // OUTPUT FIELDS
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "sodium_deficit",
                name = "Déficit de Sodio",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mEq",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "sodium_replacement_rate",
                name = "Velocidad de Reemplazo Na+",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mEq/h",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "sodium_solution_volume",
                name = "Volumen de Solución Salina",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mL",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "potassium_deficit",
                name = "Déficit de Potasio",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mEq",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "potassium_dose",
                name = "Dosis de Potasio",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mEq",
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "potassium_infusion_rate",
                name = "Velocidad de Infusión K+",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mEq/h",
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "safety_warnings",
                name = "Advertencias de Seguridad",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "monitoring_protocol",
                name = "Protocolo de Monitoreo",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 7
            ),
            FieldEntity(
                calculatorId = "electrolyte_management",
                id = "solution_recommendations",
                name = "Recomendaciones de Soluciones",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 8
            )
        ))

        // ==============================================
        // 7. BMI CALCULATOR (Updated)
        // ==============================================
        fields.addAll(listOf(
            // Input fields
            FieldEntity(
                calculatorId = "bmi_calculator",
                id = "height",
                name = "Estatura",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "cm",
                minValue = 50.0,
                maxValue = 250.0,
                defaultValue = "170",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "bmi_calculator",
                id = "weight",
                name = "Peso",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "kg",
                minValue = 3.0,
                maxValue = 300.0,
                defaultValue = "70",
                displayOrder = 1
            ),

            // Output fields
            FieldEntity(
                calculatorId = "bmi_calculator",
                id = "bmi",
                name = "IMC",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "kg/m²",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "bmi_calculator",
                id = "category",
                name = "Categoría",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "bmi_calculator",
                id = "health_recommendations",
                name = "Recomendaciones de Salud",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "bmi_calculator",
                id = "weight_range",
                name = "Rango de Peso Saludable",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 3
            )
        ))


        // ==============================================
        // 8. MAP CALCULATOR (Updated)
        // ==============================================
        fields.addAll(listOf(
            // Input fields
            FieldEntity(
                calculatorId = "map_calculator",
                id = "systolic_bp",
                name = "Presión Sistólica (PAS)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mmHg",
                minValue = 50.0,
                maxValue = 250.0,
                defaultValue = "120",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "map_calculator",
                id = "diastolic_bp",
                name = "Presión Diastólica (PAD)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mmHg",
                minValue = 30.0,
                maxValue = 150.0,
                defaultValue = "80",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "map_calculator",
                id = "patient_age",
                name = "Edad del Paciente (opcional)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "años",
                minValue = 1.0,
                maxValue = 120.0,
                defaultValue = "45",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "map_calculator",
                id = "clinical_context",
                name = "Contexto Clínico",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Paciente Estable", "Cuidados Intensivos", "Postoperatorio", "Emergencia", "Choque"]""",
                defaultValue = "Paciente Estable",
                displayOrder = 3
            ),

            // Output fields
            FieldEntity(
                calculatorId = "map_calculator",
                id = "map",
                name = "Presión Arterial Media",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mmHg",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "map_calculator",
                id = "map_interpretation",
                name = "Interpretación",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "map_calculator",
                id = "clinical_recommendations",
                name = "Recomendaciones Clínicas",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "map_calculator",
                id = "perfusion_status",
                name = "Estado de Perfusión",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 3
            )
        ))

        // ==============================================
        // 10. BRADEN SCALE CALCULATOR
        // ==============================================
        fields.addAll(listOf(
            // INPUT FIELDS - The 6 Braden Scale Components
            FieldEntity(
                calculatorId = "braden_scale",
                id = "sensory_perception",
                name = "Percepción Sensorial",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 - Completamente limitada", "2 - Muy limitada", "3 - Ligeramente limitada", "4 - Sin alteraciones"]""",
                defaultValue = "4 - Sin alteraciones",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "moisture",
                name = "Exposición a la Humedad",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 - Constantemente húmeda", "2 - Muy húmeda", "3 - Ocasionalmente húmeda", "4 - Raramente húmeda"]""",
                defaultValue = "4 - Raramente húmeda",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "activity",
                name = "Actividad",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 - Encamado", "2 - En silla", "3 - Camina ocasionalmente", "4 - Camina frecuentemente"]""",
                defaultValue = "4 - Camina frecuentemente",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "mobility",
                name = "Movilidad",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 - Completamente inmóvil", "2 - Muy limitada", "3 - Ligeramente limitada", "4 - Sin limitaciones"]""",
                defaultValue = "4 - Sin limitaciones",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "nutrition",
                name = "Nutrición",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 - Muy pobre", "2 - Probablemente inadecuada", "3 - Adecuada", "4 - Excelente"]""",
                defaultValue = "4 - Excelente",
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "friction_shear",
                name = "Fricción y Deslizamiento",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 - Problema", "2 - Problema potencial", "3 - Sin problema aparente"]""",
                defaultValue = "3 - Sin problema aparente",
                displayOrder = 5
            ),

            // OPTIONAL PATIENT INFORMATION
            FieldEntity(
                calculatorId = "braden_scale",
                id = "patient_age",
                name = "Edad del Paciente (opcional)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "años",
                minValue = 0.0,
                maxValue = 120.0,
                defaultValue = "65",
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "chronic_conditions",
                name = "Condiciones Crónicas (diabetes, enfermedad vascular)",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 7
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "bed_rest",
                name = "Reposo en Cama Prolongado",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 8
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "critical_illness",
                name = "Enfermedad Crítica/UCI",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 9
            ),

            // OUTPUT FIELDS
            FieldEntity(
                calculatorId = "braden_scale",
                id = "total_score",
                name = "Puntuación Total",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "puntos",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "risk_level",
                name = "Nivel de Riesgo",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "risk_interpretation",
                name = "Interpretación del Riesgo",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "detailed_assessment",
                name = "Evaluación Detallada",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "prevention_recommendations",
                name = "Recomendaciones de Prevención",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "monitoring_schedule",
                name = "Cronograma de Monitoreo",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "braden_scale",
                id = "risk_factors_analysis",
                name = "Análisis de Factores de Riesgo",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 6
            )
        ))

        // ==============================================
// 11. GLASGOW COMA SCALE CALCULATOR
// ==============================================
        fields.addAll(listOf(
            // INPUT FIELDS - The 3 Glasgow Coma Scale Components
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "eye_response",
                name = "Respuesta Ocular",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 - No abre los ojos", "2 - Abre los ojos al dolor", "3 - Abre los ojos a la voz", "4 - Abre los ojos espontáneamente"]""",
                defaultValue = "4 - Abre los ojos espontáneamente",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "verbal_response",
                name = "Respuesta Verbal",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 - No respuesta verbal", "2 - Sonidos incomprensibles", "3 - Palabras inapropiadas", "4 - Confuso", "5 - Orientado"]""",
                defaultValue = "5 - Orientado",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "motor_response",
                name = "Respuesta Motora",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 - No respuesta motora", "2 - Extensión anormal (descerebración)", "3 - Flexión anormal (decorticación)", "4 - Flexión de retirada", "5 - Localiza el dolor", "6 - Obedece órdenes"]""",
                defaultValue = "6 - Obedece órdenes",
                displayOrder = 2
            ),

            // CLINICAL CONTEXT AND PATIENT FACTORS
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "is_intubated",
                name = "Paciente Intubado",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "patient_age",
                name = "Edad del Paciente (opcional)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "años",
                minValue = 0.0,
                maxValue = 120.0,
                defaultValue = "45",
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "traumatic_brain_injury",
                name = "Traumatismo Craneoencefálico",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "has_seizures",
                name = "Actividad Convulsiva",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "drugs_alcohol",
                name = "Intoxicación por Drogas/Alcohol",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 7
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "clinical_context",
                name = "Contexto Clínico",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Evaluación General", "Urgencias", "UCI", "Postoperatorio", "Trauma", "Neurológico"]""",
                defaultValue = "Evaluación General",
                displayOrder = 8
            ),

            // OUTPUT FIELDS
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "total_score",
                name = "Puntuación Total",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "puntos",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "consciousness_level",
                name = "Nivel de Consciencia",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "neurological_interpretation",
                name = "Interpretación Neurológica",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "detailed_assessment",
                name = "Evaluación Detallada",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "clinical_recommendations",
                name = "Recomendaciones Clínicas",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "monitoring_protocol",
                name = "Protocolo de Monitoreo",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "prognostic_indicators",
                name = "Indicadores Pronósticos",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "emergency_alerts",
                name = "Alertas de Emergencia",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 7
            )
        ))

        // ==============================================
        // 12. PEDIATRIC DOSAGE CALCULATOR
        // ==============================================
        fields.addAll(listOf(
            // PATIENT INFORMATION
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "patient_weight",
                name = "Peso del Paciente",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "kg",
                minValue = 0.5,
                maxValue = 80.0,
                defaultValue = "10",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "patient_age_months",
                name = "Edad del Paciente",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "meses",
                minValue = 0.0,
                maxValue = 216.0, // 18 years = 216 months
                defaultValue = "24",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "medication",
                name = "Medicamento",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Amoxicilina", "Paracetamol", "Ibuprofeno", "Azitromicina", "Cefixima", "Trimetoprim-Sulfametoxazol", "Claritromicina", "Dexametasona", "Salbutamol", "Loratadina", "Cetirizina", "Furosemida", "Otro medicamento"]""",
                defaultValue = "Amoxicilina",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "custom_dose_per_kg",
                name = "Dosis personalizada (mg/kg/día) - Solo si seleccionó 'Otro'",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mg/kg/día",
                minValue = 0.1,
                maxValue = 500.0,
                defaultValue = "50",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "custom_doses_per_day",
                name = "Número de dosis por día - Solo si seleccionó 'Otro'",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "dosis/día",
                minValue = 1.0,
                maxValue = 6.0,
                defaultValue = "2",
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "medication_concentration",
                name = "Concentración del Medicamento (opcional)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mg/mL",
                minValue = 0.1,
                maxValue = 1000.0,
                defaultValue = "50",
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "clinical_condition",
                name = "Condición Clínica",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Infección Respiratoria", "Infección del Oído", "Fiebre", "Dolor/Inflamación", "Infección Urinaria", "Infección Gastrointestinal", "Asma/Broncoespasmo", "Alergia", "Otra condición"]""",
                defaultValue = "Infección Respiratoria",
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "severity",
                name = "Severidad de la Condición",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["Leve", "Moderada", "Severa"]""",
                defaultValue = "Moderada",
                displayOrder = 7
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "renal_function",
                name = "Función Renal",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Normal", "Insuficiencia Leve", "Insuficiencia Moderada", "Insuficiencia Severa"]""",
                defaultValue = "Normal",
                displayOrder = 8
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "premature_infant",
                name = "Recién Nacido Prematuro",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 9
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "allergies",
                name = "Alergias Conocidas",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 10
            ),

            // OUTPUT FIELDS
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "recommended_dose_per_kg",
                name = "Dosis Recomendada",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mg/kg/día",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "total_daily_dose",
                name = "Dosis Total Diaria",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mg/día",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "dose_per_administration",
                name = "Dosis por Administración",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mg",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "volume_per_dose",
                name = "Volumen por Dosis",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mL",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "doses_per_day",
                name = "Número de Dosis por Día",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "dosis",
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "dosing_schedule",
                name = "Horario de Administración",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "safety_warnings",
                name = "Advertencias de Seguridad",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "administration_instructions",
                name = "Instrucciones de Administración",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 7
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "monitoring_recommendations",
                name = "Recomendaciones de Monitoreo",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 8
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "age_appropriate_warnings",
                name = "Advertencias Específicas por Edad",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 9
            )
        ))

        // ==============================================
// 13. APGAR SCORE CALCULATOR
// ==============================================
        fields.addAll(listOf(
            // EVALUATION TIME
            FieldEntity(
                calculatorId = "apgar_score",
                id = "evaluation_time",
                name = "Tiempo de Evaluación",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["1 minuto", "5 minutos", "10 minutos"]""",
                defaultValue = "1 minuto",
                displayOrder = 0
            ),

            // THE 5 APGAR CRITERIA
            FieldEntity(
                calculatorId = "apgar_score",
                id = "appearance_color",
                name = "Apariencia (Color de la Piel)",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["0 - Cianosis generalizada o palidez", "1 - Extremidades cianóticas, cuerpo rosado", "2 - Rosado completamente"]""",
                defaultValue = "2 - Rosado completamente",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "pulse_heart_rate",
                name = "Pulso (Frecuencia Cardíaca)",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["0 - Ausente", "1 - Menos de 100 lpm", "2 - Más de 100 lpm"]""",
                defaultValue = "2 - Más de 100 lpm",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "grimace_reflex",
                name = "Gesticulación (Irritabilidad Refleja)",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["0 - Sin respuesta", "1 - Mueca o débil", "2 - Llanto vigoroso"]""",
                defaultValue = "2 - Llanto vigoroso",
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "activity_muscle_tone",
                name = "Actividad (Tono Muscular)",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["0 - Flácido", "1 - Flexión mínima de extremidades", "2 - Movimientos activos"]""",
                defaultValue = "2 - Movimientos activos",
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "respiratory_effort",
                name = "Respiración (Esfuerzo Respiratorio)",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["0 - Ausente", "1 - Débil o irregular", "2 - Llanto fuerte"]""",
                defaultValue = "2 - Llanto fuerte",
                displayOrder = 5
            ),

            // ADDITIONAL NEONATAL INFORMATION
            FieldEntity(
                calculatorId = "apgar_score",
                id = "gestational_age",
                name = "Edad Gestacional (opcional)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "semanas",
                minValue = 20.0,
                maxValue = 44.0,
                defaultValue = "39",
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "birth_weight",
                name = "Peso al Nacer (opcional)",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "gramos",
                minValue = 500.0,
                maxValue = 6000.0,
                defaultValue = "3200",
                displayOrder = 7
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "delivery_type",
                name = "Tipo de Parto",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["Vaginal espontáneo", "Vaginal instrumentado", "Cesárea electiva", "Cesárea de urgencia"]""",
                defaultValue = "Vaginal espontáneo",
                displayOrder = 8
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "maternal_complications",
                name = "Complicaciones Maternas",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 9
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "multiple_birth",
                name = "Embarazo Múltiple",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 10
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "resuscitation_needed",
                name = "Requirió Reanimación",
                type = FieldType.CHECKBOX.name,
                isInputField = true,
                defaultValue = "false",
                displayOrder = 11
            ),

            // OUTPUT FIELDS
            FieldEntity(
                calculatorId = "apgar_score",
                id = "total_score",
                name = "Puntuación Total APGAR",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "puntos",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "clinical_status",
                name = "Estado Clínico",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "clinical_interpretation",
                name = "Interpretación Clínica",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "detailed_assessment",
                name = "Evaluación Detallada",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 3
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "immediate_actions",
                name = "Acciones Inmediatas",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 4
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "monitoring_protocol",
                name = "Protocolo de Monitoreo",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 5
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "prognostic_indicators",
                name = "Indicadores Pronósticos",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 6
            ),
            FieldEntity(
                calculatorId = "apgar_score",
                id = "follow_up_recommendations",
                name = "Recomendaciones de Seguimiento",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 7
            )
        ))


        return fields
    }
}

