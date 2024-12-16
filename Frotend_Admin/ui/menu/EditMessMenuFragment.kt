package com.example.myapplication.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.MessMenuAdapter
import com.example.myapplication.databinding.FragmentEditMessMenuBinding
import com.example.myapplication.model.MessMenuData
import com.example.myapplication.model.MessMenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class EditMessMenuFragment : Fragment() {
    private var _binding: FragmentEditMessMenuBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance("https://mess-5306b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val menuRef = database.getReference("menu")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditMessMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadMenuFromFirebase()
    }

    private fun setupRecyclerView() {
        binding.menuRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = MessMenuAdapter(
                menuItems = MessMenuData.menuItems,
                isEditing = true  // Always in edit mode
            ) { updatedItem ->
                // Save changes to Firebase
                val menuMap = mapOf(
                    updatedItem.day to mapOf(
                        "breakfast" to updatedItem.breakfast,
                        "lunch" to updatedItem.lunch,
                        "snacks" to updatedItem.snacks,
                        "dinner" to updatedItem.dinner
                    )
                )
                
                menuRef.updateChildren(menuMap)
                    .addOnSuccessListener {
                        Snackbar.make(binding.root, "Changes saved", Snackbar.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Snackbar.make(binding.root, "Failed to save: ${e.message}", Snackbar.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun loadMenuFromFirebase() {
        menuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    MessMenuData.menuItems.clear()
                    
                    for (daySnapshot in snapshot.children) {
                        val day = daySnapshot.key ?: continue
                        
                        val breakfastList = (daySnapshot.child("breakfast").value as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
                        val lunchList = (daySnapshot.child("lunch").value as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
                        val snacksList = (daySnapshot.child("snacks").value as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()
                        val dinnerList = (daySnapshot.child("dinner").value as? List<*>)?.mapNotNull { it.toString() } ?: emptyList()

                        MessMenuData.menuItems.add(
                            MessMenuItem(
                                day = day,
                                breakfast = breakfastList,
                                lunch = lunchList,
                                snacks = snacksList,
                                dinner = dinnerList
                            )
                        )
                    }
                    
                    binding.menuRecyclerView.adapter?.notifyDataSetChanged()
                } catch (e: Exception) {
                    Snackbar.make(binding.root, "Error loading menu: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(binding.root, "Error loading menu: ${error.message}", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 