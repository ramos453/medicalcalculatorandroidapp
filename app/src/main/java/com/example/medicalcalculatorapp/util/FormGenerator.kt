// Create a new file at:
// app/src/main/java/com/example/medicalcalculatorapp/presentation/util/FormGenerator.kt

package com.example.medicalcalculatorapp.presentation.util

import android.content.Context
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.domain.model.CalculatorField
import com.example.medicalcalculatorapp.domain.model.FieldType
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Utility class for dynamically generating form elements based on calculator field types
 */
class FormGenerator(private val context: Context) {

    /**
     * Generates input fields for a calculator and adds them to the container
     * @return A map of generated input views for later reference
     */
    fun generateInputFields(
        fields: List<CalculatorField>,
        container: ViewGroup,
        initialValues: Map<String, String>,
        onValueChanged: (String, String) -> Unit
    ): Map<String, View> {
        // Clear existing views
        container.removeAllViews()

        val fieldViews = mutableMapOf<String, View>()

        for (field in fields) {
            val fieldView = when (field.type) {
                FieldType.NUMBER -> createNumberField(field, initialValues, onValueChanged)
                FieldType.TEXT -> createTextField(field, initialValues, onValueChanged)
                FieldType.DROPDOWN -> createDropdownField(field, initialValues, onValueChanged)
                FieldType.RADIO -> createRadioField(field, initialValues, onValueChanged)
                FieldType.CHECKBOX -> createCheckboxField(field, initialValues, onValueChanged)
            }

            fieldViews[field.id] = fieldView
            container.addView(fieldView)

            // Add some spacing
            val space = Space(context)
            space.layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                context.resources.getDimensionPixelSize(R.dimen.spacing_normal)
            )
            container.addView(space)
        }

