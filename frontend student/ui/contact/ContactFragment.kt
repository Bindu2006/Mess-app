package com.example.mess.ui.contact

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mess.data.FirebaseHelper
import com.example.mess.databinding.FragmentContactBinding
import com.example.mess.model.ContactInfo
import kotlinx.coroutines.launch

class ContactFragment : Fragment() {
    private var _binding: FragmentContactBinding? = null
    private val binding get() = _binding!!
    private lateinit var contactAdapter: ContactAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContactBinding.inflate(inflater, container, false)
        setupRecyclerView()
        loadContacts()
        return binding.root
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter(emptyList())
        binding.contactsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = contactAdapter
        }
    }

    private fun loadContacts() {
        lifecycleScope.launch {
            try {
                val contacts = FirebaseHelper.getContacts()
                contactAdapter = ContactAdapter(contacts)
                binding.contactsRecyclerView.adapter = contactAdapter
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load contacts: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 