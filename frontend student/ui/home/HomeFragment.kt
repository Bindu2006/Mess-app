package com.example.mess.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mess.R
import com.example.mess.databinding.FragmentHomeBinding
import com.example.mess.databinding.ItemActiveComplaintBinding
import com.example.mess.ui.complaints.Complaint
import com.example.mess.ui.complaints.ComplaintStatus
import com.example.mess.data.FirebaseHelper
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var activeComplaintsAdapter: ActiveComplaintsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        
        // Add click listener to the menu card
        binding.menuCard.setOnClickListener {
            findNavController().navigate(R.id.nav_weekly_menu)
        }
        
        displayCurrentMenu()
        setupActiveComplaints()
        return binding.root
    }

    private fun displayCurrentMenu() {
        lifecycleScope.launch {
            try {
                val (mealTime, menuItems) = FirebaseHelper.getMenuForCurrentTime()
                
                // Set meal icon based on time
                val mealIcon = when (mealTime) {
                    "breakfast" -> R.drawable.ic_meal_breakfast
                    "lunch" -> R.drawable.ic_meal_lunch
                    "snacks" -> R.drawable.ic_meal_snacks
                    else -> R.drawable.ic_meal_dinner
                }
                
                binding.mealIcon.setImageResource(mealIcon)
                binding.currentTimeSlot.text = mealTime.capitalize()
                
                if (menuItems.isEmpty()) {
                    binding.menuTextView.text = "No menu items available"
                } else {
                    // Format menu items with bullet points
                    val formattedMenu = menuItems.joinToString("\n") { "• $it" }
                    binding.menuTextView.text = formattedMenu
                }
                
            } catch (e: Exception) {
                Log.e("HomeFragment", "Error loading menu: ${e.message}", e)
                Toast.makeText(
                    context, 
                    "Failed to load menu: ${e.message}", 
                    Toast.LENGTH_SHORT
                ).show()
                binding.menuTextView.text = "Menu not available"
            }
        }
    }

    private fun setupActiveComplaints() {
        lifecycleScope.launch {
            try {
                val allComplaints = FirebaseHelper.getComplaints()
                // Filter for active complaints (ACKNOWLEDGED or IN_PROGRESS)
                val activeComplaints = allComplaints.filter { 
                    it.status == ComplaintStatus.ACKNOWLEDGED || 
                    it.status == ComplaintStatus.IN_PROGRESS 
                }
                
                activeComplaintsAdapter = ActiveComplaintsAdapter(activeComplaints) {
                    findNavController().navigate(R.id.nav_complaint_status)
                }
                binding.activeComplaintsRecyclerView.adapter = activeComplaintsAdapter
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load complaints: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class ActiveComplaintsAdapter(
    private val complaints: List<Complaint>,
    private val onComplaintClick: () -> Unit
) : RecyclerView.Adapter<ActiveComplaintsAdapter.ComplaintViewHolder>() {

    inner class ComplaintViewHolder(private val binding: ItemActiveComplaintBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                onComplaintClick()
            }
        }

        fun bind(complaint: Complaint) {
            binding.apply {
                complaintTitle.text = "${complaint.category}: ${complaint.description}"
                complaintStatus.text = when (complaint.status) {
                    ComplaintStatus.ACKNOWLEDGED -> "New"
                    ComplaintStatus.IN_PROGRESS -> "In Progress"
                    else -> ""
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val binding = ItemActiveComplaintBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ComplaintViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        holder.bind(complaints[position])
    }

    override fun getItemCount() = complaints.size
}