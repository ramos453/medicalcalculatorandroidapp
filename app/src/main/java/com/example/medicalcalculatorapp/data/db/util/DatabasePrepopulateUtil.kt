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
                id = "general",
                name = "General",
                description = "Calculadoras médicas generales",
                iconResId = R.drawable.ic_launcher_foreground,
                displayOrder = 0
            ),
            CategoryEntity(
                id = "cardiology",
                name = "Cardiología",
                description = "Calculadoras cardiovasculares",
                iconResId = R.drawable.ic_launcher_foreground,
                displayOrder = 1
            ),
            CategoryEntity(
                id = "renal",
                name = "Nefrología",
                description = "Calculadoras de función renal",
                iconResId = R.drawable.ic_launcher_foreground,
                displayOrder = 2
            ),
            CategoryEntity(
                id = "obstetrics",
                name = "Obstetricia",
                description = "Calculadoras de embarazo y parto",
                iconResId = R.drawable.ic_launcher_foreground,
                displayOrder = 3
            )
        )
    }

    private fun createInitialCalculators(): List<CalculatorEntity> {
        return listOf(
            CalculatorEntity(
                id = "bmi_calc",
                name = "Calculadora de IMC",
                description = "Calcula el Índice de Masa Corporal basado en altura y peso",
                categoryId = "general"
            ),
            CalculatorEntity(
                id = "creatinine_clearance",
                name = "Depuración de Creatinina",
                description = "Estima la depuración de creatinina (función renal)",
                categoryId = "renal"
            ),
            CalculatorEntity(
                id = "map_calc",
                name = "Presión Arterial Media (PAM)",
                description = "Calcula la PAM.",
                categoryId = "cardiology"
            ),
            CalculatorEntity(
                id = "pregnancy_calc",
                name = "Fechas de Embarazo",
                description = "Desde FUM, EG, o fecha de concepción.",
                categoryId = "obstetrics"
            ),
            CalculatorEntity(
                id = "bmi_bsa_calc",
                name = "IMC y SC",
                description = "Categoriza obesidad, ayuda en dosificación de medicamentos.",
                categoryId = "general"
            )
        )
    }

