package com.example.myapplication.ui.contact

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.ContactAdapter
import com.example.myapplication.databinding.FragmentContactBinding
import com.example.myapplication.model.Contact
import com.example.myapplication.model.ContactData
import com.example.myapplication.model.FirebaseContact
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.android.material.snackbar.Snackbar

class ContactFragment : Fragment() {
    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance("https://mess-5306b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val contactsRef = database.getReference("contacts")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupFab()
        checkDatabaseConnection()
    }

    private fun setupRecyclerView() {
        binding.contactRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = ContactAdapter(ContactData.contacts)
        }
    }

    private fun setupFab() {
        binding.addContactFab.setOnClickListener {
            showAddContactDialog()
        }
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

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_contact, null)

        val designationInput = dialogView.findViewById<TextInputLayout>(R.id.designationInput).editText
        val nameInput = dialogView.findViewById<TextInputLayout>(R.id.nameInput).editText
        val emailInput = dialogView.findViewById<TextInputLayout>(R.id.emailInput).editText
        val phoneInput = dialogView.findViewById<TextInputLayout>(R.id.phoneInput).editText

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Contact")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val designation = designationInput?.text?.toString() ?: return@setPositiveButton
                val name = nameInput?.text?.toString() ?: return@setPositiveButton
                val email = emailInput?.text?.toString() ?: return@setPositiveButton
                val phone = phoneInput?.text?.toString() ?: return@setPositiveButton

                if (designation.isNotBlank() && name.isNotBlank() && 
                    email.isNotBlank() && phone.isNotBlank()) {
                    
                    // First add locally
                    val contact = Contact(designation, name, email, phone)
                    ContactData.contacts.add(contact)
                    binding.contactRecyclerView.adapter?.notifyItemInserted(ContactData.contacts.size - 1)

                    // Then push to Firebase
                    val firebaseContact = FirebaseContact(designation, name, email, phone)
                    contactsRef.push().setValue(firebaseContact)
                        .addOnSuccessListener {
                            Log.d("ContactFragment", "Contact added to Firebase successfully")
                            Snackbar.make(binding.root, "Contact added successfully", Snackbar.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("ContactFragment", "Error adding contact to Firebase", e)
                            Snackbar.make(binding.root, "Error adding contact", Snackbar.LENGTH_SHORT).show()
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 