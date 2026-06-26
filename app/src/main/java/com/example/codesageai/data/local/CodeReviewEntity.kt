package com.example.codesageai.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "code_reviews")
data class CodeReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val code: String,
    val language: String,
    val timestamp: Long,
    val rawAiReview: String, // JSON payload for styling/bugs/security feedback
    val timeComplexity: String,
    val spaceComplexity: String,
    val complexityDetails: String,
    val improvedCode: String
)
