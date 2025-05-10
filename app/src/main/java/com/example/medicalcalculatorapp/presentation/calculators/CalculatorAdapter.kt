package com.example.medicalcalculatorapp.presentation.calculators

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medicalcalculatorapp.domain.model.MedicalCalculator
import com.example.medicalcalculatorapp.databinding.ItemCalculatorBinding

class CalculatorAdapter(
    private val onItemClick: (MedicalCalculator) -> Unit,
    private val onFavoriteClick: (MedicalCalculator) -> Unit
) : ListAdapter<MedicalCalculator, CalculatorAdapter.CalculatorViewHolder>(CalculatorDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalculatorViewHolder {
        val binding = ItemCalculatorBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalculatorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalculatorViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

//    inner class CalculatorViewHolder(
//        private val binding: ItemCalculatorBinding
//    ) : RecyclerView.ViewHolder(binding.root) {
//
//        init {
//            binding.root.setOnClickListener {
//                val position = bindingAdapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onItemClick(getItem(position))
//                }
//            }
//
//            binding.btnFavorite.setOnClickListener {
//                val position = bindingAdapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onFavoriteClick(getItem(position))
//                }
//            }
//        }
//
//        fun bind(calculator: MedicalCalculator) {
//            binding.tvCalculatorName.text = calculator.name
//            binding.tvCalculatorDescription.text = calculator.description
//            // Set favorite icon based on isFavorite property
//            binding.btnFavorite.setImageResource(
//                if (calculator.isFavorite) {
//                    android.R.drawable.btn_star_big_on
//                } else {
//                    android.R.drawable.btn_star_big_off
//                }
//            )
//        }
//    }

    inner class CalculatorViewHolder(
        private val binding: ItemCalculatorBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition // Changed from bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }

            binding.btnFavorite.setOnClickListener {
                val position = adapterPosition // Changed from bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onFavoriteClick(getItem(position))
                }
            }
        }

        fun bind(calculator: MedicalCalculator) {
            binding.tvCalculatorName.text = calculator.name
            binding.tvCalculatorDescription.text = calculator.description
            // Set favorite icon based on isFavorite property
            binding.btnFavorite.setImageResource(
                if (calculator.isFavorite) {
                    android.R.drawable.btn_star_big_on
                } else {
                    android.R.drawable.btn_star_big_off
                }
            )
        }
    }

    private class CalculatorDiffCallback : DiffUtil.ItemCallback<MedicalCalculator>() {
        override fun areItemsTheSame(oldItem: MedicalCalculator, newItem: MedicalCalculator): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: MedicalCalculator, newItem: MedicalCalculator): Boolean {
            return oldItem == newItem
        }
    }
}