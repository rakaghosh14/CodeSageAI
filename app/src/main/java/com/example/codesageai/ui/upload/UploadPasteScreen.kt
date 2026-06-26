package com.example.codesageai.ui.upload

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.codesageai.theme.BorderSlate
import com.example.codesageai.theme.DarkBackground
import com.example.codesageai.theme.DarkSurface
import com.example.codesageai.theme.PrimaryNeonBlue
import com.example.codesageai.theme.SecondaryCyan
import com.example.codesageai.theme.TextPrimary
import com.example.codesageai.theme.TextSecondary
import java.io.BufferedReader
import java.io.InputStreamReader


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadPasteScreen(
    onCodeImported: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    var codeText by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("Detect Automatically") }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var uploadedFileName by remember { mutableStateOf<String?>(null) }

    val languages = listOf(
        "Detect Automatically", "Java", "Python", "C++", "Kotlin", "JavaScript", "C"
    )

    // Helper to read URI content
    fun readTextFromUri(context: Context, uri: Uri): String {
        val stringBuilder = StringBuilder()
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    stringBuilder.append(line).append("\n")
                }
            }
        }
        return stringBuilder.toString()
    }

    // Helper to get filename
    fun getFileName(context: Context, uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index != -1) {
                        result = it.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "source_code.txt"
    }

    // Launcher for file picking
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val fileName = getFileName(context, it)
                val content = readTextFromUri(context, it)
                codeText = content
                uploadedFileName = fileName

                // Auto detect from extension
                val ext = fileName.substringAfterLast('.', "").lowercase()
                selectedLanguage = when (ext) {
                    "java" -> "Java"
                    "py" -> "Python"
                    "cpp", "cc", "h" -> "C++"
                    "kt", "kts" -> "Kotlin"
                    "js" -> "JavaScript"
                    "c" -> "C"
                    else -> "Detect Automatically"
                }
            } catch (e: Exception) {
                // Ignore or handle
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Import Source Code", color = Color.White, fontWeight = FontWeight.Bold) },
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
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // File Upload Section
            Text(
                text = "File Upload",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .background(DarkSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, BorderSlate, RoundedCornerShape(16.dp))
                    .clickable { filePickerLauncher.launch("*/*") }
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FileUpload,
                        contentDescription = "Upload",
                        tint = PrimaryNeonBlue,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = uploadedFileName ?: "Tap to choose a source code file",
                        color = if (uploadedFileName != null) Color.White else TextSecondary,
                        fontWeight = if (uploadedFileName != null) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Supports .java, .py, .cpp, .js, .kt, etc.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Paste Code Section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Or Paste Code Manually",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurface)
                        .clickable {
                            clipboardManager.getText()?.let {
                                codeText = it.text
                                uploadedFileName = null
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentPaste,
                        contentDescription = "Paste",
                        tint = PrimaryNeonBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Paste", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = codeText,
                onValueChange = {
                    codeText = it
                    if (uploadedFileName != null) uploadedFileName = null
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                placeholder = { Text("Write or paste your code snippet here...", color = TextSecondary) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryNeonBlue,
                    unfocusedBorderColor = BorderSlate,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                shape = RoundedCornerShape(14.dp),
                maxLines = 15
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Language Selector Section
            Text(
                text = "Programming Language",
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface, RoundedCornerShape(12.dp))
                    .border(1.dp, BorderSlate, RoundedCornerShape(12.dp))
                    .clickable { dropdownExpanded = true }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = selectedLanguage, color = Color.White, fontSize = 14.sp)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Dropdown", tint = Color.White)
                }

                DropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(DarkSurface)
                        .border(1.dp, BorderSlate, RoundedCornerShape(8.dp))
                ) {
                    languages.forEach { lang ->
                        DropdownMenuItem(
                            text = { Text(lang, color = Color.White) },
                            onClick = {
                                selectedLanguage = lang
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Load to Editor Button
            Button(
                onClick = {
                    if (codeText.isNotBlank()) {
                        // Resolve actual auto-detected language if set
                        val language = if (selectedLanguage == "Detect Automatically") {
                            detectLanguage(codeText)
                        } else {
                            selectedLanguage
                        }
                        onCodeImported(codeText, language)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = codeText.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryNeonBlue,
                    disabledContainerColor = BorderSlate
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Load Into Editor",
                    color = if (codeText.isNotBlank()) Color.Black else TextSecondary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// Basic auto-detector based on code semantics/keywords
fun detectLanguage(code: String): String {
    val lower = code.lowercase()
    return when {
        lower.contains("def ") || lower.contains("import pandas") || lower.contains("print(") && !lower.contains(";") -> "Python"
        lower.contains("public class ") || lower.contains("system.out.print") -> "Java"
        lower.contains("#include") || lower.contains("std::cout") -> "C++"
        lower.contains("fun main") || lower.contains("val ") || lower.contains("var ") && lower.contains(":") -> "Kotlin"
        lower.contains("const ") || lower.contains("let ") || lower.contains("console.log") -> "JavaScript"
        else -> "Java" // Default fallback
    }
}
