package com.example.mess.ui.complaints

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mess.R
import com.example.mess.databinding.ItemComplaintStatusBinding

class ComplaintStatusAdapter(
    private var complaints: List<Complaint>,
    private val onActionClick: (Complaint, ComplaintAction) -> Unit
) : RecyclerView.Adapter<ComplaintStatusAdapter.ComplaintViewHolder>() {

    fun updateComplaints(newComplaints: List<Complaint>) {
        complaints = newComplaints
        notifyDataSetChanged()
    }

    inner class ComplaintViewHolder(private val binding: ItemComplaintStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(complaint: Complaint) {
            binding.apply {
                complaintCategory.text = complaint.category
                complaintDescription.text = complaint.description
                complaintDate.text = complaint.date
                
                // Set status icon and background
                statusIcon.setImageResource(when (complaint.status) {
                    ComplaintStatus.ACKNOWLEDGED -> R.drawable.ic_status_acknowledged
                    ComplaintStatus.IN_PROGRESS -> R.drawable.ic_status_in_progress
                    ComplaintStatus.COMPLETED -> R.drawable.ic_status_completed
                    ComplaintStatus.PENDING -> R.drawable.ic_status_pending
                    ComplaintStatus.RESOLVED -> R.drawable.ic_status_resolved
                })
                
                complaintStatus.apply {
                    text = when (complaint.status) {
                        ComplaintStatus.ACKNOWLEDGED -> "Acknowledged"
                        ComplaintStatus.IN_PROGRESS -> "In Progress"
                        ComplaintStatus.COMPLETED -> "Completed - Pending Confirmation"
                        ComplaintStatus.PENDING -> "Pending"
                        ComplaintStatus.RESOLVED -> "Resolved"
                    }
                    background = when (complaint.status) {
                        ComplaintStatus.ACKNOWLEDGED -> context.getDrawable(R.drawable.status_background_acknowledged)
                        ComplaintStatus.IN_PROGRESS -> context.getDrawable(R.drawable.status_background_in_progress)
                        ComplaintStatus.COMPLETED -> context.getDrawable(R.drawable.status_background_completed)
                        ComplaintStatus.PENDING -> context.getDrawable(R.drawable.status_background_pending)
                        ComplaintStatus.RESOLVED -> context.getDrawable(R.drawable.status_background_completed)
                    }
                }

                // Show action buttons only for COMPLETED status
                actionButtons.visibility = when (complaint.status) {
                    ComplaintStatus.COMPLETED -> {
                        confirmResolutionButton.visibility = View.VISIBLE
                        reportUnresolvedButton.visibility = View.VISIBLE
                        View.VISIBLE
                    }
                    ComplaintStatus.ACKNOWLEDGED,
                    ComplaintStatus.IN_PROGRESS,
                    ComplaintStatus.PENDING,
                    ComplaintStatus.RESOLVED -> View.GONE
                }

                // Update button texts for COMPLETED status
                if (complaint.status == ComplaintStatus.COMPLETED) {
                    confirmResolutionButton.text = "Confirm Resolution"
                    reportUnresolvedButton.text = "Still Unresolved"
                }

                confirmResolutionButton.setOnClickListener {
                    onActionClick(complaint, ComplaintAction.CONFIRM_RESOLUTION)
                }

                reportUnresolvedButton.setOnClickListener {
                    onActionClick(complaint, ComplaintAction.REPORT_UNRESOLVED)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val binding = ItemComplaintStatusBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ComplaintViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComplaintViewHolder, position: Int) {
        holder.bind(complaints[position])
    }

    override fun getItemCount() = complaints.size
} 