package com.example.medicalcalculatorapp.presentation.calculators.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.medicalcalculatorapp.databinding.FragmentCalculatorDetailBinding
import com.example.medicalcalculatorapp.di.AppDependencies
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.example.medicalcalculatorapp.presentation.util.FormGenerator
import android.widget.LinearLayout
import com.example.medicalcalculatorapp.domain.model.MedicalCalculator
import com.example.medicalcalculatorapp.domain.model.FieldType


class CalculatorDetailFragment : Fragment() {

    private var _binding: FragmentCalculatorDetailBinding? = null
    private val binding get() = _binding!!

    // Get calculatorId from arguments
    private val calculatorId: String by lazy {
        arguments?.getString("calculatorId") ?: ""
    }

    // Initialize ViewModel with Factory
    private val viewModel: CalculatorDetailViewModel by viewModels {
        CalculatorDetailViewModel.Factory(
            calculatorId = calculatorId,
            calculatorRepository = AppDependencies.provideCalculatorRepository(requireContext()),
            historyRepository = AppDependencies.provideHistoryRepository(requireContext()),
            calculatorService = AppDependencies.provideCalculatorService() // NEW: Add this line
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalculatorDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    private lateinit var formGenerator: FormGenerator
    private var inputFieldViews = mapOf<String, View>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize form generator
        formGenerator = FormGenerator(requireContext())

        // Setup toolbar
        setupToolbar()

        // Observe ViewModel data
        setupObservers()

        // Setup button listeners
        setupListeners()
    }

    private fun setupToolbar() {
        // We'll update title when calculator data is loaded
    }

    private fun setupListeners() {
        binding.btnCalculate.setOnClickListener {
            // Validate inputs before calculation
            if (validateInputs()) {
                viewModel.performCalculation()
            }
        }

        binding.btnReset.setOnClickListener {
            viewModel.resetInputs()
            // Reset form fields to default values
            viewModel.calculator.value?.let { calculator ->
                generateInputFields(calculator)
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Basic validation - check if required fields have values
        var isValid = true

        viewModel.calculator.value?.inputFields?.forEach { field ->
            val value = viewModel.inputValues.value[field.id] ?: ""

            if (value.isBlank()) {
                // Show error for empty required fields
                Toast.makeText(
                    requireContext(),
                    "Please enter a value for ${field.name}",
                    Toast.LENGTH_SHORT
                ).show()
                isValid = false
                return@forEach
            }

            // For number fields, check if the value is a valid number
            if (field.type == FieldType.NUMBER) {
                try {
                    val numValue = value.toDouble()

                    // Check min/max range if defined
                    field.minValue?.let { min ->
                        if (numValue < min) {
                            Toast.makeText(
                                requireContext(),
                                "${field.name} must be at least $min",
                                Toast.LENGTH_SHORT
                            ).show()
                            isValid = false
                            return@forEach
                        }
                    }

                    field.maxValue?.let { max ->
                        if (numValue > max) {
                            Toast.makeText(
                                requireContext(),
                                "${field.name} must not exceed $max",
                                Toast.LENGTH_SHORT
                            ).show()
                            isValid = false
                            return@forEach
                        }
                    }

                } catch (e: NumberFormatException) {
                    Toast.makeText(
                        requireContext(),
                        "Please enter a valid number for ${field.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    isValid = false
                    return@forEach
                }
            }
        }

        return isValid
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Observe calculator data
                launch {
                    viewModel.calculator.collectLatest { calculator ->
                        calculator?.let {
                            // Update UI with calculator info
                            binding.tvCalculatorName.text = it.name
                            binding.tvCalculatorDescription.text = it.description
                            activity?.title = it.name

                            // We'll implement form generation in the next step
                            generateInputFields(it)
                        }
                    }
                }

                // Observe input values
                launch {
                    viewModel.inputValues.collectLatest { values ->
                        // Update input fields if they change in ViewModel
                        // (We don't need to do anything here as fields update directly)
                    }
                }

                // Observe result values
                launch {
                    viewModel.resultValues.collectLatest { results ->
                        // Update result fields
                        viewModel.calculator.value?.let { calculator ->
                            formGenerator.generateResultFields(
                                calculator.resultFields,
                                binding.resultFieldsContainer,
                                results
                            )
                        }
                    }
                }

                // Observe loading state
                launch {
                    viewModel.isLoading.collectLatest { isLoading ->
                        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
                        binding.inputFieldsContainer.visibility = if (isLoading) View.GONE else View.VISIBLE
                        binding.btnCalculate.isEnabled = !isLoading
                        binding.btnReset.isEnabled = !isLoading
                    }
                }

                // Observe error messages
                launch {
                    viewModel.error.collectLatest { errorMessage ->
                        errorMessage?.let {
                            Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                        }
                    }
                }

                // Observe calculation results visibility
                launch {
                    viewModel.calculationPerformed.collectLatest { performed ->
                        binding.resultsCard.visibility = if (performed) View.VISIBLE else View.GONE
                    }
                }

                // Observe interpretation
                launch {
                    viewModel.interpretation.collectLatest { interpretation ->
                        binding.tvInterpretation.text = interpretation
                    }
                }
            }
        }
    }

    private fun generateInputFields(calculator: MedicalCalculator) {
        // Generate input fields using our form generator
        inputFieldViews = formGenerator.generateInputFields(
            fields = calculator.inputFields,
            container = binding.inputFieldsContainer,
            initialValues = viewModel.inputValues.value
        ) { fieldId, value ->
            // Update ViewModel when input values change
            viewModel.updateInputValue(fieldId, value)
        }
    }

//    private fun setupListeners() {
//        binding.btnCalculate.setOnClickListener {
//            viewModel.performCalculation()
//        }
//
//        binding.btnReset.setOnClickListener {
//            viewModel.resetInputs()
//            // We'll update input fields in next step
//        }
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}