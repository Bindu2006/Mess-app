package com.example.myapplication.ui.grievances

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.GrievanceCategoryAdapter
import com.example.myapplication.databinding.FragmentGrievancesBinding
import com.example.myapplication.model.FirebaseModels
import com.example.myapplication.model.Grievance
import com.example.myapplication.model.GrievanceStatus
import com.example.myapplication.model.FirebaseGrievance
import com.example.myapplication.model.GrievanceCategory
import com.example.myapplication.model.GrievanceItem
import com.example.myapplication.model.SubCategory
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*

object GrievanceCategories {
    val categories = mutableListOf<GrievanceCategory>()
}

class GrievancesFragment : Fragment() {

    private var _binding: FragmentGrievancesBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance("https://mess-5306b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val complaintsRef = database.getReference("complaints")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGrievancesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadComplaints()
        checkDatabaseConnection()
    }

    private fun setupRecyclerView() {
        binding.grievancesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GrievanceCategoryAdapter(GrievanceCategories.categories) { category ->
                val action = GrievancesFragmentDirections
                    .actionNavGrievancesToGrievanceDetails(category)
                findNavController().navigate(action)
            }
        }
    }

    private fun loadComplaints() {
        Log.d("GrievancesFragment", "Starting to load complaints")
        
        complaintsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("GrievancesFragment", "Number of complaints: ${snapshot.childrenCount}")
                
                val categoryMap = mutableMapOf<String, MutableList<Grievance>>()
                
                for (childSnapshot in snapshot.children) {
                    try {
                        val firebaseGrievance = childSnapshot.getValue(FirebaseGrievance::class.java)
                        Log.d("GrievancesFragment", "Complaint data: $firebaseGrievance")
                        
                        firebaseGrievance?.let {
                            val status = try {
                                GrievanceStatus.valueOf(it.status.uppercase())
                            } catch (e: IllegalArgumentException) {
                                GrievanceStatus.PENDING
                            }

                            if (status != GrievanceStatus.ACKNOWLEDGED) {
                                val category = it.category ?: "Other"
                                val complaints = categoryMap.getOrPut(category) { mutableListOf() }
                                
                                complaints.add(
                                    Grievance(
                                        id = childSnapshot.key ?: "",
                                        title = it.title,
                                        description = it.description,
                                        status = status,
                                        timestamp = it.timestamp,
                                        userId = it.userId,
                                        userName = it.userName
                                    )
                                )
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("GrievancesFragment", "Error parsing complaint", e)
                    }
                }
                
                // Convert to GrievanceCategory list
                val categories = categoryMap.map { (categoryName, complaints) ->
                    GrievanceCategory(
                        id = categoryName.hashCode(),
                        title = categoryName,
                        subcategories = listOf(
                            SubCategory(
                                id = 1,
                                title = "All Complaints",
                                complaints = complaints.map { grievance ->
                                    GrievanceItem(
                                        id = grievance.id.toIntOrNull() ?: 0,
                                        title = grievance.title,
                                        description = grievance.description,
                                        status = grievance.status,
                                        dateSubmitted = grievance.timestamp.toString(),
                                        userName = grievance.userName
                                    )
                                }
                            )
                        )
                    )
                }

                GrievanceCategories.categories.clear()
                GrievanceCategories.categories.addAll(categories)
                
                binding.grievancesRecyclerView.adapter?.notifyDataSetChanged()
                Log.d("GrievancesFragment", "Loaded ${categories.size} categories")
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("GrievancesFragment", "Error loading complaints", error.toException())
                Snackbar.make(binding.root, "Error loading complaints", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun checkDatabaseConnection() {
        val connectedRef = database.getReference(".info/connected")
        connectedRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val connected = snapshot.getValue(Boolean::class.java) ?: false
                if (connected) {
                    Log.d("Database", "Connected to Firebase Database")
                    Snackbar.make(binding.root, "Connected to database", Snackbar.LENGTH_SHORT).show()
                } else {
                    Log.e("Database", "Not connected to Firebase Database")
                    Snackbar.make(binding.root, "Database connection failed", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database", "Listener was cancelled: ${error.message}")
                Snackbar.make(binding.root, "Database error: ${error.message}", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 