package com.example.mess.ui.feedback

data class FeedbackData(
    val foodQualityRating: Float,
    val serviceRating: Float,
    val cleanlinessRating: Float,
    val cookingRating: Float,
    val washAreaRating: Float,
    val suggestions: String,
    val date: String
) 