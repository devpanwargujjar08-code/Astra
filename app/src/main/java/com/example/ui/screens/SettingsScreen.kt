package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AstraAvatar
import com.example.ui.theme.AstraCyanLight
import com.example.ui.theme.AstraIndigoLight
import com.example.viewmodel.AstraViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: AstraViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val customKey by viewModel.preferences.customApiKey.collectAsState()
    val selectedModel by viewModel.preferences.selectedModel.collectAsState()
    val toneStyle by viewModel.preferences.toneStyle.collectAsState()
    val languagePref by viewModel.preferences.languagePreference.collectAsState()
    val ttsEnabled by viewModel.preferences.ttsEnabled.collectAsState()

    var keyInput by remember(customKey) { mutableStateOf(customKey) }
    var showKey by remember { mutableStateOf(false) }

    val hasKey = viewModel.hasValidApiKey()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Astra Settings & Personality",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Astra Profile Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AstraAvatar(size = 48.dp)
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Astra Assistant v1.0",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Friendly, empathetic, and all-in-one personal AI assistant.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

            // Gemini API Key Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = AstraIndigoLight)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Gemini AI Connection",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Status badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (hasKey) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(if (hasKey) Color(0xFF10B981) else Color(0xFFF59E0B))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (hasKey) "Connected" else "Key Required",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (hasKey) Color(0xFF10B981) else Color(0xFFF59E0B),
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "You can enter your own Gemini API key or use the environment key from AI Studio Secrets panel.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = keyInput,
                        onValueChange = { keyInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("api_key_input"),
                        placeholder = { Text("Paste AI Studio Gemini Key...") },
                        visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showKey = !showKey }) {
                                Icon(
                                    imageVector = if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle visibility"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (customKey.isNotEmpty()) {
                            TextButton(
                                onClick = {
                                    viewModel.preferences.setCustomApiKey("")
                                    keyInput = ""
                                    Toast.makeText(context, "Custom key cleared", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Text("Clear Custom Key", color = MaterialTheme.colorScheme.error)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Button(
                            onClick = {
                                viewModel.preferences.setCustomApiKey(keyInput)
                                Toast.makeText(context, "API Key saved successfully!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AstraIndigoLight),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Save Key")
                        }
                    }
                }
            }

            // Language Preference Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Translate, contentDescription = null, tint = AstraCyanLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Language & Vernacular Style",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val languages = listOf(
                        "Auto-detect (English / Hindi / Hinglish)",
                        "Hinglish (Natural conversational blend)",
                        "English (Clear and structured)",
                        "Hindi (Shuddh evam spasht)"
                    )

                    languages.forEach { lang ->
                        val isSelected = languagePref == lang
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.preferences.setLanguagePreference(lang) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.preferences.setLanguagePreference(lang) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = lang,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            )
                        }
                    }
                }
            }

            // Tone & Style Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Psychology, contentDescription = null, tint = AstraIndigoLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Astra Assistant Persona Tone",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val tones = listOf(
                        "Friendly & Practical" to "Empathetic, clear, step-by-step guidance without robotic fluff.",
                        "Deep & Academic" to "In-depth explanations with mathematical/scientific rigorous foundations.",
                        "Direct & Concise" to "Fast, bulleted takeaways for quick execution."
                    )

                    tones.forEach { (tone, desc) ->
                        val isSelected = toneStyle == tone
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.preferences.setToneStyle(tone) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.preferences.setToneStyle(tone) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = tone,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Gemini Model Selection
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Memory, contentDescription = null, tint = AstraCyanLight)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Gemini Model Selection",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    val models = listOf(
                        "gemini-3.5-flash" to "Standard high-performance model for all-round daily tasks & problem solving.",
                        "gemini-3.1-pro-preview" to "Advanced reasoning model for complex STEM, coding, and architecture.",
                        "gemini-3.1-flash-lite-preview" to "Ultra-fast low-latency model for rapid brainstorming."
                    )

                    models.forEach { (model, desc) ->
                        val isSelected = selectedModel == model
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.preferences.setSelectedModel(model) }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { viewModel.preferences.setSelectedModel(model) }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = model,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                    )
                                )
                                Text(
                                    text = desc,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Safety & Ethics Notice
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = AstraIndigoLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Safety & Ethics: Astra upholds respectful, ethical, and safe guidelines. Harmful or illegal queries will be politely refused with safe alternatives.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
