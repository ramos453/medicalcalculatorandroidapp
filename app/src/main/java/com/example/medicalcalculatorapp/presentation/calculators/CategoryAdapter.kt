package com.example.medicalcalculatorapp.presentation.calculators

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medicalcalculatorapp.R
import com.example.medicalcalculatorapp.domain.model.CategoryWithCount

class CategoryAdapter(
    private val onCategoryClick: (CategoryWithCount?) -> Unit // null means "All Categories"
) : ListAdapter<CategoryWithCount, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    private var selectedCategoryId: String? = null

//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(android.R.layout.simple_list_item_1, parent, false)
//        return CategoryViewHolder(view)
//    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_filter, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setSelectedCategory(categoryId: String?) {
        selectedCategoryId = categoryId
        notifyDataSetChanged()
    }

//    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
//        private val textView: TextView = itemView.findViewById(android.R.id.text1)
//
//        init {
//            itemView.setOnClickListener {
//                val position = adapterPosition
//                if (position != RecyclerView.NO_POSITION) {
//                    onCategoryClick(getItem(position))
//                }
//            }
//        }
//
//        fun bind(categoryWithCount: CategoryWithCount) {
//            val isSelected = categoryWithCount.category.id == selectedCategoryId
//            textView.text = "${categoryWithCount.category.name} (${categoryWithCount.calculatorCount})"
//            textView.alpha = if (isSelected) 1.0f else 0.7f
//            textView.setBackgroundColor(
//                if (isSelected)
//                    itemView.context.getColor(R.color.primary_light)
//                else
//                    android.graphics.Color.TRANSPARENT
//            )
//        }
//    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val tvCalculatorCount: TextView = itemView.findViewById(R.id.tvCalculatorCount)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onCategoryClick(getItem(position))
                }
            }
        }

        fun bind(categoryWithCount: CategoryWithCount) {
            val isSelected = categoryWithCount.category.id == selectedCategoryId

            tvCategoryName.text = categoryWithCount.category.name
            tvCalculatorCount.text = "(${categoryWithCount.calculatorCount})"

            // Visual feedback for selection
            val alpha = if (isSelected) 1.0f else 0.7f
            tvCategoryName.alpha = alpha
            tvCalculatorCount.alpha = alpha

            // Background color for selection
            itemView.setBackgroundColor(
                if (isSelected)
                    itemView.context.getColor(R.color.primary_light)
                else
                    android.graphics.Color.TRANSPARENT
            )
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryWithCount>() {
        override fun areItemsTheSame(oldItem: CategoryWithCount, newItem: CategoryWithCount): Boolean {
            return oldItem.category.id == newItem.category.id
        }

        override fun areContentsTheSame(oldItem: CategoryWithCount, newItem: CategoryWithCount): Boolean {
            return oldItem == newItem
        }
    }
}