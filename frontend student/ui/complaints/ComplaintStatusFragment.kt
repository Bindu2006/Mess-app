package com.example.mess.ui.complaints

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.mess.data.FirebaseHelper
import com.example.mess.databinding.FragmentComplaintStatusBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ComplaintStatusFragment : Fragment() {

    private var _binding: FragmentComplaintStatusBinding? = null
    private val binding get() = _binding!!
    private lateinit var complaintsListener: ValueEventListener
    private lateinit var adapter: ComplaintStatusAdapter
    private var selectedCategory = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComplaintStatusBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupRealtimeUpdates()
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
                    try {
                        val id = complaintSnapshot.key ?: continue
                        val category = complaintSnapshot.child("category").getValue(String::class.java) ?: ""
                        val description = complaintSnapshot.child("description").getValue(String::class.java) ?: ""
                        val statusStr = complaintSnapshot.child("status").getValue(String::class.java) ?: ""
                        val date = complaintSnapshot.child("date").getValue(String::class.java) ?: ""
                        val validationStatus = complaintSnapshot.child("validationStatus").getValue(String::class.java) ?: "none"
                        
                        // Convert string status to enum
                        val status = try {
                            ComplaintStatus.valueOf(statusStr.uppercase())
                        } catch (e: IllegalArgumentException) {
                            ComplaintStatus.PENDING
                        }

                        complaints.add(
                            Complaint(
                                id = id,
                                category = category,
                                description = description,
                                status = status,
                                date = date,
                                validationStatus = validationStatus
                            )
                        )
                    } catch (e: Exception) {
                        Log.e("ComplaintStatusFragment", "Error parsing complaint", e)
                    }
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
        FirebaseHelper.complaintsRef.removeEventListener(complaintsListener)
        _binding = null
    }
} 