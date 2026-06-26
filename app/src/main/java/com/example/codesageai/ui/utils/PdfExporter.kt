package com.example.codesageai.ui.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.codesageai.data.local.CodeReviewEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun exportToPdf(context: Context, review: CodeReviewEntity): File? {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        
        var yPosition = 50f
        val margin = 40f
        val contentWidth = 595 - (margin * 2)

        // Helper to draw text and wrap lines
        fun drawWrappedText(text: String, size: Float, isBold: Boolean, color: Int) {
            paint.textSize = size
            paint.isFakeBoldText = isBold
            paint.color = color
            
            val lines = text.split("\n")
            for (line in lines) {
                var words = line.split(" ")
                var currentLine = ""
                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    val width = paint.measureText(testLine)
                    if (width > contentWidth) {
                        // Check if we need to start a new page
                        if (yPosition > 800) {
                            pdfDocument.finishPage(page)
                            page = pdfDocument.startPage(pageInfo)
                            canvas = page.canvas
                            yPosition = 50f
                            // Re-apply paint styles
                            paint.textSize = size
                            paint.isFakeBoldText = isBold
                            paint.color = color
                        }
                        canvas.drawText(currentLine, margin, yPosition, paint)
                        yPosition += size + 6
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                if (currentLine.isNotEmpty()) {
                    if (yPosition > 800) {
                        pdfDocument.finishPage(page)
                        page = pdfDocument.startPage(pageInfo)
                        canvas = page.canvas
                        yPosition = 50f
                        paint.textSize = size
                        paint.isFakeBoldText = isBold
                        paint.color = color
                    }
                    canvas.drawText(currentLine, margin, yPosition, paint)
                    yPosition += size + 6
                }
            }
            yPosition += 10f // spacing after paragraph
        }

        // Draw Header
        drawWrappedText("CODESAGE AI - INTELLIGENT CODE REVIEW REPORT", 18f, true, Color.parseColor("#4F46E5"))
        val dateString = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(review.timestamp))
        drawWrappedText("Generated on: $dateString", 10f, false, Color.GRAY)
        
        // Draw Divider
        paint.color = Color.LTGRAY
        canvas.drawRect(margin, yPosition, 595f - margin, yPosition + 2, paint)
        yPosition += 20f

        // Draw Metadata Info
        drawWrappedText("File Name / Title: ${review.title}", 12f, true, Color.BLACK)
        drawWrappedText("Language Detected: ${review.language}", 12f, true, Color.BLACK)
        drawWrappedText("Estimated Time Complexity: ${review.timeComplexity}", 12f, true, Color.parseColor("#D97706"))
        drawWrappedText("Estimated Space Complexity: ${review.spaceComplexity}", 12f, true, Color.parseColor("#2563EB"))
        
        // Complexity Details
        drawWrappedText("Complexity Bottleneck Analysis:", 12f, true, Color.BLACK)
        drawWrappedText(review.complexityDetails, 10f, false, Color.DKGRAY)

        // Draw Divider
        paint.color = Color.LTGRAY
        canvas.drawRect(margin, yPosition, 595f - margin, yPosition + 2, paint)
        yPosition += 20f

        // Original Code (truncated/formatted)
        drawWrappedText("Original Source Code Reviewed:", 12f, true, Color.BLACK)
        // Split code to fit pages cleanly
        val codeLines = review.code.lines()
        val formattedCode = codeLines.take(30).joinToString("\n") + if (codeLines.size > 30) "\n... [truncated for space]" else ""
        drawWrappedText(formattedCode, 9f, false, Color.parseColor("#1E293B"))

        // Add page break if needed for review results
        pdfDocument.finishPage(page)
        page = pdfDocument.startPage(pageInfo)
        canvas = page.canvas
        yPosition = 50f

        drawWrappedText("AI Feedback & Recommendations:", 14f, true, Color.parseColor("#4F46E5"))
        
        // Clean JSON printing
        try {
            val rawMap = com.google.gson.Gson().fromJson(review.rawAiReview, Map::class.java)
            val bugs = rawMap["bugs"] as? List<*>
            if (!bugs.isNullOrEmpty()) {
                drawWrappedText("Detected Bugs & Vulnerabilities (${bugs.size})", 12f, true, Color.parseColor("#DC2626"))
                for (b in bugs) {
                    val bugMap = b as? Map<*, *>
                    val line = (bugMap?.get("line") as? Double)?.toInt() ?: 0
                    val desc = bugMap?.get("description") as? String ?: ""
                    val sev = bugMap?.get("severity") as? String ?: ""
                    drawWrappedText("• Line $line [Severity: $sev]: $desc", 10f, false, Color.DKGRAY)
                }
            } else {
                drawWrappedText("✔ No bugs or vulnerabilities detected.", 11f, true, Color.parseColor("#16A34A"))
            }

            val opts = rawMap["optimizations"] as? List<*>
            if (!opts.isNullOrEmpty()) {
                yPosition += 10f
                drawWrappedText("Performance Optimization Tips", 12f, true, Color.parseColor("#D97706"))
                for (o in opts) {
                    val optMap = o as? Map<*, *>
                    val line = (optMap?.get("line") as? Double)?.toInt() ?: 0
                    val desc = optMap?.get("description") as? String ?: ""
                    drawWrappedText("• Line $line: $desc", 10f, false, Color.DKGRAY)
                }
            }

            val styles = rawMap["readabilityStyle"] as? List<*>
            if (!styles.isNullOrEmpty()) {
                yPosition += 10f
                drawWrappedText("Readability & Style Improvements", 12f, true, Color.parseColor("#2563EB"))
                for (s in styles) {
                    val styleMap = s as? Map<*, *>
                    val line = (styleMap?.get("line") as? Double)?.toInt() ?: 0
                    val desc = styleMap?.get("description") as? String ?: ""
                    val sug = styleMap?.get("suggestedChange") as? String ?: ""
                    drawWrappedText("• Line $line: $desc (Suggestion: $sug)", 10f, false, Color.DKGRAY)
                }
            }
        } catch (e: Exception) {
            drawWrappedText("Raw AI Review Feedback:\n" + review.rawAiReview, 9f, false, Color.DKGRAY)
        }

        // Finish last page
        pdfDocument.finishPage(page)

        // Write document to file
        val filename = "CodeSageAI_Report_${review.id}_${System.currentTimeMillis()}.pdf"
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
        val file = File(directory, filename)

        return try {
            val fos = FileOutputStream(file)
            pdfDocument.writeTo(fos)
            pdfDocument.close()
            fos.close()
            Toast.makeText(context, "Report exported to Downloads folder", Toast.LENGTH_LONG).show()
            file
        } catch (e: Exception) {
            pdfDocument.close()
            Toast.makeText(context, "Failed to export PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            null
        }
    }
}
