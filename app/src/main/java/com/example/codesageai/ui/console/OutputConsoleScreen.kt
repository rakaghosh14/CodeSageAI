package com.example.codesageai.ui.console

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codesageai.data.remote.Judge0Response
import com.example.codesageai.data.repository.CodeReviewRepository
import com.example.codesageai.theme.BorderSlate
import com.example.codesageai.theme.DarkBackground
import com.example.codesageai.theme.DarkSurface
import com.example.codesageai.theme.ErrorRed
import com.example.codesageai.theme.PrimaryNeonBlue
import com.example.codesageai.theme.SuccessGreen
import com.example.codesageai.theme.TextSecondary
import com.example.codesageai.theme.WarningOrange
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutputConsoleScreen(
    code: String,
    language: String,
    reviewRepository: CodeReviewRepository,
    onBack: () -> Unit
) {
    var isExecuting by remember { mutableStateOf(true) }
    var executionResult by remember { mutableStateOf<Judge0Response?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    fun triggerExecution() {
        isExecuting = true
        coroutineScope.launch {
            try {
                val res = reviewRepository.executeCode(code, language)
                executionResult = res
                isExecuting = false
            } catch (e: Exception) {
                isExecuting = false
            }
        }
    }

    LaunchedEffect(key1 = true) {
        triggerExecution()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Output Console", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Simulated retro terminal window
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF020617), RoundedCornerShape(12.dp)) // Deep obsidian black
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
            ) {
                // Terminal window header bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface, RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Simulated window controls
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFEF4444)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFFF59E0B)))
                        Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981)))
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Terminal,
                            contentDescription = "Console",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "terminal.sh ($language)",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                // Terminal body
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(scrollState)
                ) {
                    if (isExecuting) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Spacer(modifier = Modifier.height(48.dp))
                            CircularProgressIndicator(color = PrimaryNeonBlue, modifier = Modifier.size(28.dp))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Connecting to Judge0 API cloud...",
                                color = PrimaryNeonBlue,
                                fontSize = 12.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "Compiling and executing code sandbox...",
                                color = TextSecondary,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    } else {
                        val res = executionResult
                        Column(modifier = Modifier.fillMaxWidth()) {
                            // Compile Output if error
                            if (!res?.compile_output.isNullOrBlank()) {
                                Text(
                                    text = res?.compile_output ?: "",
                                    color = WarningOrange,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Standard Output
                            if (!res?.stdout.isNullOrBlank()) {
                                Text(
                                    text = res?.stdout ?: "",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Standard Error
                            if (!res?.stderr.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = res?.stderr ?: "",
                                    color = ErrorRed,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            // Empty stdout/stderr case
                            if (res != null && res.stdout.isNullOrBlank() && res.stderr.isNullOrBlank() && res.compile_output.isNullOrBlank()) {
                                Text(
                                    text = "Code executed. Return code: ${res.status?.id} (${res.status?.description})",
                                    color = SuccessGreen,
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))
                            
                            // Metrics block
                            if (res != null) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(DarkSurface.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Time: ${res.time ?: "0.00"}s",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Memory: ${res.memory ?: 0} KB",
                                        color = TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Text(
                                        text = "Status: ${res.status?.description ?: "N/A"}",
                                        color = if (res.status?.id == 3) SuccessGreen else ErrorRed,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Re-run Button
            Button(
                onClick = { triggerExecution() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = !isExecuting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeonBlue,
                    disabledContainerColor = BorderSlate
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run", tint = Color.Black)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Re-execute Code", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
