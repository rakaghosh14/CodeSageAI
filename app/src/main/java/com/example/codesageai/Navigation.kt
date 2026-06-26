package com.example.codesageai

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.codesageai.data.local.AppDatabase
import com.example.codesageai.data.repository.CodeReviewRepository
import com.example.codesageai.data.repository.SettingsRepository
import com.example.codesageai.ui.chat.ChatWithCodeScreen
import com.example.codesageai.ui.complexity.ComplexityAnalysisScreen
import com.example.codesageai.ui.console.OutputConsoleScreen
import com.example.codesageai.ui.editor.CodeEditorScreen
import com.example.codesageai.ui.history.ReviewHistoryScreen
import com.example.codesageai.ui.home.HomeScreen
import com.example.codesageai.ui.results.ReviewResultsScreen
import com.example.codesageai.ui.settings.SettingsScreen
import com.example.codesageai.ui.splash.SplashScreen
import com.example.codesageai.ui.upload.UploadPasteScreen

@Composable
fun MainNavigation() {
    val context = LocalContext.current.applicationContext
    
    // Core Singletons
    val db = remember { AppDatabase.getDatabase(context) }
    val settingsRepository = remember { SettingsRepository(context) }
    val reviewRepository = remember {
        CodeReviewRepository(
            db.codeReviewDao(),
            db.chatMessageDao(),
            settingsRepository
        )
    }

    // Navigation Back Stack - starts at Splash screen
    val backStack = rememberNavBackStack(Splash)

    NavDisplay(
        backStack = backStack,
        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
        modifier = Modifier.fillMaxSize(),
        entryProvider = entryProvider {
            entry<Splash> {
                SplashScreen(
                    onAnimationFinished = {
                        backStack.add(Home)
                    }
                )
            }
            entry<Home> {
                HomeScreen(
                    reviewRepository = reviewRepository,
                    onNavigate = { key -> backStack.add(key) }
                )
            }
            entry<UploadPaste> {
                UploadPasteScreen(
                    onCodeImported = { code, lang ->
                        backStack.add(CodeEditor(code, lang))
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<CodeEditor> { key ->
                CodeEditorScreen(
                    initialCode = key.initialCode,
                    initialLanguage = key.initialLanguage,
                    reviewRepository = reviewRepository,
                    onReviewCompleted = { reviewId ->
                        backStack.add(ReviewResults(reviewId))
                    },
                    onRunCode = { code, lang ->
                        backStack.add(OutputConsole(code, lang))
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ReviewResults> { key ->
                ReviewResultsScreen(
                    reviewId = key.reviewId,
                    reviewRepository = reviewRepository,
                    onNavigateToComplexity = { id ->
                        backStack.add(ComplexityAnalysis(id))
                    },
                    onNavigateToChat = { id ->
                        backStack.add(ChatWithCode(id))
                    },
                    onBack = {
                        // Pop back to Home
                        backStack.removeLastOrNull()
                    }
                )
            }
            entry<ComplexityAnalysis> { key ->
                ComplexityAnalysisScreen(
                    reviewId = key.reviewId,
                    reviewRepository = reviewRepository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<OutputConsole> { key ->
                OutputConsoleScreen(
                    code = key.code,
                    language = key.language,
                    reviewRepository = reviewRepository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ChatWithCode> { key ->
                ChatWithCodeScreen(
                    reviewId = key.reviewId,
                    reviewRepository = reviewRepository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<ReviewHistory> {
                ReviewHistoryScreen(
                    reviewRepository = reviewRepository,
                    onReviewSelected = { id ->
                        backStack.add(ReviewResults(id))
                    },
                    onBack = { backStack.removeLastOrNull() }
                )
            }
            entry<Settings> {
                SettingsScreen(
                    settingsRepository = settingsRepository,
                    onBack = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
