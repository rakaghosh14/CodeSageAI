package com.example.codesageai.data.remote

// Gemini API models
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

data class Content(
    val role: String? = null,
    val parts: List<Part>
)

data class Part(
    val text: String
)

data class GenerationConfig(
    val responseMimeType: String? = null,
    val temperature: Double? = null
)

data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)

// Judge0 API models
data class Judge0SubmissionRequest(
    val source_code: String,
    val language_id: Int,
    val stdin: String = ""
)

data class Judge0Response(
    val token: String?,
    val stdout: String?,
    val stderr: String?,
    val compile_output: String?,
    val time: String?,
    val memory: Int?,
    val status: Judge0Status?
)

data class Judge0Status(
    val id: Int,
    val description: String
)

// Parsed AI Review Structured Model (for internal parsing of rawAiReview JSON)
data class AiReviewPayload(
    val bugs: List<BugReport>,
    val optimizations: List<OptimizationReport>,
    val readabilityStyle: List<StyleReport>,
    val explanation: String
)

data class BugReport(
    val line: Int,
    val description: String,
    val severity: String, // "High" | "Medium" | "Low"
    val originalSnippet: String,
    val suggestedSnippet: String
)

data class OptimizationReport(
    val line: Int,
    val description: String,
    val originalSnippet: String,
    val suggestedSnippet: String
)

data class StyleReport(
    val line: Int,
    val description: String,
    val suggestedChange: String
)
