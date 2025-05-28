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
            )
        )
    }


//    private fun createInitialCalculators(): List<CalculatorEntity> {
//        return listOf(
//            // 1. DOSIFICACIÓN Y MEDICACIÓN
//            CalculatorEntity(
//                id = "medication_dosage",
//                name = "Calculadora de Dosis de Medicamentos",
//                description = "Cálculos de dosis (mg/kg), unidades y concentraciones",
//                categoryId = "dosing_medication"
//            ),
//            CalculatorEntity(
//                id = "heparin_dosage",
//                name = "Calculadora de Dosis de Heparina",
//                description = "HBPM y aPTT - Anticoagulación especializada",
//                categoryId = "dosing_medication"
//            ),
//            CalculatorEntity(
//                id = "unit_converter",
//                name = "Conversor de Unidades",
//                description = "mg⇄mL, mEq⇄mg y otras conversiones médicas",
//                categoryId = "dosing_medication"
//            ),
//
//            // 2. FLUIDOS Y ELECTROLITOS
//            CalculatorEntity(
//                id = "iv_drip_rate",
//                name = "Velocidad de Goteo IV",
//                description = "Cálculo de gtt/min y mL/h para terapia intravenosa",
//                categoryId = "fluids_electrolytes"
//            ),
//            CalculatorEntity(
//                id = "fluid_balance",
//                name = "Balance Hídrico 24h",
//                description = "Ingresos y egresos en 24 horas",
//                categoryId = "fluids_electrolytes"
//            ),
//            CalculatorEntity(
//                id = "electrolyte_management",
//                name = "Gestión de Fluidos y Electrolitos",
//                description = "Reemplazo de potasio, sodio y otros electrolitos",
//                categoryId = "fluids_electrolytes"
//            ),
//
//            // 3. MONITOREO DE SIGNOS VITALES
//            CalculatorEntity(
//                id = "bmi_calculator",
//                name = "Índice de Masa Corporal (IMC)",
//                description = "Evaluación nutricional: peso ÷ talla²",
//                categoryId = "vital_signs"
//            ),
//            CalculatorEntity(
//                id = "map_calculator",
//                name = "Presión Arterial Media (PAM)",
//                description = "Cálculo hemodinámico: (PAS + 2×PAD)/3",
//                categoryId = "vital_signs"
//            ),
//
//
//            // 4. ESCALAS DE VALORACIÓN CLÍNICA
//            CalculatorEntity(
//                id = "braden_scale",
//                name = "Escala de Braden",
//                description = "Evaluación de riesgo de úlceras por presión",
//                categoryId = "clinical_scales"
//            ),
//            CalculatorEntity(
//                id = "glasgow_coma_scale",
//                name = "Escala de Coma de Glasgow",
//                description = "Nivel de conciencia en urgencias neurológicas",
//                categoryId = "clinical_scales"
//            ),
//
//            // 5. PEDIATRÍA Y NEONATOLOGÍA
//            CalculatorEntity(
//                id = "pediatric_dosage",
//                name = "Dosis Pediátrica",
//                description = "Dosificación (mg/kg) según peso y edad",
//                categoryId = "pediatric_neonatal"
//            ),
//            CalculatorEntity(
//                id = "apgar_score",
//                name = "Puntuación APGAR",
//                description = "Evaluación inmediata del recién nacido",
//                categoryId = "pediatric_neonatal"
//            ),
//            CalculatorEntity(
//                id = "minute_ventilation",
//                name = "Calculadora de Ventilación Minuto",
//                description = "Cálculo de VE = FR × VT para evaluación ventilatoria",
//                categoryId = "vital_signs"
//            )
//
//        )
//    }

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

        return fields
    }
}

