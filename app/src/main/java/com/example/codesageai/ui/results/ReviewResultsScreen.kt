package com.example.codesageai.ui.results

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.codesageai.data.local.CodeReviewEntity
import com.example.codesageai.data.remote.AiReviewPayload
import com.example.codesageai.data.remote.BugReport
import com.example.codesageai.data.remote.OptimizationReport
import com.example.codesageai.data.remote.StyleReport
import com.example.codesageai.data.repository.CodeReviewRepository
import com.example.codesageai.theme.AccentPurple
import com.example.codesageai.theme.BorderSlate
import com.example.codesageai.theme.DarkBackground
import com.example.codesageai.theme.DarkSurface
import com.example.codesageai.theme.ErrorRed
import com.example.codesageai.theme.PrimaryNeonBlue
import com.example.codesageai.theme.SecondaryCyan
import com.example.codesageai.theme.SuccessGreen
import com.example.codesageai.theme.TextPrimary
import com.example.codesageai.theme.TextSecondary
import com.example.codesageai.theme.WarningOrange
import com.example.codesageai.ui.utils.PdfExporter
import com.example.codesageai.ui.utils.SyntaxHighlighter
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewResultsScreen(
    reviewId: Long,
    reviewRepository: CodeReviewRepository,
    onNavigateToComplexity: (Long) -> Unit,
    onNavigateToChat: (Long) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val reviewState by reviewRepository.getReviewById(reviewId).collectAsStateWithLifecycle(initialValue = null)
    var selectedTab by remember { mutableIntStateOf(0) }
    val gson = remember { Gson() }

    val tabs = listOf("Bugs", "Optimizations", "Style", "Diff Code")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Analysis Report", color = Color.White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    reviewState?.let { r ->
                        IconButton(onClick = {
                            PdfExporter.exportToPdf(context, r)
                        }) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = "Export PDF", tint = PrimaryNeonBlue)
                        }
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
                Text("Loading review details...", color = Color.White)
            }
            return@Scaffold
        }

        // Parse review details
        val payload = remember(review.rawAiReview) {
            try {
                gson.fromJson(review.rawAiReview, AiReviewPayload::class.java)
            } catch (e: Exception) {
                AiReviewPayload(
                    bugs = emptyList(),
                    optimizations = emptyList(),
                    readabilityStyle = emptyList(),
                    explanation = "Explanation could not be parsed."
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = DarkBackground,
                contentColor = PrimaryNeonBlue,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = PrimaryNeonBlue
                    )
                },
                divider = { Box(modifier = Modifier.height(1.dp).background(BorderSlate)) }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontSize = 13.sp,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTab == index) PrimaryNeonBlue else TextSecondary
                            )
                        }
                    )
                }
            }

            // Tab Content scrollable area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                when (selectedTab) {
                    0 -> BugsTabContent(payload.bugs)
                    1 -> OptimizationsTabContent(payload.optimizations)
                    2 -> StyleTabContent(payload.readabilityStyle)
                    3 -> CodeDiffTabContent(review.code, review.improvedCode)
                }
            }

            // Bottom Navigation Actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    { onNavigateToComplexity(reviewId) }, Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurface),
                    border = BorderStroke(
                        1.dp,
                        BorderSlate
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = "Complexity", tint = PrimaryNeonBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Complexity", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onNavigateToChat(reviewId) },
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryNeonBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Chat, contentDescription = "Chat", tint = Color.Black)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ask CodeSage", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BugsTabContent(bugs: List<BugReport>) {
    if (bugs.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface)
        ) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.BugReport,
                    contentDescription = "No bugs",
                    tint = SuccessGreen,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Excellent Work!",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    "AI reviewed this code and found zero logical bugs or security vulnerabilities.",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }
        }
    } else {
        bugs.forEach { bug ->
            BugReportCard(bug = bug)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun BugReportCard(bug: BugReport) {
    val severityColor = when (bug.severity.lowercase()) {
        "high" -> ErrorRed
        "medium" -> WarningOrange
        else -> SecondaryCyan
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderSlate, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(severityColor, RoundedCornerShape(4.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Line ${bug.line}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Severity Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(severityColor.copy(alpha = 0.2f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = bug.severity,
                        color = severityColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = bug.description, color = TextPrimary, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(12.dp))

            // Original Snippet
            Text("Original code:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                text = bug.originalSnippet,
                color = ErrorRed,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Suggested Fix
            Text("Suggested fix:", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                text = bug.suggestedSnippet,
                color = SuccessGreen,
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                    .padding(8.dp)
            )
        }
    }
}

@Composable
fun OptimizationsTabContent(opts: List<OptimizationReport>) {
    if (opts.isEmpty()) {
        Text("No optimizations recommended.", color = TextSecondary, fontSize = 14.sp)
    } else {
        opts.forEach { opt ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Line ${opt.line} Optimization", color = WarningOrange, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = opt.description, color = TextPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Replace:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = opt.originalSnippet,
                        color = ErrorRed,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text("With:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = opt.suggestedSnippet,
                        color = SuccessGreen,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun StyleTabContent(styles: List<StyleReport>) {
    if (styles.isEmpty()) {
        Text("Your style is perfect!", color = SuccessGreen, fontSize = 14.sp)
    } else {
        styles.forEach { style ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSlate, RoundedCornerShape(14.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Line ${style.line} Readability Style", color = AccentPurple, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = style.description, color = TextPrimary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Recommendation:", color = TextSecondary, fontSize = 11.sp)
                    Text(
                        text = style.suggestedChange,
                        color = PrimaryNeonBlue,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
fun CodeDiffTabContent(original: String, improved: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Original Code", color = TextSecondary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = SyntaxHighlighter.highlight(original),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Improved AI Version", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                .padding(12.dp)
        ) {
            Text(
                text = SyntaxHighlighter.highlight(improved),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                lineHeight = 16.sp
            )
        }
    }
}
