package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun FormattedAstraText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val blocks = remember(text) { parseMarkdownBlocks(text) }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Code -> {
                    CodeBlockView(
                        language = block.language,
                        code = block.content,
                        onCopy = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("Code", block.content)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Code copied to clipboard!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
                is MarkdownBlock.Header -> {
                    Text(
                        text = parseInlineMarkdown(block.content),
                        style = when (block.level) {
                            1 -> MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            2 -> MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
                            else -> MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                        },
                        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                    )
                }
                is MarkdownBlock.Bullet -> {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "• ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = parseInlineMarkdown(block.content),
                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.Numbered -> {
                    Row(
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp, bottom = 2.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${block.number}. ",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = parseInlineMarkdown(block.content),
                            style = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is MarkdownBlock.Quote -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .fillMaxHeight()
                                .background(MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = parseInlineMarkdown(block.content),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
                is MarkdownBlock.Paragraph -> {
                    Text(
                        text = parseInlineMarkdown(block.content),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = textColor,
                            lineHeight = 22.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun CodeBlockView(
    language: String,
    code: String,
    onCopy: () -> Unit
) {
    var copied by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F141C)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column {
            // Header bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF161E2E))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = language.ifBlank { "code" }.uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                )

                IconButton(
                    onClick = {
                        onCopy()
                        copied = true
                        scope.launch {
                            delay(2000)
                            copied = false
                        }
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    if (copied) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Copied",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy code",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Code content
            Box(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = code,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFFE2E8F0),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

sealed class MarkdownBlock {
    data class Paragraph(val content: String) : MarkdownBlock()
    data class Header(val level: Int, val content: String) : MarkdownBlock()
    data class Bullet(val content: String) : MarkdownBlock()
    data class Numbered(val number: String, val content: String) : MarkdownBlock()
    data class Code(val language: String, val content: String) : MarkdownBlock()
    data class Quote(val content: String) : MarkdownBlock()
}

fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = rawText.lines()
    var i = 0

    while (i < lines.size) {
        val line = lines[i]

        // Code block check
        if (line.trim().startsWith("```")) {
            val language = line.trim().removePrefix("```").trim()
            val codeLines = mutableListOf<String>()
            i++
            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                codeLines.add(lines[i])
                i++
            }
            blocks.add(MarkdownBlock.Code(language, codeLines.joinToString("\n")))
            i++
            continue
        }

        val trimmed = line.trim()

        when {
            trimmed.startsWith("### ") -> {
                blocks.add(MarkdownBlock.Header(3, trimmed.removePrefix("### ")))
            }
            trimmed.startsWith("## ") -> {
                blocks.add(MarkdownBlock.Header(2, trimmed.removePrefix("## ")))
            }
            trimmed.startsWith("# ") -> {
                blocks.add(MarkdownBlock.Header(1, trimmed.removePrefix("# ")))
            }
            trimmed.startsWith("* ") || trimmed.startsWith("- ") -> {
                blocks.add(MarkdownBlock.Bullet(trimmed.substring(2)))
            }
            trimmed.matches(Regex("^\\d+\\.\\s+.*")) -> {
                val num = trimmed.substringBefore(".")
                val content = trimmed.substringAfter(".").trim()
                blocks.add(MarkdownBlock.Numbered(num, content))
            }
            trimmed.startsWith("> ") -> {
                blocks.add(MarkdownBlock.Quote(trimmed.removePrefix("> ")))
            }
            trimmed.isNotBlank() -> {
                blocks.add(MarkdownBlock.Paragraph(trimmed))
            }
        }
        i++
    }
    return blocks
}

fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var cursor = 0
        val boldRegex = Regex("\\*\\*(.*?)\\*\\*")
        val codeInlineRegex = Regex("`(.*?)`")

        var remaining = text
        var offset = 0

        // Parse bold and inline code simply
        val tokens = mutableListOf<Pair<IntRange, SpanStyle>>()

        boldRegex.findAll(text).forEach { match ->
            tokens.add(match.range to SpanStyle(fontWeight = FontWeight.Bold))
        }

        // Simpler clean build
        var index = 0
        while (index < text.length) {
            if (index + 1 < text.length && text[index] == '*' && text[index + 1] == '*') {
                val end = text.indexOf("**", index + 2)
                if (end != -1) {
                    val boldContent = text.substring(index + 2, end)
                    pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                    append(boldContent)
                    pop()
                    index = end + 2
                    continue
                }
            } else if (text[index] == '`') {
                val end = text.indexOf('`', index + 1)
                if (end != -1) {
                    val codeContent = text.substring(index + 1, end)
                    pushStyle(SpanStyle(fontFamily = FontFamily.Monospace, background = Color(0x336366F1), color = Color(0xFF818CF8)))
                    append(" $codeContent ")
                    pop()
                    index = end + 1
                    continue
                }
            }
            append(text[index])
            index++
        }
    }
}
