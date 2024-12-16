package com.example.mess.ui.complaints

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mess.data.FirebaseHelper
import com.example.mess.databinding.FragmentComplaintsBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ComplaintsFragment : Fragment() {

    private var _binding: FragmentComplaintsBinding? = null
    private val binding get() = _binding!!
    private var selectedImageUri: Uri? = null
    private var selectedVideoUri: Uri? = null
    private val PICK_IMAGE_REQUEST = 1
    private val PICK_VIDEO_REQUEST = 2

    private val mainCategories = listOf(
        "Timeliness of Service",
        "Cleanliness & Hygiene",
        "Food Quality & Quantity",
        "Staff Behavior & Hygiene",
        "Menu Adherence & Cooking Standards",
        "Other Issues"
    )

    private val subCategories = mapOf(
        "Timeliness of Service" to listOf(
            "Delay in service",
            "Queue management",
            "Waiting time at counters"
        ),
        "Cleanliness & Hygiene" to listOf(
            "Dining hall cleanliness",
            "Cleanliness of washbasins and wash areas",
            "Cleanliness of plates and cutlery",
            "Cleanliness of cooking utensils",
            "Staff hygiene (uniforms, gloves, masks)"
        ),
        "Food Quality & Quantity" to listOf(
            "Quality of rice, snacks, tea, coffee, or breakfast",
            "Quantity of food served not as per the menu",
            "Expiry of items or poor-quality raw materials",
            "Taste and freshness of food",
            "Shortage of food at counters"
        ),
        "Staff Behavior & Hygiene" to listOf(
            "Courtesy of mess staff towards students",
            "Staff hygiene (uniforms, gloves, masks)",
            "Number of staff at the counters (insufficient staff)"
        ),
        "Menu Adherence & Cooking Standards" to listOf(
            "Cooking and serving not as per the menu",
            "Quantity of food not as per the menu",
            "Menu changes without prior notice",
            "Cooking methods not followed properly"
        ),
        "Other Issues" to listOf(
            "Lack of regular meetings with mess supervisors",
            "Lack of updates regarding changes",
            "Other"
        )
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComplaintsBinding.inflate(inflater, container, false)
        setupSpinners()
        setupButtons()
        return binding.root
    }

    private fun setupSpinners() {
        try {
            // Setup main category spinner
            val mainAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                mainCategories
            ).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }
            binding.mainCategorySpinner.adapter = mainAdapter

            // Setup subcategory spinner and handle main category selection changes
            binding.mainCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    val selectedCategory = mainCategories[position]
                    val subCategoryList = subCategories[selectedCategory] ?: emptyList()
                    
                    val subAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_spinner_item,
                        subCategoryList
                    ).apply {
                        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    }
                    binding.subCategorySpinner.adapter = subAdapter
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {
                    // Do nothing
                }
            }
        } catch (e: Exception) {
            Log.e("ComplaintsFragment", "Error setting up spinners: ${e.message}", e)
            Toast.makeText(context, "Error setting up complaint categories", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupButtons() {
        binding.attachPhotoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        binding.attachVideoButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_VIDEO_REQUEST)
        }

        binding.submitButton.setOnClickListener {
            submitComplaint()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                PICK_IMAGE_REQUEST -> {
                    selectedImageUri = data.data
                    binding.attachedPhoto.apply {
                        setImageURI(selectedImageUri)
                        visibility = View.VISIBLE
                    }
                    binding.attachedVideo.visibility = View.GONE
                    selectedVideoUri = null
                }
                PICK_VIDEO_REQUEST -> {
                    selectedVideoUri = data.data
                    binding.attachedVideo.apply {
                        setVideoURI(selectedVideoUri)
                        visibility = View.VISIBLE
                        start()
                    }
                    binding.attachedPhoto.visibility = View.GONE
                    selectedImageUri = null
                }
            }
        }
    }

    private fun submitComplaint() {
        val mainCategory = binding.mainCategorySpinner.selectedItem.toString()
        val subCategory = binding.subCategorySpinner.selectedItem.toString()
        val description = binding.complaintDescription.text.toString()

        if (description.isEmpty()) {
            Toast.makeText(requireContext(), "Please describe your complaint", Toast.LENGTH_SHORT).show()
            return
        }

        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val complaint = Complaint(
            category = mainCategory,
            description = "$subCategory: $description",
            status = ComplaintStatus.PENDING,
            date = currentDate,
            validationStatus = "none"
        )

        FirebaseHelper.submitComplaint(
            complaint = complaint,
            onSuccess = {
                Toast.makeText(requireContext(), "Complaint submitted successfully", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            },
            onError = { error ->
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun clearForm() {
        binding.mainCategorySpinner.setSelection(0)
        binding.complaintDescription.text?.clear()
        binding.attachedPhoto.apply {
            setImageURI(null)
            visibility = View.GONE
        }
        binding.attachedVideo.apply {
            setVideoURI(null)
            visibility = View.GONE
            stopPlayback()
        }
        selectedImageUri = null
        selectedVideoUri = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 