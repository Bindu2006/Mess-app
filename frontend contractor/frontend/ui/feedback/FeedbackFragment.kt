package com.example.mess.ui.feedback

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mess.data.FirebaseHelper
import com.example.mess.databinding.FragmentFeedbackBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FeedbackFragment : Fragment() {

    private var _binding: FragmentFeedbackBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFeedbackBinding.inflate(inflater, container, false)
        setupSubmitButton()
        return binding.root
    }

    private fun setupSubmitButton() {
        binding.submitFeedback.setOnClickListener {
            submitFeedback()
        }
    }

    private fun submitFeedback() {
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        
        val feedback = FeedbackData(
            foodQualityRating = binding.ratingFoodStatus.rating,
            serviceRating = binding.ratingTimeliness.rating,
            cleanlinessRating = binding.ratingNeatness.rating,
            cookingRating = binding.ratingCooking.rating,
            washAreaRating = binding.ratingWashArea.rating,
            suggestions = binding.suggestions.text.toString(),
            date = currentDate
        )

        FirebaseHelper.submitFeedback(
            feedback = feedback,
            onSuccess = {
                Toast.makeText(context, "Feedback submitted successfully", Toast.LENGTH_SHORT).show()
                clearForm()
            },
            onError = { error ->
                Toast.makeText(context, "Error: $error", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun convertRatingToScore(rating: Float): Double {
        return when (rating) {
            5f -> 1.0    // Excellent - 100%
            4f -> 0.8    // Good - 80%
            3f -> 0.6    // Satisfactory - 60%
            2f -> 0.4    // Below Satisfactory - 40%
            1f -> 0.2    // Poor - 20%
            else -> 0.0
        }
    }

    private fun clearForm() {
        binding.apply {
            ratingTimeliness.rating = 0f
            ratingNeatness.rating = 0f
            ratingFoodStatus.rating = 0f
            ratingTaste.rating = 0f
            ratingSnacks.rating = 0f
            ratingQuantity.rating = 0f
            ratingCourtesy.rating = 0f
            ratingUniforms.rating = 0f
            ratingCooking.rating = 0f
            ratingWashArea.rating = 0f
            suggestions.text?.clear()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 