        return fieldViews
    }

    /**
     * Generates result fields and adds them to the container
     */
    fun generateResultFields(
        fields: List<CalculatorField>,
        container: ViewGroup,
        resultValues: Map<String, String>
    ) {
        // Clear existing views
        container.removeAllViews()

        for (field in fields) {
            val value = resultValues[field.id] ?: ""

            val resultView = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // Field name
            val nameView = TextView(context).apply {
                text = field.name
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            // Field value
            val valueView = TextView(context).apply {
                val displayValue = if (field.units != null) "$value ${field.units}" else value
                text = displayValue
                textAlignment = View.TEXT_ALIGNMENT_VIEW_END
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            resultView.addView(nameView)
            resultView.addView(valueView)
            container.addView(resultView)

            // Add divider
            val divider = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1
                )
                setBackgroundColor(context.getColor(R.color.divider))
            }

            container.addView(divider)

            // Add spacing
            val space = Space(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    context.resources.getDimensionPixelSize(R.dimen.spacing_small)
                )
            }
            container.addView(space)
        }
    }

    private fun createNumberField(
        field: CalculatorField,
        initialValues: Map<String, String>,
        onValueChanged: (String, String) -> Unit
    ): View {
        val layout = LayoutInflater.from(context)
            .inflate(R.layout.item_input_number, null) as LinearLayout

        val inputLayout = layout.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = layout.findViewById<TextInputEditText>(R.id.editText)

        // Set label and hint
        val labelText = if (field.units != null) "${field.name} (${field.units})" else field.name
        inputLayout.hint = labelText

        // Set input type for numbers
        editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL

        // Set initial value
        editText.setText(initialValues[field.id] ?: field.defaultValue ?: "")

        // Set up text change listener
        editText.setOnTextChangedListener { text ->
            onValueChanged(field.id, text)
        }

        return layout
    }

    private fun createTextField(
        field: CalculatorField,
        initialValues: Map<String, String>,
        onValueChanged: (String, String) -> Unit
    ): View {
        val layout = LayoutInflater.from(context)
            .inflate(R.layout.item_input_text, null) as LinearLayout

        val inputLayout = layout.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = layout.findViewById<TextInputEditText>(R.id.editText)

        // Set label and hint
        inputLayout.hint = field.name

        // Set input type for text
        editText.inputType = InputType.TYPE_CLASS_TEXT

        // Set initial value
        editText.setText(initialValues[field.id] ?: field.defaultValue ?: "")

        // Set up text change listener
        editText.setOnTextChangedListener { text ->
            onValueChanged(field.id, text)
        }

        return layout
    }

    private fun createDropdownField(
        field: CalculatorField,
        initialValues: Map<String, String>,
        onValueChanged: (String, String) -> Unit
    ): View {
        // Get options from field
        val options = field.options ?: listOf()
        if (options.isEmpty()) {
            return TextView(context).apply { text = "Error: No options for dropdown ${field.name}" }
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Add label
        val label = TextView(context).apply {
            text = field.name
            setTextAppearance(android.R.style.TextAppearance_Medium)
        }
        layout.addView(label)

        // Add spinner (dropdown)
        val spinner = Spinner(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Create adapter for spinner
        val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, options)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        // Set initial selection
        val initialValue = initialValues[field.id] ?: field.defaultValue
        val initialIndex = options.indexOf(initialValue)
        if (initialIndex >= 0) {
            spinner.setSelection(initialIndex)
        }

        // Set selection listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedValue = options[position]
                onValueChanged(field.id, selectedValue)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }

        layout.addView(spinner)
        return layout
    }

    private fun createRadioField(
        field: CalculatorField,
        initialValues: Map<String, String>,
        onValueChanged: (String, String) -> Unit
    ): View {
        // Get options from field
        val options = field.options ?: listOf()
        if (options.isEmpty()) {
            return TextView(context).apply { text = "Error: No options for radio ${field.name}" }
        }

        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Add label
        val label = TextView(context).apply {
            text = field.name
            setTextAppearance(android.R.style.TextAppearance_Medium)
        }
        layout.addView(label)

        // Add radio group
        val radioGroup = RadioGroup(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            orientation = RadioGroup.VERTICAL
        }

        // Get initial value
        val initialValue = initialValues[field.id] ?: field.defaultValue

        // Add radio buttons for each option
        options.forEachIndexed { index, option ->
            val radioButton = RadioButton(context).apply {
                id = View.generateViewId()
                text = option
                isChecked = option == initialValue
            }
            radioGroup.addView(radioButton)

            // If this option is selected, update the value
            if (radioButton.isChecked) {
                onValueChanged(field.id, option)
            }
        }

        // Set listener for radio group
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            val selected = group.findViewById<RadioButton>(checkedId)
            onValueChanged(field.id, selected.text.toString())
        }

        layout.addView(radioGroup)
        return layout
    }

    private fun createCheckboxField(
        field: CalculatorField,
        initialValues: Map<String, String>,
        onValueChanged: (String, String) -> Unit
    ): View {
        val initialValue = initialValues[field.id] ?: field.defaultValue
        val isChecked = initialValue == "true"

        val checkbox = CheckBox(context).apply {
            text = field.name
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            this.isChecked = isChecked
        }

        // Set initial value
        onValueChanged(field.id, isChecked.toString())

        // Set listener
        checkbox.setOnCheckedChangeListener { _, checked ->
            onValueChanged(field.id, checked.toString())
        }

        return checkbox
    }

    // Extension function to simplify text change listener
    private fun TextInputEditText.setOnTextChangedListener(listener: (String) -> Unit) {
        this.addTextChangedListener(SimpleTextWatcher { text ->
            listener(text.toString())
        })
    }

    // Simple TextWatcher interface implementation
    private class SimpleTextWatcher(private val onTextChanged: (CharSequence) -> Unit) : android.text.TextWatcher {
        override fun afterTextChanged(s: android.text.Editable?) {
            // Not used
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            // Not used
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let { onTextChanged(it) }
        }
    }
}