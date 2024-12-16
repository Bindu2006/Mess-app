package com.example.mess.ui.contact

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mess.databinding.ItemContactBinding
import com.example.mess.model.ContactInfo

class ContactAdapter(
    private val contacts: List<ContactInfo>
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    inner class ContactViewHolder(private val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: ContactInfo) {
            binding.apply {
                contactTitle.text = contact.title
                contactName.text = contact.name
                contactEmail.text = contact.email
                contactPhone.text = contact.phone

                emailButton.setOnClickListener {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:${contact.email}")
                    }
                    root.context.startActivity(Intent.createChooser(intent, "Send email"))
                }

                callButton.setOnClickListener {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${contact.phone}")
                    }
                    root.context.startActivity(intent)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(contacts[position])
    }

    override fun getItemCount() = contacts.size
} 