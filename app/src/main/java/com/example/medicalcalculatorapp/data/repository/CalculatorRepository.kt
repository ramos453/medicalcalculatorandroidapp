package com.example.medicalcalculatorapp.data.repository

import com.example.medicalcalculatorapp.domain.model.CalculatorField
import com.example.medicalcalculatorapp.domain.model.FieldType
import com.example.medicalcalculatorapp.domain.model.MedicalCalculator

class CalculatorRepository {

    fun getCalculators(): List<MedicalCalculator> {
        // In a real professional app, this would come from a database or API
        return listOf(
            // BMI Calculator
            MedicalCalculator(
                id = "bmi_calc",
                name = "BMI Calculator",
                description = "Calculate Body Mass Index based on height and weight",
                category = "general",
                inputFields = listOf(
                    CalculatorField(
                        id = "height",
                        name = "Height",
                        type = FieldType.NUMBER,
                        units = "cm",
                        minValue = 50.0,
                        maxValue = 300.0,
                        defaultValue = "170"
                    ),
                    CalculatorField(
                        id = "weight",
                        name = "Weight",
                        type = FieldType.NUMBER,
                        units = "kg",
                        minValue = 20.0,
                        maxValue = 500.0,
                        defaultValue = "70"
                    )
                ),
                resultFields = listOf(
                    CalculatorField(
                        id = "bmi",
                        name = "BMI",
                        type = FieldType.NUMBER,
                        units = "kg/m²"
                    ),
                    CalculatorField(
                        id = "category",
                        name = "Category",
                        type = FieldType.TEXT
                    )
                )
            ),

            // Creatinine Clearance
            MedicalCalculator(
                id = "creatinine_clearance",
                name = "Creatinine Clearance",
                description = "Estimates creatinine clearance (kidney function)",
                category = "renal",
                inputFields = listOf(
                    CalculatorField(
                        id = "age",
                        name = "Age",
                        type = FieldType.NUMBER,
                        units = "years",
                        minValue = 18.0,
                        maxValue = 120.0,
                        defaultValue = "50"
                    ),
                    CalculatorField(
                        id = "weight",
                        name = "Weight",
                        type = FieldType.NUMBER,
                        units = "kg",
                        minValue = 20.0,
                        maxValue = 500.0,
                        defaultValue = "70"
                    ),
                    CalculatorField(
                        id = "gender",
                        name = "Gender",
                        type = FieldType.RADIO,
                        options = listOf("Male", "Female")
                    ),
                    CalculatorField(
                        id = "serum_creatinine",
                        name = "Serum Creatinine",
                        type = FieldType.NUMBER,
                        units = "mg/dL",
                        minValue = 0.1,
                        maxValue = 20.0,
                        defaultValue = "1.0"
                    )
                ),
                resultFields = listOf(
                    CalculatorField(
                        id = "creatinine_clearance",
                        name = "Creatinine Clearance",
                        type = FieldType.NUMBER,
                        units = "mL/min"
                    )
                ),
                isFavorite = true
            ),

            // Mean Arterial Pressure
            MedicalCalculator(
                id = "map_calc",
                name = "Mean Arterial Pressure (MAP)",
                description = "Calculates MAP.",
                category = "cardiology",
                inputFields = listOf(
                    CalculatorField(
                        id = "systolic",
                        name = "Systolic BP",
                        type = FieldType.NUMBER,
                        units = "mmHg",
                        minValue = 40.0,
                        maxValue = 300.0,
                        defaultValue = "120"
                    ),
                    CalculatorField(
                        id = "diastolic",
                        name = "Diastolic BP",
                        type = FieldType.NUMBER,
                        units = "mmHg",
                        minValue = 20.0,
                        maxValue = 200.0,
                        defaultValue = "80"
                    )
                ),
                resultFields = listOf(
                    CalculatorField(
                        id = "map",
                        name = "MAP",
                        type = FieldType.NUMBER,
                        units = "mmHg"
                    )
                )
            ),

            // Pregnancy Due Date
            MedicalCalculator(
                id = "pregnancy_calc",
                name = "Pregnancy Due Dates",
                description = "From LMP, EGA, or date of conception.",
                category = "obstetrics",
                inputFields = listOf(
                    CalculatorField(
                        id = "lmp_date",
                        name = "Last Menstrual Period",
                        type = FieldType.TEXT,
                        defaultValue = ""
                    )
                ),
                resultFields = listOf(
                    CalculatorField(
                        id = "due_date",
                        name = "Due Date",
                        type = FieldType.TEXT
                    ),
                    CalculatorField(
                        id = "current_ega",
                        name = "Current EGA",
                        type = FieldType.TEXT
                    )
                ),
                isFavorite = true
            ),

            // BMI & BSA
            MedicalCalculator(
                id = "bmi_bsa_calc",
                name = "BMI & BSA",
                description = "Categorizes obesity, assists some med dosing.",
                category = "general",
                inputFields = listOf(
                    CalculatorField(
                        id = "height",
                        name = "Height",
                        type = FieldType.NUMBER,
                        units = "cm",
                        minValue = 50.0,
                        maxValue = 300.0,
                        defaultValue = "170"
                    ),
                    CalculatorField(
                        id = "weight",
                        name = "Weight",
                        type = FieldType.NUMBER,
                        units = "kg",
                        minValue = 20.0,
                        maxValue = 500.0,
                        defaultValue = "70"
                    )
                ),
                resultFields = listOf(
                    CalculatorField(
                        id = "bmi",
                        name = "BMI",
                        type = FieldType.NUMBER,
                        units = "kg/m²"
                    ),
                    CalculatorField(
                        id = "bsa",
                        name = "BSA",
                        type = FieldType.NUMBER,
                        units = "m²"
                    )
                )
            )
        )
    }
}