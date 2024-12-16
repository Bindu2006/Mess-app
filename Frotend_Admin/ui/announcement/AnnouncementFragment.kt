package com.example.myapplication.ui.announcement

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.R
import com.example.myapplication.adapter.AnnouncementAdapter
import com.example.myapplication.databinding.DialogMessManagementBinding
import com.example.myapplication.databinding.FragmentAnnouncementBinding
import com.example.myapplication.model.Announcement
import com.example.myapplication.model.AnnouncementData
import com.example.myapplication.model.FirebaseAnnouncement
import com.example.myapplication.model.MessManagement
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.android.material.snackbar.Snackbar
import android.widget.ArrayAdapter
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Spinner

class AnnouncementFragment : Fragment() {
    private var _binding: FragmentAnnouncementBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance("https://mess-5306b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val announcementsRef = database.getReference("announcements")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Check if user is admin
        val isAdmin = FirebaseAuth.getInstance().currentUser?.email == "admin@gmail.com"
        
        // Show mess management button only for admin
        binding.messManagementButton.visibility = if (isAdmin) View.VISIBLE else View.GONE
        
        binding.messManagementButton.setOnClickListener {
            showMessManagementDialog()
        }

        setupRecyclerView()
        setupFab()
        checkDatabaseConnection()
    }

    private fun setupRecyclerView() {
        binding.announcementRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AnnouncementAdapter(AnnouncementData.announcements)
        }
    }

    private fun setupFab() {
        binding.addAnnouncementFab.setOnClickListener {
            showAddAnnouncementDialog()
        }
    }

    private fun showAddAnnouncementDialog() {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_announcement, null)

        val titleInput = dialogView.findViewById<TextInputLayout>(R.id.titleInput).editText
        val contentInput = dialogView.findViewById<TextInputLayout>(R.id.contentInput).editText

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Add Announcement")
            .setView(dialogView)
            .setPositiveButton("Post") { _, _ ->
                val title = titleInput?.text?.toString() ?: return@setPositiveButton
                val content = contentInput?.text?.toString() ?: return@setPositiveButton

                if (title.isNotBlank() && content.isNotBlank()) {
                    val timestamp = System.currentTimeMillis()
                    // First add locally
                    val announcement = Announcement(title, content, timestamp)
                    AnnouncementData.announcements.add(0, announcement)
                    binding.announcementRecyclerView.adapter?.notifyItemInserted(0)

                    // Then push to Firebase
                    val firebaseAnnouncement = FirebaseAnnouncement(title, content, timestamp)
                    announcementsRef.push().setValue(firebaseAnnouncement)
                        .addOnSuccessListener {
                            Log.d("AnnouncementFragment", "Announcement added to Firebase successfully")
                        }
                        .addOnFailureListener { e ->
                            Log.e("AnnouncementFragment", "Error adding announcement to Firebase", e)
                        }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
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

    private fun showMessManagementDialog() {
        val dialogBinding = DialogMessManagementBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Mess Management")
            .setView(dialogBinding.root)
            .create()

        val years = arrayOf("P1", "P2", "E1", "E2", "E3", "E4")
        val halls = arrayOf("DH1", "DH2", "DH3", "DH4", "DH5", "DH6")
        val mappings = mutableMapOf<String, String>()

        dialogBinding.apply {
            addMappingButton.setOnClickListener {
                val mappingView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_mess_mapping, mappingContainer, false)

                // Setup year spinner
                val yearSpinner = mappingView.findViewById<Spinner>(R.id.yearSpinner)
                yearSpinner.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    years.filter { year -> !mappings.containsKey(year) }
                )

                // Setup hall spinner
                val hallSpinner = mappingView.findViewById<Spinner>(R.id.hallSpinner)
                hallSpinner.adapter = ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    halls.filter { hall -> !mappings.containsValue(hall) }
                )

                // Setup remove button
                mappingView.findViewById<ImageButton>(R.id.removeButton).setOnClickListener {
                    mappingContainer.removeView(mappingView)
                    val year = yearSpinner.selectedItem.toString()
                    mappings.remove(year)
                }

                // Update mappings when selection changes
                yearSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val year = yearSpinner.selectedItem.toString()
                        val hall = hallSpinner.selectedItem.toString()
                        mappings[year] = hall
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                hallSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        val year = yearSpinner.selectedItem.toString()
                        val hall = hallSpinner.selectedItem.toString()
                        mappings[year] = hall
                    }
                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }

                mappingContainer.addView(mappingView)
            }

            submitButton.setOnClickListener {
                val messManagement = MessManagement(
                    messRepName = repNameInput.text.toString(),
                    messRepContact = repContactInput.text.toString(),
                    messRepEmail = repEmailInput.text.toString(),
                    messAllocation = mappings
                )

                saveMessManagement(messManagement, dialog)
            }
        }

        dialog.show()
    }

    private fun saveMessManagement(messManagement: MessManagement, dialog: AlertDialog) {
        val messRef = database.getReference("mess_management")
        
        messRef.push().setValue(messManagement)
            .addOnSuccessListener {
                // Create announcement
                val announcement = mapOf(
                    "title" to "New Mess Management Update",
                    "content" to createAnnouncementContent(messManagement),
                    "timestamp" to System.currentTimeMillis()
                )
                
                announcementsRef.push().setValue(announcement)
                    .addOnSuccessListener {
                        dialog.dismiss()
                        Snackbar.make(binding.root, "Mess management updated and announced", Snackbar.LENGTH_LONG).show()
                    }
                    .addOnFailureListener { e ->
                        Snackbar.make(binding.root, "Error: ${e.message}", Snackbar.LENGTH_LONG).show()
                    }
            }
    }

    private fun createAnnouncementContent(messManagement: MessManagement): String {
        val allocationsText = messManagement.messAllocation.entries.joinToString("\n") { (year, hall) ->
            "$year: $hall"
        }

        return """
            New mess representative has been appointed:
            Name: ${messManagement.messRepName}
            Contact: ${messManagement.messRepContact}
            Email: ${messManagement.messRepEmail}
            
            Mess Allocations:
            $allocationsText
            
            For any queries, please contact the mess representative.
        """.trimIndent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 