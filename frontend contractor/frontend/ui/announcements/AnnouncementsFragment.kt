package com.example.mess.ui.announcements

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mess.MainActivity
import com.example.mess.R
import com.example.mess.data.FirebaseHelper
import com.example.mess.databinding.FragmentAnnouncementsBinding
import kotlinx.coroutines.launch

class AnnouncementsFragment : Fragment() {

    private var _binding: FragmentAnnouncementsBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: AnnouncementsAdapter

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (!isGranted) {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        createNotificationChannel()
        checkNotificationPermission()
        return binding.root
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AnnouncementsAdapter(emptyList())
        binding.announcementsRecyclerView.adapter = adapter
        loadAnnouncements()
    }

    private fun loadAnnouncements() {
        lifecycleScope.launch {
            try {
                val announcements = FirebaseHelper.getAnnouncements()
                adapter = AnnouncementsAdapter(announcements)
                binding.announcementsRecyclerView.adapter = adapter
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load announcements: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Mess Announcements"
            val descriptionText = "Notifications for mess announcements"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("MESS_ANNOUNCEMENTS", name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class Announcement(
    val title: String,
    val description: String,
    val date: String
) 