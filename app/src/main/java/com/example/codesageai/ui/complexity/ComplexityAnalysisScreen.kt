package com.example.codesageai.ui.complexity

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.codesageai.data.repository.CodeReviewRepository
import com.example.codesageai.theme.AccentPurple
import com.example.codesageai.theme.BorderSlate
import com.example.codesageai.theme.DarkBackground
import com.example.codesageai.theme.DarkSurface
import com.example.codesageai.theme.PrimaryNeonBlue
import com.example.codesageai.theme.SecondaryCyan
import com.example.codesageai.theme.SuccessGreen
import com.example.codesageai.theme.TextPrimary
import com.example.codesageai.theme.TextSecondary
import com.example.codesageai.theme.WarningOrange

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComplexityAnalysisScreen(
    reviewId: Long,
    reviewRepository: CodeReviewRepository,
    onBack: () -> Unit
) {
    val reviewState by reviewRepository.getReviewById(reviewId).collectAsStateWithLifecycle(initialValue = null)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Complexity Analysis", color = Color.White, fontWeight = FontWeight.Bold) },
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
        val review = reviewState
        if (review == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Loading details...", color = Color.White)
            }
            return@Scaffold
        }

        // Parse sweeps
        val timeSweep = getSweepAngle(review.timeComplexity)
        val spaceSweep = getSweepAngle(review.spaceComplexity)

        val timeColor = getComplexityColor(review.timeComplexity)
        val spaceColor = getComplexityColor(review.spaceComplexity)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Circular Dials Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Time Complexity Dial
                ComplexityGauge(
                    title = "Time Complexity",
                    complexityLabel = review.timeComplexity,
                    sweepAngle = timeSweep,
                    color = timeColor,
                    modifier = Modifier.weight(1f)
                )

                // Space Complexity Dial
                ComplexityGauge(
                    title = "Space Complexity",
                    complexityLabel = review.spaceComplexity,
                    sweepAngle = spaceSweep,
                    color = spaceColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Bottleneck Description Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = PrimaryNeonBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Analysis Breakdown",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = review.complexityDetails,
                        color = TextPrimary,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Complexity Scale reference Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "Complexity Chart Legend",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LegendItem(label = "O(1) / O(log N) - Efficient / Excellent", color = SuccessGreen)
                    LegendItem(label = "O(N) - Fair / Standard", color = SecondaryCyan)
                    LegendItem(label = "O(N log N) - Marginal / Check constraints", color = WarningOrange)
                    LegendItem(label = "O(N^2) / O(2^N) - Inefficient / Bottleneck", color = Color(0xFFEF4444))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun ComplexityGauge(
    title: String,
    complexityLabel: String,
    sweepAngle: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .border(1.dp, BorderSlate, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(110.dp)
            ) {
                // Background Track
                Canvas(modifier = Modifier.size(90.dp)) {
                    drawArc(
                        color = BorderSlate,
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // Foreground Value Ring with Neon Glow Gradient
                Canvas(modifier = Modifier.size(90.dp)) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            colors = listOf(color.copy(alpha = 0.5f), color)
                        ),
                        startAngle = 140f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = complexityLabel,
                        fontSize = 18.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, RoundedCornerShape(5.dp))
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
    }
}

// Map complexity classes to ring sizes
private fun getSweepAngle(complexity: String): Float {
    val clean = complexity.lowercase().replace(" ", "")
    return when {
        clean.contains("o(1)") -> 50f
        clean.contains("o(logn)") -> 100f
        clean.contains("o(n)") && !clean.contains("logn") -> 150f
        clean.contains("o(nlogn)") -> 200f
        clean.contains("o(n^2)") || clean.contains("o(n2)") -> 260f
        clean.contains("o(2^n)") || clean.contains("o(2n)") || clean.contains("o(n!)") -> 260f
        else -> 150f // Default middle angle
    }
}

// Map complexity classes to colors
private fun getComplexityColor(complexity: String): Color {
    val clean = complexity.lowercase().replace(" ", "")
    return when {
        clean.contains("o(1)") || clean.contains("o(logn)") -> SuccessGreen
        clean.contains("o(n)") && !clean.contains("logn") -> SecondaryCyan
        clean.contains("o(nlogn)") -> WarningOrange
        clean.contains("o(n^2)") || clean.contains("o(n2)") || clean.contains("o(2^n)") || clean.contains("o(n!)") -> Color(0xFFEF4444)
        else -> PrimaryNeonBlue
    }
}
