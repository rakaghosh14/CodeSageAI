package com.example.codesageai.ui.editor

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codesageai.data.repository.CodeReviewRepository
import com.example.codesageai.theme.BorderSlate
import com.example.codesageai.theme.DarkBackground
import com.example.codesageai.theme.DarkSurface
import com.example.codesageai.theme.PrimaryNeonBlue
import com.example.codesageai.theme.SuccessGreen
import com.example.codesageai.theme.TextPrimary
import com.example.codesageai.theme.TextSecondary
import com.example.codesageai.ui.utils.SyntaxHighlighter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodeEditorScreen(
    initialCode: String,
    initialLanguage: String,
    reviewRepository: CodeReviewRepository,
    onReviewCompleted: (Long) -> Unit,
    onRunCode: (String, String) -> Unit,
    onBack: () -> Unit
) {
    var code by remember { mutableStateOf(initialCode.ifBlank { getLanguageTemplate(initialLanguage) }) }
    var isAnalyzing by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Syntax Highlighter VisualTransformation
    val syntaxHighlightTransformation = remember {
        VisualTransformation { text ->
            TransformedText(SyntaxHighlighter.highlight(text.text), OffsetMapping.Identity)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Code Editor", color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(12.dp))
                        // Language Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryNeonBlue.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(initialLanguage, color = PrimaryNeonBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { code = "" }) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        if (isAnalyzing) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = PrimaryNeonBlue)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("AI is analyzing your code...", color = Color.White, fontWeight = FontWeight.SemiBold)
                    Text("Detecting bugs, complexity, and styling rules", color = TextSecondary, fontSize = 12.sp)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Editor Area (Scrollable Box with numbers and Text)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .verticalScroll(scrollState)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Line numbers
                    val lineCount = code.lines().size
                    val lineNumbersText = (1..lineCount).joinToString("\n")
                    
                    Text(
                        text = lineNumbersText,
                        color = TextSecondary.copy(alpha = 0.5f),
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                        lineHeight = 19.sp,
                        modifier = Modifier.width(28.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    // Editable plain Text Field styled with Dracula theme via VisualTransformation
                    BasicTextField(
                        value = code,
                        onValueChange = { code = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 19.sp
                        ),
                        visualTransformation = syntaxHighlightTransformation
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {
                        onRunCode(code, initialLanguage)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SuccessGreen
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Run Code", color = Color.Black, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        isAnalyzing = true
                        coroutineScope.launch {
                            try {
                                val reviewId = reviewRepository.performCodeReview(
                                    title = "Review: ${initialLanguage} Code",
                                    code = code,
                                    language = initialLanguage
                                )
                                onReviewCompleted(reviewId)
                            } catch (e: Exception) {
                                isAnalyzing = false
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryNeonBlue
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Analyze", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("AI Review", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun getLanguageTemplate(language: String): String {
    return when (language.lowercase()) {
        "python" -> """# Python Code Template
def bubble_sort(arr):
    n = len(arr)
    # BUG: Inner loop index out of bounds
    for i in range(n):
        for j in range(0, n):
            if arr[j] > arr[j + 1]:
                arr[j], arr[j + 1] = arr[j + 1], arr[j]
    return arr

# Test run
print(bubble_sort([5, 1, 4, 2, 8]))
"""
        "java" -> """// Java Code Template
public class Main {
    public static void main(String[] args) {
        int[] arr = {5, 1, 4, 2, 8};
        bubbleSort(arr);
    }

    public static void bubbleSort(int[] arr) {
        int n = arr.length;
        // BUG: index out of bounds
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (arr[j] > arr[j + 1]) {
                    int temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;
                }
            }
        }
    }
}
"""
        "kotlin" -> """// Kotlin Code Template
fun main() {
    val arr = intArrayOf(5, 1, 4, 2, 8)
    bubbleSort(arr)
}

fun bubbleSort(arr: IntArray) {
    val n = arr.size
    for (i in 0 until n) {
        // BUG: j+1 exceeds bounds
        for (j in 0 until n) {
            if (arr[j] > arr[j + 1]) {
                val temp = arr[j]
                arr[j] = arr[j + 1]
                arr[j + 1] = temp
            }
        }
    }
}
"""
        "c++", "cpp" -> """// C++ Code Template
#include <iostream>
#include <vector>

void bubbleSort(std::vector<int>& arr) {
    int n = arr.size();
    for (int i = 0; i < n; i++) {
        // BUG: j+1 exceeds bounds
        for (int j = 0; j < n; j++) {
            if (arr[j] > arr[j + 1]) {
                int temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
}

int main() {
    std::vector<int> arr = {5, 1, 4, 2, 8};
    bubbleSort(arr);
    return 0;
}
"""
        "javascript", "js" -> """// JavaScript Code Template
function bubbleSort(arr) {
    let n = arr.length;
    for (let i = 0; i < n; i++) {
        // BUG: j+1 exceeds bounds
        for (let j = 0; j < n; j++) {
            if (arr[j] > arr[j + 1]) {
                let temp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = temp;
            }
        }
    }
    return arr;
}

console.log(bubbleSort([5, 1, 4, 2, 8]));
"""
        else -> """// Write your code snippet here
"""
    }
}
