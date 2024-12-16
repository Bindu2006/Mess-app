package com.example.myapplication.ui.home

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentHomeBinding
import com.example.myapplication.model.GrievanceCategories
import com.example.myapplication.model.GrievanceStatus
import com.example.myapplication.model.MessManagement
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance("https://mess-5306b-default-rtdb.asia-southeast1.firebasedatabase.app/")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPieChart()
        setupCategoryStats()
    }

    private fun setupPieChart() {
        // Get all complaints across all categories
        val allComplaints = GrievanceCategories.categories.flatMap { category ->
            category.subcategories.flatMap { it.complaints }
        }

        // Count complaints by status
        val acknowledged = allComplaints.count { it.status == GrievanceStatus.ACKNOWLEDGED }
        val inProgress = allComplaints.count { it.status == GrievanceStatus.IN_PROGRESS }
        val resolved = allComplaints.count { it.status == GrievanceStatus.RESOLVED }
        val total = allComplaints.size.toFloat()

        // Create pie chart entries
        val entries = mutableListOf<PieEntry>().apply {
            if (acknowledged > 0) add(PieEntry((acknowledged / total) * 100, "Acknowledged"))
            if (inProgress > 0) add(PieEntry((inProgress / total) * 100, "In Progress"))
            if (resolved > 0) add(PieEntry((resolved / total) * 100, "Resolved"))
        }

        // Set colors for each status
        val colors = listOf(
            resources.getColor(R.color.status_pending, null),     // Acknowledged
            resources.getColor(R.color.status_in_progress, null), // In Progress
            resources.getColor(R.color.status_resolved, null)     // Resolved
        )

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 14f
            valueTextColor = Color.WHITE
            valueLineColor = Color.WHITE
            valueLinePart1Length = 0.4f
            valueLinePart2Length = 0.4f
        }

        binding.statusPieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            transparentCircleRadius = 60f
            centerText = "Total\nComplaints\n$total"
            setCenterTextSize(16f)
            legend.textSize = 14f
            setEntryLabelColor(Color.BLACK)
            setEntryLabelTextSize(12f)
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    private fun setupCategoryStats() {
        val categoryStats = GrievanceCategories.categories.map { category ->
            val complaints = category.subcategories.flatMap { it.complaints }
            CategoryStat(
                title = category.title,
                total = complaints.size,
                resolved = complaints.count { it.status == GrievanceStatus.RESOLVED }
            )
        }

        binding.categoryStatsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = CategoryStatsAdapter(categoryStats)
        }
    }

    private fun updateMessManagementUI(messManagement: MessManagement) {
        // This function can stay but will be unused
        // Keeping it to avoid breaking any other potential references
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}