//
//    private fun createInitialCategories(): List<CategoryEntity> {
//        return listOf(
//            CategoryEntity(
//                id = "general",
//                name = "General",
//                description = "General medical calculators",
//                iconResId = R.drawable.ic_launcher_foreground,
//                displayOrder = 0
//            ),
//            CategoryEntity(
//                id = "cardiology",
//                name = "Cardiology",
//                description = "Cardiovascular calculators",
//                iconResId = R.drawable.ic_launcher_foreground,
//                displayOrder = 1
//            ),
//            CategoryEntity(
//                id = "renal",
//                name = "Nephrology",
//                description = "Kidney function calculators",
//                iconResId = R.drawable.ic_launcher_foreground,
//                displayOrder = 2
//            ),
//            CategoryEntity(
//                id = "obstetrics",
//                name = "Obstetrics",
//                description = "Pregnancy and childbirth calculators",
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
//                name = "BMI Calculator",
//                description = "Calculate Body Mass Index based on height and weight",
//                categoryId = "general"
//            ),
//            CalculatorEntity(
//                id = "creatinine_clearance",
//                name = "Creatinine Clearance",
//                description = "Estimates creatinine clearance (kidney function)",
//                categoryId = "renal"
//            ),
//            CalculatorEntity(
//                id = "map_calc",
//                name = "Mean Arterial Pressure (MAP)",
//                description = "Calculates MAP.",
//                categoryId = "cardiology"
//            ),
//            CalculatorEntity(
//                id = "pregnancy_calc",
//                name = "Pregnancy Due Dates",
//                description = "From LMP, EGA, or date of conception.",
//                categoryId = "obstetrics"
//            ),
//            CalculatorEntity(
//                id = "bmi_bsa_calc",
//                name = "BMI & BSA",
//                description = "Categorizes obesity, assists some med dosing.",
//                categoryId = "general"
//            )
//        )
//    }

    private fun createInitialFields(): List<FieldEntity> {
        val fields = mutableListOf<FieldEntity>()

        // BMI Calculator fields
        fields.addAll(
            listOf(
                // Input fields
//                FieldEntity(
//                    calculatorId = "bmi_calc",
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
//                    calculatorId = "bmi_calc",
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
                // Update field names to Spanish
                FieldEntity(
                    calculatorId = "bmi_calc",
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
                    calculatorId = "bmi_calc",
                    id = "weight",
                    name = "Peso",
                    type = FieldType.NUMBER.name,
                    isInputField = true,
                    units = "kg",
                    minValue = 20.0,
                    maxValue = 500.0,
                    defaultValue = "70",
                    displayOrder = 1
                ),
                // Output fields
                FieldEntity(
                    calculatorId = "bmi_calc",
                    id = "bmi",
                    name = "BMI",
                    type = FieldType.NUMBER.name,
                    isInputField = false,
                    units = "kg/m²",
                    displayOrder = 0
                ),
                FieldEntity(
                    calculatorId = "bmi_calc",
                    id = "category",
                    name = "Category",
                    type = FieldType.TEXT.name,
                    isInputField = false,
                    displayOrder = 1
                )
            )
        )

        // Add Creatinine Clearance fields
        fields.addAll(
            listOf(
                // Input fields
                FieldEntity(
                    calculatorId = "creatinine_clearance",
                    id = "age",
                    name = "Age",
                    type = FieldType.NUMBER.name,
                    isInputField = true,
                    units = "years",
                    minValue = 18.0,
                    maxValue = 120.0,
                    defaultValue = "50",
                    displayOrder = 0
                ),
                FieldEntity(
                    calculatorId = "creatinine_clearance",
                    id = "weight",
                    name = "Weight",
                    type = FieldType.NUMBER.name,
                    isInputField = true,
                    units = "kg",
                    minValue = 20.0,
                    maxValue = 500.0,
                    defaultValue = "70",
                    displayOrder = 1
                ),
                FieldEntity(
                    calculatorId = "creatinine_clearance",
                    id = "gender",
                    name = "Gender",
                    type = FieldType.RADIO.name,
                    isInputField = true,
                    options = """["Male", "Female"]""", // JSON string
                    displayOrder = 2
                ),
                FieldEntity(
                    calculatorId = "creatinine_clearance",
                    id = "serum_creatinine",
                    name = "Serum Creatinine",
                    type = FieldType.NUMBER.name,
                    isInputField = true,
                    units = "mg/dL",
                    minValue = 0.1,
                    maxValue = 20.0,
                    defaultValue = "1.0",
                    displayOrder = 3
                ),
                // Output field
                FieldEntity(
                    calculatorId = "creatinine_clearance",
                    id = "creatinine_clearance",
                    name = "Creatinine Clearance",
                    type = FieldType.NUMBER.name,
                    isInputField = false,
                    units = "mL/min",
                    displayOrder = 0
                )
            )
        )

        // Add MAP Calculator fields
        fields.addAll(
            listOf(
                // Input fields
                FieldEntity(
                    calculatorId = "map_calc",
                    id = "systolic",
                    name = "Systolic BP",
                    type = FieldType.NUMBER.name,
                    isInputField = true,
                    units = "mmHg",
                    minValue = 40.0,
                    maxValue = 300.0,
                    defaultValue = "120",
                    displayOrder = 0
                ),
                FieldEntity(
                    calculatorId = "map_calc",
                    id = "diastolic",
                    name = "Diastolic BP",
                    type = FieldType.NUMBER.name,
                    isInputField = true,
                    units = "mmHg",
                    minValue = 20.0,
                    maxValue = 200.0,
                    defaultValue = "80",
                    displayOrder = 1
                ),
                // Output field
                FieldEntity(
                    calculatorId = "map_calc",
                    id = "map",
                    name = "MAP",
                    type = FieldType.NUMBER.name,
                    isInputField = false,
                    units = "mmHg",
                    displayOrder = 0
                )
            )
        )

        // Add Pregnancy Calculator fields
        fields.addAll(
            listOf(
                // Input field
                FieldEntity(
                    calculatorId = "pregnancy_calc",
                    id = "lmp_date",
                    name = "Last Menstrual Period",
                    type = FieldType.TEXT.name,
                    isInputField = true,
                    displayOrder = 0
                ),
                // Output fields
                FieldEntity(
                    calculatorId = "pregnancy_calc",
                    id = "due_date",
                    name = "Due Date",
                    type = FieldType.TEXT.name,
                    isInputField = false,
                    displayOrder = 0
                ),
                FieldEntity(
                    calculatorId = "pregnancy_calc",
                    id = "current_ega",
                    name = "Current EGA",
                    type = FieldType.TEXT.name,
                    isInputField = false,
                    displayOrder = 1
                )
            )
        )

        // Add BMI & BSA Calculator fields
        fields.addAll(
            listOf(
                // Input fields
                FieldEntity(
                    calculatorId = "bmi_bsa_calc",
                    id = "height",
                    name = "Height",
                    type = FieldType.NUMBER.name,
                    isInputField = true,
                    units = "cm",
                    minValue = 50.0,
                    maxValue = 300.0,
                    defaultValue = "170",
                    displayOrder = 0
                ),
                FieldEntity(
                    calculatorId = "bmi_bsa_calc",
                    id = "weight",
                    name = "Weight",
                    type = FieldType.NUMBER.name,
                    isInputField = true,
                    units = "kg",
                    minValue = 20.0,
                    maxValue = 500.0,
                    defaultValue = "70",
                    displayOrder = 1
                ),
                // Output fields
                FieldEntity(
                    calculatorId = "bmi_bsa_calc",
                    id = "bmi",
                    name = "BMI",
                    type = FieldType.NUMBER.name,
                    isInputField = false,
                    units = "kg/m²",
                    displayOrder = 0
                ),
                FieldEntity(
                    calculatorId = "bmi_bsa_calc",
                    id = "bsa",
                    name = "BSA",
                    type = FieldType.NUMBER.name,
                    isInputField = false,
                    units = "m²",
                    displayOrder = 1
                )
            )
        )

        return fields
    }
}