package com.example.mess.ui.announcements

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mess.databinding.ItemAnnouncementBinding

class AnnouncementsAdapter(
    private val announcements: List<Announcement>
) : RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementViewHolder>() {

    inner class AnnouncementViewHolder(private val binding: ItemAnnouncementBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(announcement: Announcement) {
            binding.apply {
                announcementTitle.text = announcement.title
                announcementDescription.text = announcement.description
                announcementDate.text = announcement.date
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val binding = ItemAnnouncementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AnnouncementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        holder.bind(announcements[position])
    }

    override fun getItemCount() = announcements.size
} 