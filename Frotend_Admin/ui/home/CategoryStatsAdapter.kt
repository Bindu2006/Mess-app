package com.example.myapplication.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.ItemCategoryStatsBinding

data class CategoryStat(
    val title: String,
    val total: Int,
    val resolved: Int
)

class CategoryStatsAdapter(
    private val stats: List<CategoryStat>
) : RecyclerView.Adapter<CategoryStatsAdapter.ViewHolder>() {

    inner class ViewHolder(
        private val binding: ItemCategoryStatsBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(stat: CategoryStat) {
            binding.apply {
                categoryTitle.text = stat.title
                totalComplaints.text = "Total Complaints: ${stat.total}"
                resolvedComplaints.text = "Resolved: ${stat.resolved}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryStatsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(stats[position])
    }

    override fun getItemCount() = stats.size
} 