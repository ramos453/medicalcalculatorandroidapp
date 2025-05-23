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
            // 1. DOSIFICACIÓN Y MEDICACIÓN
            CalculatorEntity(
                id = "medication_dosage",
                name = "Calculadora de Dosis de Medicamentos",
                description = "Cálculos de dosis (mg/kg), unidades y concentraciones",
                categoryId = "dosing_medication"
            ),
            CalculatorEntity(
                id = "heparin_dosage",
                name = "Calculadora de Dosis de Heparina",
                description = "HBPM y aPTT - Anticoagulación especializada",
                categoryId = "dosing_medication"
            ),
            CalculatorEntity(
                id = "unit_converter",
                name = "Conversor de Unidades",
                description = "mg⇄mL, mEq⇄mg y otras conversiones médicas",
                categoryId = "dosing_medication"
            ),

            // 2. FLUIDOS Y ELECTROLITOS
            CalculatorEntity(
                id = "iv_drip_rate",
                name = "Velocidad de Goteo IV",
                description = "Cálculo de gtt/min y mL/h para terapia intravenosa",
                categoryId = "fluids_electrolytes"
            ),
            CalculatorEntity(
                id = "fluid_balance",
                name = "Balance Hídrico 24h",
                description = "Ingresos y egresos en 24 horas",
                categoryId = "fluids_electrolytes"
            ),
            CalculatorEntity(
                id = "electrolyte_management",
                name = "Gestión de Fluidos y Electrolitos",
                description = "Reemplazo de potasio, sodio y otros electrolitos",
                categoryId = "fluids_electrolytes"
            ),

            // 3. MONITOREO DE SIGNOS VITALES
            CalculatorEntity(
                id = "bmi_calculator",
                name = "Índice de Masa Corporal (IMC)",
                description = "Evaluación nutricional: peso ÷ talla²",
                categoryId = "vital_signs"
            ),
            CalculatorEntity(
                id = "map_calculator",
                name = "Presión Arterial Media (PAM)",
                description = "Cálculo hemodinámico: (PAS + 2×PAD)/3",
                categoryId = "vital_signs"
            ),
            CalculatorEntity(
                id = "pain_scale",
                name = "Escala de Valoración del Dolor (EVA/VAS)",
                description = "Puntuación de dolor de 0-10",
                categoryId = "vital_signs"
            ),

            // 4. ESCALAS DE VALORACIÓN CLÍNICA
            CalculatorEntity(
                id = "braden_scale",
                name = "Escala de Braden",
                description = "Evaluación de riesgo de úlceras por presión",
                categoryId = "clinical_scales"
            ),
            CalculatorEntity(
                id = "glasgow_coma_scale",
                name = "Escala de Coma de Glasgow",
                description = "Nivel de conciencia en urgencias neurológicas",
                categoryId = "clinical_scales"
            ),

            // 5. PEDIATRÍA Y NEONATOLOGÍA
            CalculatorEntity(
                id = "pediatric_dosage",
                name = "Dosis Pediátrica",
                description = "Dosificación (mg/kg) según peso y edad",
                categoryId = "pediatric_neonatal"
            ),
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
        // 2. BMI CALCULATOR
        // ==============================================
        fields.addAll(listOf(
            // Input fields
            FieldEntity(
                calculatorId = "bmi_calculator",
                id = "height",
                name = "Altura",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "cm",
                minValue = 50.0,
                maxValue = 300.0,
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
                minValue = 2.0,
                maxValue = 500.0,
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
            )
        ))

        // ==============================================
        // 3. MAP CALCULATOR
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
                maxValue = 300.0,
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
                maxValue = 200.0,
                defaultValue = "80",
                displayOrder = 1
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
            )
        ))

        // ==============================================
        // 4. IV DRIP RATE CALCULATOR
        // ==============================================
        fields.addAll(listOf(
            // Input fields
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "total_volume",
                name = "Volumen Total",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mL",
                minValue = 1.0,
                maxValue = 3000.0,
                defaultValue = "1000",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "infusion_time",
                name = "Tiempo de Infusión",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "horas",
                minValue = 0.1,
                maxValue = 24.0,
                defaultValue = "8",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "drop_factor",
                name = "Factor de Goteo",
                type = FieldType.DROPDOWN.name,
                isInputField = true,
                options = """["10 gtt/mL", "15 gtt/mL", "20 gtt/mL", "60 gtt/mL (microgotero)"]""",
                defaultValue = "20 gtt/mL",
                displayOrder = 2
            ),
            // Output fields
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "ml_per_hour",
                name = "Velocidad",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mL/h",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "iv_drip_rate",
                id = "drops_per_minute",
                name = "Goteo",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "gtt/min",
                displayOrder = 1
            )
        ))

        // ==============================================
        // 5. GLASGOW COMA SCALE
        // ==============================================
        fields.addAll(listOf(
            // Input fields
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "eye_response",
                name = "Respuesta Ocular",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["4 - Espontánea", "3 - Al llamado verbal", "2 - Al dolor", "1 - Sin respuesta"]""",
                defaultValue = "4 - Espontánea",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "verbal_response",
                name = "Respuesta Verbal",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["5 - Orientado", "4 - Confuso", "3 - Palabras inapropiadas", "2 - Sonidos incomprensibles", "1 - Sin respuesta"]""",
                defaultValue = "5 - Orientado",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "glasgow_coma_scale",
                id = "motor_response",
                name = "Respuesta Motora",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["6 - Obedece órdenes", "5 - Localiza dolor", "4 - Retira al dolor", "3 - Flexión anormal", "2 - Extensión anormal", "1 - Sin respuesta"]""",
                defaultValue = "6 - Obedece órdenes",
                displayOrder = 2
            ),
            // Output fields
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
                id = "severity_level",
                name = "Nivel de Severidad",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 1
            )
        ))

        // ==============================================
        // 6. PEDIATRIC DOSAGE
        // ==============================================
        fields.addAll(listOf(
            // Input fields
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "child_weight",
                name = "Peso del Niño",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "kg",
                minValue = 0.5,
                maxValue = 80.0,
                defaultValue = "20",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "child_age",
                name = "Edad",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "años",
                minValue = 0.1,
                maxValue = 18.0,
                defaultValue = "5",
                displayOrder = 1
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "adult_dose",
                name = "Dosis de Adulto",
                type = FieldType.NUMBER.name,
                isInputField = true,
                units = "mg",
                minValue = 1.0,
                maxValue = 2000.0,
                defaultValue = "500",
                displayOrder = 2
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "calculation_method",
                name = "Método de Cálculo",
                type = FieldType.RADIO.name,
                isInputField = true,
                options = """["Por peso (mg/kg)", "Fórmula de Young (edad)", "Fórmula de Clark (peso)"]""",
                defaultValue = "Por peso (mg/kg)",
                displayOrder = 3
            ),
            // Output fields
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "pediatric_dose",
                name = "Dosis Pediátrica",
                type = FieldType.NUMBER.name,
                isInputField = false,
                units = "mg",
                displayOrder = 0
            ),
            FieldEntity(
                calculatorId = "pediatric_dosage",
                id = "safety_warning",
                name = "Advertencia de Seguridad",
                type = FieldType.TEXT.name,
                isInputField = false,
                displayOrder = 1
            )
        ))

        return fields
    }
}

// package com.example.medicalcalculatorapp.data.db.util
//
//import android.content.Context
//import com.example.medicalcalculatorapp.R
//import com.example.medicalcalculatorapp.data.db.MedicalCalculatorDatabase
//import com.example.medicalcalculatorapp.data.db.entity.CalculatorEntity
//import com.example.medicalcalculatorapp.data.db.entity.CategoryEntity
//import com.example.medicalcalculatorapp.data.db.entity.FieldEntity
//import com.example.medicalcalculatorapp.domain.model.FieldType
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.withContext
//
//object DatabasePrepopulateUtil {
//
//    suspend fun prepopulateDatabase(context: Context, database: MedicalCalculatorDatabase) {
//        withContext(Dispatchers.IO) {
//            // First, insert categories
//            val categories = createInitialCategories()
//            database.categoryDao().insertCategories(categories)
//
//            // Then, insert calculators
//            val calculators = createInitialCalculators()
//            database.calculatorDao().insertCalculators(calculators)
//
//            // Finally, insert fields for each calculator
//            val allFields = createInitialFields()
//            database.fieldDao().insertFields(allFields)
//        }
//    }
//
//
//    private fun createInitialCategories(): List<CategoryEntity> {
//        return listOf(
//            CategoryEntity(
//                id = "general",
//                name = "General",
//                description = "Calculadoras médicas generales",
//                iconResId = R.drawable.ic_launcher_foreground,
//                displayOrder = 0
//            ),
//            CategoryEntity(
//                id = "cardiology",
//                name = "Cardiología",
//                description = "Calculadoras cardiovasculares",
//                iconResId = R.drawable.ic_launcher_foreground,
//                displayOrder = 1
//            ),
//            CategoryEntity(
//                id = "renal",
//                name = "Nefrología",
//                description = "Calculadoras de función renal",
//                iconResId = R.drawable.ic_launcher_foreground,
//                displayOrder = 2
//            ),
//            CategoryEntity(
//                id = "obstetrics",
//                name = "Obstetricia",
//                description = "Calculadoras de embarazo y parto",
//                iconResId = R.drawable.ic_launcher_foreground,
//                displayOrder = 3
//            )
//        )
//    }
//
//    private fun createInitialCalculators(): List<CalculatorEntity> {
//        return listOf(
//            CalculatorEntity(
//                id = "bmi_calc",
//                name = "Calculadora de IMC",
//                description = "Calcula el Índice de Masa Corporal basado en altura y peso",
//                categoryId = "general"
//            ),
//            CalculatorEntity(
//                id = "creatinine_clearance",
//                name = "Depuración de Creatinina",
//                description = "Estima la depuración de creatinina (función renal)",
//                categoryId = "renal"
//            ),
//            CalculatorEntity(
//                id = "map_calc",
//                name = "Presión Arterial Media (PAM)",
//                description = "Calcula la PAM.",
//                categoryId = "cardiology"
//            ),
//            CalculatorEntity(
//                id = "pregnancy_calc",
//                name = "Fechas de Embarazo",
//                description = "Desde FUM, EG, o fecha de concepción.",
//                categoryId = "obstetrics"
//            ),
//            CalculatorEntity(
//                id = "bmi_bsa_calc",
//                name = "IMC y SC",
//                description = "Categoriza obesidad, ayuda en dosificación de medicamentos.",
//                categoryId = "general"
//            )
//        )
//    }
//
////
////    private fun createInitialCategories(): List<CategoryEntity> {
////        return listOf(
////            CategoryEntity(
////                id = "general",
////                name = "General",
////                description = "General medical calculators",
////                iconResId = R.drawable.ic_launcher_foreground,
////                displayOrder = 0
////            ),
////            CategoryEntity(
////                id = "cardiology",
////                name = "Cardiology",
////                description = "Cardiovascular calculators",
////                iconResId = R.drawable.ic_launcher_foreground,
////                displayOrder = 1
////            ),
////            CategoryEntity(
////                id = "renal",
////                name = "Nephrology",
////                description = "Kidney function calculators",
////                iconResId = R.drawable.ic_launcher_foreground,
////                displayOrder = 2
////            ),
////            CategoryEntity(
////                id = "obstetrics",
////                name = "Obstetrics",
////                description = "Pregnancy and childbirth calculators",
////                iconResId = R.drawable.ic_launcher_foreground,
////                displayOrder = 3
////            )
////        )
////    }
////
////    private fun createInitialCalculators(): List<CalculatorEntity> {
////        return listOf(
////            CalculatorEntity(
////                id = "bmi_calc",
////                name = "BMI Calculator",
////                description = "Calculate Body Mass Index based on height and weight",
////                categoryId = "general"
////            ),
////            CalculatorEntity(
////                id = "creatinine_clearance",
////                name = "Creatinine Clearance",
////                description = "Estimates creatinine clearance (kidney function)",
////                categoryId = "renal"
////            ),
////            CalculatorEntity(
////                id = "map_calc",
////                name = "Mean Arterial Pressure (MAP)",
////                description = "Calculates MAP.",
////                categoryId = "cardiology"
////            ),
////            CalculatorEntity(
////                id = "pregnancy_calc",
////                name = "Pregnancy Due Dates",
////                description = "From LMP, EGA, or date of conception.",
////                categoryId = "obstetrics"
////            ),
////            CalculatorEntity(
////                id = "bmi_bsa_calc",
////                name = "BMI & BSA",
////                description = "Categorizes obesity, assists some med dosing.",
////                categoryId = "general"
////            )
////        )
////    }
//
//    private fun createInitialFields(): List<FieldEntity> {
//        val fields = mutableListOf<FieldEntity>()
//
//        // BMI Calculator fields
//        fields.addAll(
//            listOf(
//                // Input fields
////                FieldEntity(
////                    calculatorId = "bmi_calc",
////                    id = "height",
////                    name = "Height",
////                    type = FieldType.NUMBER.name,
////                    isInputField = true,
////                    units = "cm",
////                    minValue = 50.0,
////                    maxValue = 300.0,
////                    defaultValue = "170",
////                    displayOrder = 0
////                ),
////                FieldEntity(
////                    calculatorId = "bmi_calc",
////                    id = "weight",
////                    name = "Weight",
////                    type = FieldType.NUMBER.name,
////                    isInputField = true,
////                    units = "kg",
////                    minValue = 20.0,
////                    maxValue = 500.0,
////                    defaultValue = "70",
////                    displayOrder = 1
////                ),
//                // Update field names to Spanish
//                FieldEntity(
//                    calculatorId = "bmi_calc",
//                    id = "height",
//                    name = "Altura",
//                    type = FieldType.NUMBER.name,
//                    isInputField = true,
//                    units = "cm",
//                    minValue = 50.0,
//                    maxValue = 300.0,
//                    defaultValue = "170",
//                    displayOrder = 0
//                ),
//                FieldEntity(
//                    calculatorId = "bmi_calc",
//                    id = "weight",
//                    name = "Peso",
//                    type = FieldType.NUMBER.name,
//                    isInputField = true,
//                    units = "kg",
//                    minValue = 20.0,
//                    maxValue = 500.0,
//                    defaultValue = "70",
//                    displayOrder = 1
//                ),
//                // Output fields
//                FieldEntity(
//                    calculatorId = "bmi_calc",
//                    id = "bmi",
//                    name = "BMI",
//                    type = FieldType.NUMBER.name,
//                    isInputField = false,
//                    units = "kg/m²",
//                    displayOrder = 0
//                ),
//                FieldEntity(
//                    calculatorId = "bmi_calc",
//                    id = "category",
//                    name = "Category",
//                    type = FieldType.TEXT.name,
//                    isInputField = false,
//                    displayOrder = 1
//                )
//            )
//        )
//
//        // Add Creatinine Clearance fields
//        fields.addAll(
//            listOf(
//                // Input fields
//                FieldEntity(
//                    calculatorId = "creatinine_clearance",
//                    id = "age",
//                    name = "Age",
//                    type = FieldType.NUMBER.name,
//                    isInputField = true,
//                    units = "years",
//                    minValue = 18.0,
//                    maxValue = 120.0,
//                    defaultValue = "50",
//                    displayOrder = 0
//                ),
//                FieldEntity(
//                    calculatorId = "creatinine_clearance",
//                    id = "weight",
//                    name = "Weight",
//                    type = FieldType.NUMBER.name,
//                    isInputField = true,
//                    units = "kg",
//                    minValue = 20.0,
//                    maxValue = 500.0,
//                    defaultValue = "70",
//                    displayOrder = 1
//                ),
//                FieldEntity(
//                    calculatorId = "creatinine_clearance",
//                    id = "gender",
//                    name = "Gender",
//                    type = FieldType.RADIO.name,
//                    isInputField = true,
//                    options = """["Male", "Female"]""", // JSON string
//                    displayOrder = 2
//                ),
//                FieldEntity(
//                    calculatorId = "creatinine_clearance",
//                    id = "serum_creatinine",
//                    name = "Serum Creatinine",
//                    type = FieldType.NUMBER.name,
//                    isInputField = true,
//                    units = "mg/dL",
//                    minValue = 0.1,
//                    maxValue = 20.0,
//                    defaultValue = "1.0",
//                    displayOrder = 3
//                ),
//                // Output field
//                FieldEntity(
//                    calculatorId = "creatinine_clearance",
//                    id = "creatinine_clearance",
//                    name = "Creatinine Clearance",
//                    type = FieldType.NUMBER.name,
//                    isInputField = false,
//                    units = "mL/min",
//                    displayOrder = 0
//                )
//            )
//        )
//
//        // Add MAP Calculator fields
//        fields.addAll(
//            listOf(
//                // Input fields
//                FieldEntity(
//                    calculatorId = "map_calc",
//                    id = "systolic",
//                    name = "Systolic BP",
//                    type = FieldType.NUMBER.name,
//                    isInputField = true,
//                    units = "mmHg",
//                    minValue = 40.0,
//                    maxValue = 300.0,
//                    defaultValue = "120",
//                    displayOrder = 0
//                ),
//                FieldEntity(
//                    calculatorId = "map_calc",
//                    id = "diastolic",
//                    name = "Diastolic BP",
//                    type = FieldType.NUMBER.name,
//                    isInputField = true,
//                    units = "mmHg",
//                    minValue = 20.0,
//                    maxValue = 200.0,
//                    defaultValue = "80",
//                    displayOrder = 1
//                ),
//                // Output field
//                FieldEntity(
//                    calculatorId = "map_calc",
//                    id = "map",
//                    name = "MAP",
//                    type = FieldType.NUMBER.name,
//                    isInputField = false,
//                    units = "mmHg",
//                    displayOrder = 0
//                )
//            )
//        )
//
//        // Add Pregnancy Calculator fields
//        fields.addAll(
//            listOf(
//                // Input field
//                FieldEntity(
//                    calculatorId = "pregnancy_calc",
//                    id = "lmp_date",
//                    name = "Last Menstrual Period",
//                    type = FieldType.TEXT.name,
//                    isInputField = true,
//                    displayOrder = 0
//                ),
//                // Output fields
//                FieldEntity(
//                    calculatorId = "pregnancy_calc",
//                    id = "due_date",
//                    name = "Due Date",
//                    type = FieldType.TEXT.name,
//                    isInputField = false,
//                    displayOrder = 0
//                ),
//                FieldEntity(
//                    calculatorId = "pregnancy_calc",
//                    id = "current_ega",
//                    name = "Current EGA",
//                    type = FieldType.TEXT.name,
//                    isInputField = false,
//                    displayOrder = 1
//                )
//            )
//        )
//
//        // Add BMI & BSA Calculator fields
//        fields.addAll(
//            listOf(
//                // Input fields
//                FieldEntity(
//                    calculatorId = "bmi_bsa_calc",
//                    id = "height",
//                    name = "Height",
//                    type = FieldType.NUMBER.name,
//                    isInputField = true,
//                    units = "cm",
//                    minValue = 50.0,
//                    maxValue = 300.0,
//                    defaultValue = "170",
//                    displayOrder = 0
//                ),
//                FieldEntity(
//                    calculatorId = "bmi_bsa_calc",
//                    id = "weight",
//                    name = "Weight",
//                    type = FieldType.NUMBER.name,
//                    isInputField = true,
//                    units = "kg",
//                    minValue = 20.0,
//                    maxValue = 500.0,
//                    defaultValue = "70",
//                    displayOrder = 1
//                ),
//                // Output fields
//                FieldEntity(
//                    calculatorId = "bmi_bsa_calc",
//                    id = "bmi",
//                    name = "BMI",
//                    type = FieldType.NUMBER.name,
//                    isInputField = false,
//                    units = "kg/m²",
//                    displayOrder = 0
//                ),
//                FieldEntity(
//                    calculatorId = "bmi_bsa_calc",
//                    id = "bsa",
//                    name = "BSA",
//                    type = FieldType.NUMBER.name,
//                    isInputField = false,
//                    units = "m²",
//                    displayOrder = 1
//                )
//            )
//        )
//
//        return fields
//    }
//}