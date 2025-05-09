package com.example.medicalcalculatorapp.ui.theme.calculators

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.medicalcalculatorapp.R

class CalculatorListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Very simple placeholder view
        return TextView(requireContext()).apply {
            setText(R.string.calculators)
            textSize = 24f
            gravity = android.view.Gravity.CENTER
        }
    }
}