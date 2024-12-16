package com.example.mess.ui.menu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.mess.data.FirebaseHelper
import com.example.mess.databinding.FragmentWeeklyMenuBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class WeeklyMenuFragment : Fragment() {
    private var _binding: FragmentWeeklyMenuBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWeeklyMenuBinding.inflate(inflater, container, false)
        displayWeeklyMenu()
        return binding.root
    }

    private fun displayWeeklyMenu() {
        lifecycleScope.launch {
            try {
                val weeklyMenu = StringBuilder()
                val snapshot = getMenuSnapshot()
                
                val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
                val meals = listOf("breakfast", "lunch", "snacks", "dinner")
                
                days.forEach { day ->
                    // Add day header with better formatting
                    weeklyMenu.append("\n\n${day.uppercase()}\n")
                    weeklyMenu.append("━━━━━━━━━━━━━━━━━━━━\n")
                    
                    meals.forEach { meal ->
                        // Add meal header with better formatting
                        weeklyMenu.append("\n${meal.capitalize()} 🍽️\n")
                        weeklyMenu.append("──────────────\n")
                        
                        val menuItems = snapshot.child(day).child(meal).children.mapNotNull { 
                            it.getValue(String::class.java) 
                        }
                        menuItems.forEach { item ->
                            weeklyMenu.append("  • $item\n")  // Added indentation
                        }
                    }
                    weeklyMenu.append("\n")  // Extra spacing between days
                }
                
                binding.weeklyMenuText.apply {
                    text = weeklyMenu.toString()
                    setLineSpacing(0f, 1.2f)  // Increase line spacing
                }
                
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to load weekly menu: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getMenuSnapshot(): DataSnapshot = suspendCoroutine { continuation ->
        FirebaseHelper.menuRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                continuation.resume(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                continuation.resumeWithException(error.toException())
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 