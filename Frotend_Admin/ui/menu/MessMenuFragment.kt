package com.example.myapplication.ui.menu

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.MessMenuAdapter
import com.example.myapplication.databinding.FragmentMessMenuBinding
import com.example.myapplication.model.FirebaseModels.FirebaseMessMenu
import com.example.myapplication.model.MessMenuData
import com.example.myapplication.model.MessMenuItem
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.AdapterView
import com.google.firebase.auth.FirebaseAuth
import android.app.AlertDialog
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.app.Activity

class MessMenuFragment : Fragment() {
    private var _binding: FragmentMessMenuBinding? = null
    private val binding get() = _binding!!
    private var isEditing = false
    private val database = FirebaseDatabase.getInstance("https://mess-5306b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val menuRef = database.getReference("menu")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMessMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Check if user is admin
        val currentUser = FirebaseAuth.getInstance().currentUser
        val isAdmin = currentUser?.email == "admin@gmail.com" // Replace with your admin check
        
        binding.editButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
        
        binding.editButton.setOnClickListener {
            isEditing = true
            binding.editButton.visibility = View.GONE
            binding.saveButton.visibility = View.VISIBLE
            (binding.menuRecyclerView.adapter as? MessMenuAdapter)?.setEditMode(true)
            Snackbar.make(binding.root, "You can now edit the menu", Snackbar.LENGTH_SHORT).show()
        }

        binding.saveButton.setOnClickListener {
            isEditing = false
            binding.editButton.visibility = View.VISIBLE
            binding.saveButton.visibility = View.GONE
            (binding.menuRecyclerView.adapter as? MessMenuAdapter)?.setEditMode(false)
            
            // Final save of all changes
            saveMenuToFirebase()
            
            // Hide keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
        }

        setupRecyclerView()
        loadMenuFromFirebase()
    }

    private fun setupToolbar() {
        binding.editButton.setOnClickListener {
            isEditing = true
            binding.editButton.visibility = GONE
            binding.saveButton.visibility = VISIBLE
            binding.menuRecyclerView.adapter?.notifyDataSetChanged()
            Snackbar.make(binding.root, "Click on any field to edit", Snackbar.LENGTH_LONG).show()
        }

        binding.saveButton.setOnClickListener {
            isEditing = false
            binding.editButton.visibility = VISIBLE
            binding.saveButton.visibility = GONE
            
            // Save to Firebase
            saveMenuToFirebase()
            
            // Hide keyboard
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
            
            binding.menuRecyclerView.adapter?.notifyDataSetChanged()
        }
    }

    private fun setupRecyclerView() {
        binding.menuRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = MessMenuAdapter(
                menuItems = MessMenuData.menuItems,
                isEditing = isEditing
            ) { updatedItem ->
                // Save individual item changes to Firebase
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
                        Log.d("MessMenuFragment", "Menu item updated successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("MessMenuFragment", "Failed to update menu item", e)
                        Snackbar.make(binding.root, "Failed to save changes", Snackbar.LENGTH_SHORT).show()
                    }
            }
        }
    }

    private fun saveMenuToFirebase() {
        try {
            val menuMap = mutableMapOf<String, Map<String, List<String>>>()
            
            MessMenuData.menuItems.forEach { menuItem ->
                menuMap[menuItem.day] = mapOf(
                    "breakfast" to menuItem.breakfast,
                    "lunch" to menuItem.lunch,
                    "snacks" to menuItem.snacks,
                    "dinner" to menuItem.dinner
                )
            }

            menuRef.setValue(menuMap)
                .addOnSuccessListener {
                    Snackbar.make(binding.root, "Menu saved successfully", Snackbar.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Snackbar.make(binding.root, "Failed to save menu: ${e.message}", Snackbar.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e("MessMenuFragment", "Error saving menu", e)
            Snackbar.make(binding.root, "Error saving menu", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun loadMenuFromFirebase() {
        Log.d("MessMenuFragment", "Starting to load menu from Firebase")
        menuRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                try {
                    MessMenuData.menuItems.clear()
                    
                    for (daySnapshot in snapshot.children) {
                        val day = daySnapshot.key ?: continue
                        
                        // Fix: Add missing lunchList variable
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
                    Log.e("MessMenuFragment", "Error loading menu", e)
                    Snackbar.make(binding.root, "Error loading menu: ${e.message}", Snackbar.LENGTH_LONG).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("MessMenuFragment", "Database error: ${error.message}", error.toException())
                Snackbar.make(binding.root, "Error loading menu: ${error.message}", Snackbar.LENGTH_LONG).show()
            }
        })
    }

    private fun initializeMenuData() {
        val menuMap = mutableMapOf<String, Map<String, List<String>>>()
        
        // Use the existing MessMenuData to initialize Firebase
        MessMenuData.menuItems.forEach { menuItem ->
            menuMap[menuItem.day] = mapOf(
                "breakfast" to menuItem.breakfast,
                "lunch" to menuItem.lunch,
                "snacks" to menuItem.snacks,
                "dinner" to menuItem.dinner
            )
        }

        // Push to Firebase
        menuRef.setValue(menuMap)
            .addOnSuccessListener {
                Log.d("MessMenuFragment", "Menu data initialized successfully")
            }
            .addOnFailureListener { e ->
                Log.e("MessMenuFragment", "Failed to initialize menu data", e)
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 