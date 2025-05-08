package com.example.medicalcalculatorapp

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.medicalcalculatorapp.data.model.CalculationResult
import com.example.medicalcalculatorapp.data.model.CalculatorField
import com.example.medicalcalculatorapp.data.model.Category
import com.example.medicalcalculatorapp.data.model.FieldType
import com.example.medicalcalculatorapp.data.model.MedicalCalculator
import com.example.medicalcalculatorapp.databinding.ActivityTestBinding

class TestActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTestBinding
    private var isShowingSplash = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Start with splash screen
        setContentView(R.layout.fragment_splash)

        // After delay, switch to main test activity content
        Handler(Looper.getMainLooper()).postDelayed({
            showMainContent()
        }, 2000) // 2 second delay
    }

    private fun showMainContent() {
        // Initialize binding and set the original layout
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        isShowingSplash = false

        // Test our data models to make sure they're working
        val testModels = createTestModels()

        // Set up button click listener
        binding.testButton.setOnClickListener {
            // Display a toast with info from our test models
            val message = "Test models created: ${testModels.size} calculators"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun createTestModels(): List<MedicalCalculator> {
        // Create test categories
        val generalCategory = Category(
            id = "general",
            name = "General",
            description = "General medical calculators",
            iconResId = R.drawable.ic_launcher_foreground
        )

        val cardioCategory = Category(
            id = "cardio",
            name = "Cardiology",
            description = "Cardiovascular calculators",
            iconResId = R.drawable.ic_launcher_foreground
        )

        // Create test calculator fields
        val heightField = CalculatorField(
            id = "height",
            name = "Height",
            type = FieldType.NUMBER,
            units = "cm",
            minValue = 50.0,
            maxValue = 300.0,
            defaultValue = "170"
        )

        val weightField = CalculatorField(
            id = "weight",
            name = "Weight",
            type = FieldType.NUMBER,
            units = "kg",
            minValue = 20.0,
            maxValue = 500.0,
            defaultValue = "70"
        )

        val bmiResult = CalculatorField(
            id = "bmi",
            name = "BMI",
            type = FieldType.NUMBER,
            units = "kg/m²"
        )

        val categoryField = CalculatorField(
            id = "category",
            name = "Category",
            type = FieldType.TEXT
        )

        // Create test calculators
        val bmiCalculator = MedicalCalculator(
            id = "bmi_calc",
            name = "BMI Calculator",
            description = "Calculate Body Mass Index based on height and weight",
            category = generalCategory.id,
            inputFields = listOf(heightField, weightField),
            resultFields = listOf(bmiResult, categoryField)
        )

        val calculationResult = CalculationResult(
            calculatorId = bmiCalculator.id,
            inputValues = mapOf(
                "height" to "180",
                "weight" to "75"
            ),
            resultValues = mapOf(
                "bmi" to "23.15",
                "category" to "Normal weight"
            )
        )

        // Return a list of test calculators
        return listOf(bmiCalculator)
    }

    // Handle back button press - prevents closing the app while in splash screen
    override fun onBackPressed() {
        if (!isShowingSplash) {
            super.onBackPressed()
        }
    }
}