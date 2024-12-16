package com.example.mess.ui.representative.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.graphics.BitmapFactory
import androidx.recyclerview.widget.RecyclerView
import com.example.mess.databinding.ItemComplaintManagementBinding
import com.example.mess.ui.complaints.Complaint
import com.example.mess.ui.complaints.ComplaintStatus
import com.google.firebase.storage.FirebaseStorage
import java.io.File

class ComplaintManagementAdapter(
    private var complaints: List<Complaint>,
    private val onValidateClick: (Complaint) -> Unit,
    private val onDenyClick: (Complaint) -> Unit
) : RecyclerView.Adapter<ComplaintManagementAdapter.ComplaintViewHolder>() {

    inner class ComplaintViewHolder(private val binding: ItemComplaintManagementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(complaint: Complaint) {
            binding.apply {
                complaintCategory.text = complaint.category
                complaintDescription.text = complaint.description
                complaintStatus.text = complaint.status.toString()

                // Handle image loading from Firebase Storage
                val storageRef = FirebaseStorage.getInstance().reference
                    .child("complaints")
                    .child("images")
                    .child("${System.currentTimeMillis()}.jpg")

                complaint.imageUrl?.let { url ->
                    if (url.isNotEmpty()) {
                        complaintImage.visibility = View.VISIBLE
                        // Load image using Firebase Storage URL
                        FirebaseStorage.getInstance().getReferenceFromUrl(url)
                            .getBytes(Long.MAX_VALUE)
                            .addOnSuccessListener { bytes ->
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                complaintImage.setImageBitmap(bitmap)
                            }
                            .addOnFailureListener {
                                complaintImage.visibility = View.GONE
                            }
                    } else {
                        complaintImage.visibility = View.GONE
                    }
                } ?: run {
                    complaintImage.visibility = View.GONE
                }

                validateButton.setOnClickListener { onValidateClick(complaint) }
                denyButton.setOnClickListener { onDenyClick(complaint) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComplaintViewHolder {
        val binding = ItemComplaintManagementBinding.inflate(
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

    fun updateComplaints(newComplaints: List<Complaint>) {
        complaints = newComplaints
        notifyDataSetChanged()
    }
} 