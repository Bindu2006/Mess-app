package com.example.mess.ui.complaints

data class Complaint(
    val id: String = "",
    val category: String = "",
    val description: String = "",
    val status: ComplaintStatus = ComplaintStatus.PENDING,
    val date: String = "",
    val validationStatus: String = "none",
    val imageUrl: String? = null
)