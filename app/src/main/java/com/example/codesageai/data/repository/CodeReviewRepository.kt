package com.example.codesageai.data.repository

import com.example.codesageai.data.local.ChatMessageDao
import com.example.codesageai.data.local.ChatMessageEntity
import com.example.codesageai.data.local.CodeReviewDao
import com.example.codesageai.data.local.CodeReviewEntity
import com.example.codesageai.data.remote.Content
import com.example.codesageai.data.remote.GeminiApiService
import com.example.codesageai.data.remote.GeminiRequest
import com.example.codesageai.data.remote.GenerationConfig
import com.example.codesageai.data.remote.Judge0ApiService
import com.example.codesageai.data.remote.Judge0Response
import com.example.codesageai.data.remote.Judge0Status
import com.example.codesageai.data.remote.Judge0SubmissionRequest
import com.example.codesageai.data.remote.Part
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class CodeReviewRepository(
    private val reviewDao: CodeReviewDao,
    private val chatMessageDao: ChatMessageDao,
    private val settingsRepository: SettingsRepository
) {
    private val gson = Gson()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    private val geminiApi = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(GeminiApiService::class.java)

    private val judge0Api = Retrofit.Builder()
        .baseUrl("https://ce.judge0.com/") // Standard Judge0 CE endpoint
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(Judge0ApiService::class.java)

    fun getAllReviews(): Flow<List<CodeReviewEntity>> = reviewDao.getAllReviews()

    fun getReviewById(id: Long): Flow<CodeReviewEntity?> = reviewDao.getReviewById(id)

    suspend fun deleteReviewById(id: Long) = reviewDao.deleteReviewById(id)

    fun searchReviews(query: String): Flow<List<CodeReviewEntity>> = reviewDao.searchReviews(query)

    fun getChatMessages(reviewId: Long): Flow<List<ChatMessageEntity>> = chatMessageDao.getMessagesForReview(reviewId)

    suspend fun sendChatMessage(reviewId: Long, message: String): ChatMessageEntity {
        // Save user message
        val userMsg = ChatMessageEntity(
            reviewId = reviewId,
            role = "user",
            message = message,
            timestamp = System.currentTimeMillis()
        )
        chatMessageDao.insertMessage(userMsg)

        val useMock = settingsRepository.useMockMode.value
        val reply = if (useMock) {
            simulateChatReply(message)
        } else {
            callGeminiChat(reviewId, message)
        }

        val assistantMsg = ChatMessageEntity(
            reviewId = reviewId,
            role = "model",
            message = reply,
            timestamp = System.currentTimeMillis()
        )
        chatMessageDao.insertMessage(assistantMsg)
        return assistantMsg
    }

    suspend fun performCodeReview(title: String, code: String, language: String): Long {
        val useMock = settingsRepository.useMockMode.value
        val (rawReview, timeComp, spaceComp, compDetails, improved) = if (useMock) {
            simulateReview(code, language)
        } else {
            callGeminiForReview(code, language)
        }

        val review = CodeReviewEntity(
            title = title,
            code = code,
            language = language,
            timestamp = System.currentTimeMillis(),
            rawAiReview = rawReview,
            timeComplexity = timeComp,
            spaceComplexity = spaceComp,
            complexityDetails = compDetails,
            improvedCode = improved
        )

        val id = reviewDao.insertReview(review)

        // Seed initial message as AI explaining the code
        val explanation = parseExplanation(rawReview)
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                reviewId = id,
                role = "model",
                message = "Hi! I have reviewed your code. Here is a brief explanation:\n\n$explanation\n\nHow can I help you improve or understand this code further?",
                timestamp = System.currentTimeMillis()
            )
        )

        return id
    }

    suspend fun executeCode(code: String, language: String): Judge0Response {
        val useMock = settingsRepository.useMockMode.value
        if (useMock) {
            return simulateCodeExecution(code, language)
        }

        val langId = getJudge0LanguageId(language)
        val request = Judge0SubmissionRequest(source_code = code, language_id = langId)
        val apiKey = settingsRepository.judge0ApiKey.value

        return try {
            if (apiKey.isNotEmpty()) {
                // If the user has a rapidapi key, use it (assumed rapidapi server is configured or standard headers are added)
                judge0Api.executeCode(
                    request = request,
                    rapidApiKey = apiKey,
                    rapidApiHost = "judge0-extra-clean.p.rapidapi.com"
                )
            } else {
                judge0Api.executeCode(request = request)
            }
        } catch (e: Exception) {
            Judge0Response(
                token = null,
                stdout = null,
                stderr = e.localizedMessage,
                compile_output = "Connection to execution service failed.",
                time = "0.0",
                memory = 0,
                status = Judge0Status(13, "Internal API Error")
            )
        }
    }

    // --- Simulation Mock Methods ---

    private fun simulateReview(code: String, language: String): ReviewDetails {
        val isSort = code.contains("sort", ignoreCase = true)
        val isSearch = code.contains("search", ignoreCase = true)

        val bugs = mutableListOf<Map<String, Any>>()
        val opts = mutableListOf<Map<String, Any>>()
        val style = mutableListOf<Map<String, Any>>()
        val timeComp: String
        val spaceComp: String
        val compDetails: String
        val explanation: String
        val improvedCode: String

        if (isSort) {
            bugs.add(mapOf(
                "line" to 5,
                "description" to "Potential IndexOutOfBoundsException. The inner loop checks elements out of the array boundary.",
                "severity" to "High",
                "originalSnippet" to "for (int j = 0; j < arr.length; j++) { if (arr[j] > arr[j+1]) ... }",
                "suggestedSnippet" to "for (int j = 0; j < arr.length - i - 1; j++) { if (arr[j] > arr[j+1]) ... }"
            ))
            opts.add(mapOf(
                "line" to 3,
                "description" to "Use a swapped boolean flag to optimize sorting and terminate early if the array becomes sorted.",
                "originalSnippet" to "for (int i = 0; i < n; i++) { ... }",
                "suggestedSnippet" to "boolean swapped;\nfor (int i = 0; i < n - 1; i++) {\n  swapped = false;\n  ...\n  if (!swapped) break;\n}"
            ))
            style.add(mapOf(
                "line" to 1,
                "description" to "Consider using camelCase for naming parameters instead of snake_case in Java/Kotlin.",
                "suggestedChange" to "Rename input parameter to inputList or arrayData."
            ))
            timeComp = "O(N^2)"
            spaceComp = "O(1)"
            compDetails = "Nested loops result in quadratic time complexity. The space complexity is constant because sorting occurs in-place."
            explanation = "This code implements Bubble Sort. It repeatedly steps through the input list, compares adjacent elements, and swaps them if they are in the wrong order. This pass is repeated until the list is sorted."
            improvedCode = when (language.lowercase()) {
                "python" -> """def bubble_sort(arr):
    n = len(arr)
    for i in range(n):
        swapped = False
        for j in range(0, n - i - 1):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
                swapped = True
        if not swapped:
            break
    return arr"""
                else -> """fun bubbleSort(arr: IntArray) {
    val n = arr.size
    var swapped: Boolean
    for (i in 0 until n - 1) {
        swapped = false
        for (j in 0 until n - i - 1) {
            if (arr[j] > arr[j + 1]) {
                val temp = arr[j]
                arr[j] = arr[j + 1]
                arr[j + 1] = temp
                swapped = true
            }
        }
        if (!swapped) break
    }
}"""
            }
        } else if (isSearch) {
            bugs.add(mapOf(
                "line" to 6,
                "description" to "Integer overflow vulnerability when calculating the mid index using (low + high) / 2.",
                "severity" to "Medium",
                "originalSnippet" to "val mid = (low + high) / 2",
                "suggestedSnippet" to "val mid = low + (high - low) / 2"
            ))
            opts.add(mapOf(
                "line" to 2,
                "description" to "Iterative binary search is generally preferred over recursive to avoid Call Stack overhead.",
                "originalSnippet" to "fun search(..., low, high)",
                "suggestedSnippet" to "while (low <= high) { ... }"
            ))
            style.add(mapOf(
                "line" to 8,
                "description" to "Single letter variable names (l, h, m) reduce readability.",
                "suggestedChange" to "Rename to low, high, and mid respectively."
            ))
            timeComp = "O(log N)"
            spaceComp = "O(1)"
            compDetails = "The search space is cut in half at each step, resulting in logarithmic time complexity. Auxiliary space is constant for the iterative approach."
            explanation = "This code implements Binary Search. It searches a sorted array by repeatedly dividing the search interval in half. If the value of the search key is less than the item in the middle of the interval, the interval is narrowed to the lower half."
            improvedCode = """fun binarySearch(arr: IntArray, target: Int): Int {
    var low = 0
    var high = arr.size - 1
    while (low <= high) {
        val mid = low + (high - low) / 2
        if (arr[mid] == target) return mid
        if (arr[mid] < target) {
            low = mid + 1
        } else {
            high = mid - 1
        }
    }
    return -1
}"""
        } else {
            // General fallback review
            bugs.add(mapOf(
                "line" to 2,
                "description" to "Ensure input parameters are checked for null or empty values to avoid runtime crashes.",
                "severity" to "Medium",
                "originalSnippet" to "// No input validation",
                "suggestedSnippet" to "if (input == null) throw IllegalArgumentException(\"Input cannot be null\")"
            ))
            opts.add(mapOf(
                "line" to 4,
                "description" to "Store the size/length of collections in a local variable if queried frequently in a loop.",
                "originalSnippet" to "for (i in 0 until list.size)",
                "suggestedSnippet" to "val size = list.size\nfor (i in 0 until size)"
            ))
            style.add(mapOf(
                "line" to 10,
                "description" to "Missing function documentation.",
                "suggestedChange" to "Add standard KDoc/Javadoc comments explaining parameters and return type."
            ))
            timeComp = "O(N)"
            spaceComp = "O(N)"
            compDetails = "Linear execution time relative to input collection length. Allocates a mapping structure proportional to inputs."
            explanation = "The code processes an input stream, maps keys to values, and returns the formatted sequence. It performs a sequential traversal of elements."
            improvedCode = code + "\n\n// Refactored version: added safety validation\n// Feel free to chat with CodeSage AI to specialize this further."
        }

        val jsonReview = gson.toJson(mapOf(
            "bugs" to bugs,
            "optimizations" to opts,
            "readabilityStyle" to style,
            "explanation" to explanation
        ))

        return ReviewDetails(jsonReview, timeComp, spaceComp, compDetails, improvedCode)
    }

    private fun simulateChatReply(message: String): String {
        val msgLower = message.lowercase()
        return when {
            msgLower.contains("bug") -> {
                "I found a bug in the loop condition which could cause an OutOfBounds index crash. Specifically, on line 5, the boundary check should be `arr.length - i - 1` instead of `arr.length`."
            }
            msgLower.contains("optimize") || msgLower.contains("performance") -> {
                "Yes! You can optimize this sorting algorithm by adding a `swapped` boolean flag. If no elements are swapped during a pass, the array is already sorted, and you can break the loop early, improving the best-case complexity to O(N)."
            }
            msgLower.contains("unit test") || msgLower.contains("test") -> {
                "Sure! Here is a JUnit test case for the sorting algorithm:\n\n```kotlin\n@Test\nfun testBubbleSort() {\n    val arr = intArrayOf(5, 1, 4, 2, 8)\n    bubbleSort(arr)\n    assertArrayEquals(intArrayOf(1, 2, 4, 5, 8), arr)\n}\n```"
            }
            msgLower.contains("kotlin") -> {
                "Here is the Kotlin version of the code:\n\n```kotlin\nfun bubbleSort(arr: IntArray) {\n    val n = arr.size\n    for (i in 0 until n) {\n        for (j in 0 until n - i - 1) {\n            if (arr[j] > arr[j + 1]) {\n                val temp = arr[j]\n                arr[j] = arr[j + 1]\n                arr[j + 1] = temp\n            }\n        }\n    }\n}\n```"
            }
            else -> {
                "That's a great question! This code performs element-wise operations. Let me know if you want me to write tests, explain complexity bottlenecks, or rewrite this function in another language."
            }
        }
    }

    private fun simulateCodeExecution(code: String, language: String): Judge0Response {
        val isError = code.contains("error", ignoreCase = true) || code.contains("throw", ignoreCase = true)
        return if (isError) {
            Judge0Response(
                token = "mock-token",
                stdout = "",
                stderr = "Exception in thread \"main\" java.lang.NullPointerException\n\tat Main.main(Main.java:6)",
                compile_output = "Compilation finished with errors.",
                time = "0.02",
                memory = 1500,
                status = Judge0Status(11, "Runtime Error (NZEC)")
            )
        } else {
            val stdout = when (language.lowercase()) {
                "python" -> "Code Sage Execution Results:\n[1, 2, 4, 5, 8]\nProcess finished with exit code 0"
                else -> "Code Sage Execution Results:\n[1, 2, 4, 5, 8]\nExecution Time: 45ms"
            }
            Judge0Response(
                token = "mock-token",
                stdout = stdout,
                stderr = "",
                compile_output = "",
                time = "0.05",
                memory = 2300,
                status = Judge0Status(3, "Accepted")
            )
        }
    }

    // --- Real Network Remote Calls ---

    private suspend fun callGeminiForReview(code: String, language: String): ReviewDetails {
        val apiKey = settingsRepository.geminiApiKey.value
        if (apiKey.isEmpty()) {
            throw IllegalStateException("Gemini API key is missing. Please set it in Settings.")
        }

        val prompt = """
You are an expert AI code reviewer. Your job is to review the following code snippet.
Language: $language
Code:
```
$code
```

You MUST return your review in a structured JSON format. Make sure to return ONLY the raw JSON string without markdown code fences (`json ...`). The JSON must match the following format exactly:
{
  "bugs": [
    {
      "line": 12,
      "description": "Bug description...",
      "severity": "High",
      "originalSnippet": "...",
      "suggestedSnippet": "..."
    }
  ],
  "optimizations": [
    {
      "line": 15,
      "description": "Optimization tip...",
      "originalSnippet": "...",
      "suggestedSnippet": "..."
    }
  ],
  "readabilityStyle": [
    {
      "line": 20,
      "description": "Readability advice...",
      "suggestedChange": "..."
    }
  ],
  "explanation": "Detailed summary explanation of what this code does in plain English.",
  "timeComplexity": "O(N)",
  "spaceComplexity": "O(1)",
  "complexityDetails": "Detailed explanation of computational complexities and performance bottlenecks.",
  "improvedCode": "Complete rewritten, refactored, optimized version of the input code."
}
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.2
            )
        )

        val response = geminiApi.generateContent(
            model = "gemini-1.5-flash",
            apiKey = apiKey,
            request = request
        )

        val jsonText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw IllegalStateException("Empty response received from Gemini API.")

        // Parse JSON response to extract complexities and clean up
        return try {
            val map = gson.fromJson(jsonText, Map::class.java)
            val timeComp = map["timeComplexity"] as? String ?: "N/A"
            val spaceComp = map["spaceComplexity"] as? String ?: "N/A"
            val compDetails = map["complexityDetails"] as? String ?: "Complexity details not found."
            val improved = map["improvedCode"] as? String ?: code

            // Keep only components for rawAiReview JSON
            val cleanMap = mapOf(
                "bugs" to (map["bugs"] ?: emptyList<Any>()),
                "optimizations" to (map["optimizations"] ?: emptyList<Any>()),
                "readabilityStyle" to (map["readabilityStyle"] ?: emptyList<Any>()),
                "explanation" to (map["explanation"] ?: "")
            )
            val rawReview = gson.toJson(cleanMap)

            ReviewDetails(rawReview, timeComp, spaceComp, compDetails, improved)
        } catch (e: Exception) {
            ReviewDetails(
                rawReview = gson.toJson(mapOf(
                    "bugs" to emptyList<Any>(),
                    "optimizations" to emptyList<Any>(),
                    "readabilityStyle" to emptyList<Any>(),
                    "explanation" to "Could not parse review output: ${e.localizedMessage}"
                )),
                timeComplexity = "N/A",
                spaceComplexity = "N/A",
                complexityDetails = "An error occurred while parsing the AI response.",
                improvedCode = code
            )
        }
    }

    private suspend fun callGeminiChat(reviewId: Long, message: String): String {
        val apiKey = settingsRepository.geminiApiKey.value
        if (apiKey.isEmpty()) return "Gemini API key is missing. Configure it in Settings."

        // Fetch history
        val history = chatMessageDao.getMessagesForReview(reviewId).first()
        val review = reviewDao.getReviewById(reviewId).first() ?: return "Review context not found."

        // Build prompt context
        val chatParts = mutableListOf<Content>()
        
        // System instruction
        chatParts.add(Content(
            role = "user",
            parts = listOf(Part(
                text = "You are CodeSage AI, an assistant helping a developer with the following code:\n\n```\n${review.code}\n```\n\nHere was the initial review analysis:\n${review.rawAiReview}\n\nHelp the developer with questions, explanations, refactorings, conversions, or unit test generations. Answer accurately and keep code snippets clean."
            ))
        ))
        chatParts.add(Content(
            role = "model",
            parts = listOf(Part(text = "Understood. I will help you with questions, refactoring, or tests for this code snippet."))
        ))

        // History
        history.forEach {
            chatParts.add(Content(
                role = if (it.role == "model") "model" else "user",
                parts = listOf(Part(text = it.message))
            ))
        }

        // New Message (already saved in DB, but not included in history list fetched prior to userMsg insertion)
        if (chatParts.lastOrNull()?.parts?.firstOrNull()?.text != message) {
            chatParts.add(Content(
                role = "user",
                parts = listOf(Part(text = message))
            ))
        }

        val request = GeminiRequest(contents = chatParts)
        return try {
            val response = geminiApi.generateContent(
                model = "gemini-1.5-flash",
                apiKey = apiKey,
                request = request
            )
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Empty response received from Gemini."
        } catch (e: Exception) {
            "Error calling Gemini: ${e.localizedMessage}"
        }
    }

    private fun parseExplanation(rawReview: String): String {
        return try {
            val map = gson.fromJson(rawReview, Map::class.java)
            map["explanation"] as? String ?: "No explanation available."
        } catch (e: Exception) {
            "No explanation available."
        }
    }

    private fun getJudge0LanguageId(language: String): Int {
        return when (language.lowercase()) {
            "c" -> 50
            "c++", "cpp" -> 54
            "java" -> 62
            "javascript", "js" -> 63
            "python", "py" -> 71
            "kotlin", "kt" -> 78
            else -> 71 // Default Python
        }
    }

    data class ReviewDetails(
        val rawReview: String,
        val timeComplexity: String,
        val spaceComplexity: String,
        val complexityDetails: String,
        val improvedCode: String
    )
}
