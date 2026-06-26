package com.example.codesageai

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Splash : NavKey
@Serializable data object Home : NavKey
@Serializable data object UploadPaste : NavKey

@Serializable 
data class CodeEditor(
    val initialCode: String = "", 
    val initialLanguage: String = "Java"
) : NavKey

@Serializable 
data class ReviewResults(val reviewId: Long) : NavKey

@Serializable 
data class ComplexityAnalysis(val reviewId: Long) : NavKey

@Serializable 
data class OutputConsole(val code: String, val language: String) : NavKey

@Serializable 
data class ChatWithCode(val reviewId: Long) : NavKey

@Serializable data object ReviewHistory : NavKey
@Serializable data object Settings : NavKey
