package com.example.codesageai.ui.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import java.util.regex.Pattern

object SyntaxHighlighter {

    private val KEYWORDS = setOf(
        "class", "interface", "fun", "def", "val", "var", "let", "const", "return", "if", "else",
        "for", "while", "import", "package", "public", "private", "protected", "override", "true",
        "false", "null", "void", "int", "float", "double", "string", "boolean", "char", "short",
        "long", "byte", "import", "from", "as", "in", "is", "try", "except", "catch", "finally",
        "throw", "new", "this", "super", "break", "continue", "switch", "case", "default"
    )

    // Regex patterns
    private val commentPattern = Pattern.compile("(//.*)|(#.*)|(/\\*[\\s\\S]*?\\*/)")
    private val stringPattern = Pattern.compile("(\".*?\")|('.*?')")
    private val numberPattern = Pattern.compile("\\b\\d+(\\.\\d+)?\\b")
    private val annotationPattern = Pattern.compile("@\\w+")

    // Color Theme (Obsidian/Dracula Style)
    val KeywordColor = Color(0xFFFF79C6) // Pink
    val CommentColor = Color(0xFF6272A4) // Grey/blue
    val StringColor = Color(0xFF50FA7B) // Green
    val NumberColor = Color(0xFFBD93F9) // Purple
    val AnnotationColor = Color(0xFFFFB86C) // Orange
    val DefaultColor = Color(0xFFF8F8F2) // White-ish

    fun highlight(code: String): AnnotatedString {
        return buildAnnotatedString {
            // Start with default style
            append(code)
            addStyle(SpanStyle(color = DefaultColor, fontFamily = FontFamily.Monospace), 0, code.length)

            // Highlight keywords using boundary matching
            val words = code.split(Regex("[\\s()+\\-*/=<>!;.,\\[\\]{}&|~%#]+"))
            var startIndex = 0
            for (word in words) {
                if (word in KEYWORDS) {
                    val idx = code.indexOf(word, startIndex)
                    if (idx != -1) {
                        // Double check it matches word boundary to avoid partial highlight
                        val charBefore = if (idx > 0) code[idx - 1] else ' '
                        val charAfter = if (idx + word.length < code.length) code[idx + word.length] else ' '
                        if (!charBefore.isLetterOrDigit() && !charAfter.isLetterOrDigit()) {
                            addStyle(
                                SpanStyle(color = KeywordColor, fontWeight = FontWeight.Bold),
                                idx,
                                idx + word.length
                            )
                        }
                        startIndex = idx + word.length
                    }
                }
            }

            // Highlight annotations
            val annotationMatcher = annotationPattern.matcher(code)
            while (annotationMatcher.find()) {
                addStyle(SpanStyle(color = AnnotationColor), annotationMatcher.start(), annotationMatcher.end())
            }

            // Highlight numbers
            val numberMatcher = numberPattern.matcher(code)
            while (numberMatcher.find()) {
                addStyle(SpanStyle(color = NumberColor), numberMatcher.start(), numberMatcher.end())
            }

            // Highlight strings (which overrides numbers and keywords inside strings)
            val stringMatcher = stringPattern.matcher(code)
            while (stringMatcher.find()) {
                addStyle(SpanStyle(color = StringColor), stringMatcher.start(), stringMatcher.end())
            }

            // Highlight comments (which overrides everything else)
            val commentMatcher = commentPattern.matcher(code)
            while (commentMatcher.find()) {
                addStyle(SpanStyle(color = CommentColor), commentMatcher.start(), commentMatcher.end())
            }
        }
    }
}
