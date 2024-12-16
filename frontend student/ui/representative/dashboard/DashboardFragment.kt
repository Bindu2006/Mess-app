package com.example.mess.ui.representative.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mess.data.FirebaseHelper
import com.example.mess.databinding.FragmentDashboardBinding
import com.example.mess.ui.complaints.Complaint
import com.example.mess.ui.complaints.ComplaintStatus
import kotlinx.coroutines.launch

class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    private lateinit var complaintAdapter: ComplaintManagementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        setupRecyclerView()
        loadComplaints()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.complaintsHeader.text = "New Complaints"
        complaintAdapter = ComplaintManagementAdapter(
            complaints = emptyList(),
            onValidateClick = { complaint -> handleValidateComplaint(complaint) },
            onDenyClick = { complaint -> handleDenyComplaint(complaint) }
        )
        binding.complaintsRecyclerView.adapter = complaintAdapter
    }

    private fun loadComplaints() {
        lifecycleScope.launch {
            try {
                val allComplaints = FirebaseHelper.getComplaints()
                val newComplaints = allComplaints.filter { 
                    it.status == ComplaintStatus.ACKNOWLEDGED 
                }
                complaintAdapter.updateComplaints(newComplaints)
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load complaints: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleValidateComplaint(complaint: Complaint) {
        lifecycleScope.launch {
            try {
                FirebaseHelper.updateComplaintValidation(complaint, "verified", ComplaintStatus.PENDING)
                loadComplaints() // Reload the list
                Toast.makeText(context, "Complaint validated", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to validate complaint: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleDenyComplaint(complaint: Complaint) {
        lifecycleScope.launch {
            try {
                FirebaseHelper.updateComplaintValidation(complaint, "denied", ComplaintStatus.RESOLVED)
                loadComplaints() // Reload the list
                Toast.makeText(context, "Complaint denied", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to deny complaint: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 