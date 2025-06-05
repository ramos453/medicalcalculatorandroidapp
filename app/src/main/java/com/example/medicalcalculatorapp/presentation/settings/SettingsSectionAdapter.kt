package com.example.medicalcalculatorapp.presentation.settings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.medicalcalculatorapp.databinding.ItemSettingsSectionBinding

class SettingsSectionAdapter(
    private val onSectionClick: (SettingsSection) -> Unit
) : ListAdapter<SettingsSection, SettingsSectionAdapter.SettingsSectionViewHolder>(SettingsSectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SettingsSectionViewHolder {
        val binding = ItemSettingsSectionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SettingsSectionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SettingsSectionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SettingsSectionViewHolder(
        private val binding: ItemSettingsSectionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSectionClick(getItem(position))
                }
            }
        }

        fun bind(section: SettingsSection) {
            binding.apply {
                tvSectionTitle.text = section.title
                tvSectionDescription.text = section.description
                ivSectionIcon.setImageResource(section.iconRes)

                // Show/hide status badge
                if (section.hasStatus && section.statusText.isNotEmpty()) {
                    layoutBadge.visibility = View.VISIBLE
                    chipStatus.text = section.statusText
                } else {
                    layoutBadge.visibility = View.GONE
                }
            }
        }
    }

    private class SettingsSectionDiffCallback : DiffUtil.ItemCallback<SettingsSection>() {
        override fun areItemsTheSame(oldItem: SettingsSection, newItem: SettingsSection): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SettingsSection, newItem: SettingsSection): Boolean {
            return oldItem == newItem
        }
    }
}