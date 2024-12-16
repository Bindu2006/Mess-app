package com.example.myapplication.ui.feedback

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.adapter.FeedbackAdapter
import com.example.myapplication.databinding.FragmentFeedbackBinding
import com.example.myapplication.model.Feedback
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class FeedbackFragment : Fragment() {
    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!
    private val database = FirebaseDatabase.getInstance("https://mess-5306b-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val feedbackRef = database.getReference("feedback")
    private val feedbackList = mutableListOf<Feedback>()
    private lateinit var adapter: FeedbackAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadFeedback()
    }

    private fun setupRecyclerView() {
        adapter = FeedbackAdapter(feedbackList)
        binding.feedbackRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@FeedbackFragment.adapter
        }
    }

    private fun loadFeedback() {
        feedbackRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                feedbackList.clear()
                for (childSnapshot in snapshot.children) {
                    try {
                        val feedback = childSnapshot.getValue(Feedback::class.java)
                        feedback?.let { feedbackList.add(it) }
                    } catch (e: Exception) {
                        Log.e("FeedbackFragment", "Error parsing feedback", e)
                    }
                }
                feedbackList.sortByDescending { it.date }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FeedbackFragment", "Error loading feedback", error.toException())
                Snackbar.make(binding.root, "Error loading feedback", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 