package com.example.myapplication.ui.grievances

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.myapplication.R
import com.example.myapplication.databinding.FragmentComplaintDetailsBinding
import com.example.myapplication.model.GrievanceStatus
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class ComplaintDetailsFragment : Fragment() {
    private var _binding: FragmentComplaintDetailsBinding? = null
    private val binding get() = _binding!!
    private val args: ComplaintDetailsFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentComplaintDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        val complaint = args.complaint
        binding.apply {
            complaintId.text = "Complaint #${complaint.id}"
            complaintTitle.text = complaint.title
            complaintDescription.text = complaint.description
            statusChip.text = complaint.status.name
            dateSubmitted.text = "Submitted: ${complaint.dateSubmitted}"
            if (complaint.lastUpdated.isNotEmpty()) {
                dateUpdated.text = "Last Updated: ${complaint.lastUpdated}"
                dateUpdated.visibility = View.VISIBLE
            }

            reportButton.setOnClickListener {
                showReportDialog()
            }

            updateStatusButton.setOnClickListener {
                showStatusUpdateDialog()
            }
        }
    }

    private fun showReportDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Generate Report")
            .setMessage("Do you want to generate a report for Complaint #${args.complaint.id}?")
            .setPositiveButton("Generate") { _, _ ->
                // TODO: Implement report generation
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showStatusUpdateDialog() {
        val statuses = GrievanceStatus.values().map { it.name }.toTypedArray()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Update Status")
            .setItems(statuses) { _, which ->
                // TODO: Implement status update
                val newStatus = GrievanceStatus.values()[which]
                binding.statusChip.text = newStatus.name
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 