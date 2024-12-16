package com.example.mess.ui.complaints

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mess.data.FirebaseHelper
import com.example.mess.databinding.FragmentComplaintStatusBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch

class ComplaintStatusFragment : Fragment() {

    private var _binding: FragmentComplaintStatusBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ComplaintStatusAdapter
    private var complaintsListener: ValueEventListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComplaintStatusBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupRealtimeUpdates()
        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = ComplaintStatusAdapter(emptyList()) { complaint, action ->
            when (action) {
                ComplaintAction.CONFIRM_RESOLUTION -> {
                    when (complaint.status) {
                        ComplaintStatus.COMPLETED -> {
                            updateComplaintStatus(complaint, ComplaintStatus.RESOLVED)
                        }
                        else -> {
                            updateComplaintStatus(complaint, ComplaintStatus.COMPLETED)
                        }
                    }
                }
                ComplaintAction.REPORT_UNRESOLVED -> {
                    when (complaint.status) {
                        ComplaintStatus.COMPLETED -> {
                            updateComplaintStatus(complaint, ComplaintStatus.PENDING)
                        }
                        else -> {
                            updateComplaintStatus(complaint, ComplaintStatus.IN_PROGRESS)
                        }
                    }
                }
            }
        }
        binding.complaintsRecyclerView.adapter = adapter
    }

    private fun setupRealtimeUpdates() {
        complaintsListener = FirebaseHelper.complaintsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val complaints = mutableListOf<Complaint>()
                for (complaintSnapshot in snapshot.children) {
                    val category = complaintSnapshot.child("category").getValue(String::class.java) ?: ""
                    val description = complaintSnapshot.child("description").getValue(String::class.java) ?: ""
                    val statusStr = complaintSnapshot.child("status").getValue(String::class.java) ?: ""
                    val date = complaintSnapshot.child("date").getValue(String::class.java) ?: ""
                    
                    val status = try {
                        ComplaintStatus.valueOf(statusStr)
                    } catch (e: IllegalArgumentException) {
                        ComplaintStatus.ACKNOWLEDGED
                    }
                    
                    complaints.add(Complaint(category, description, status, date))
                }
                
                adapter.updateComplaints(complaints)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Failed to load complaints: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateComplaintStatus(complaint: Complaint, newStatus: ComplaintStatus) {
        lifecycleScope.launch {
            try {
                FirebaseHelper.updateComplaintStatus(complaint, newStatus)
                Toast.makeText(context, "Status updated successfully", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to update status: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Remove the listener when the view is destroyed
        complaintsListener?.let {
            FirebaseHelper.complaintsRef.removeEventListener(it)
        }
        _binding = null
    }
}

data class Complaint(
    val category: String,
    val description: String,
    var status: ComplaintStatus,
    val date: String
)

enum class ComplaintStatus {
    ACKNOWLEDGED,
    IN_PROGRESS,
    COMPLETED,
    PENDING,
    RESOLVED
}

enum class ComplaintAction {
    CONFIRM_RESOLUTION,
    REPORT_UNRESOLVED
} 