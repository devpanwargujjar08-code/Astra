package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.theme.AstraCyanLight
import com.example.ui.theme.AstraIndigoLight
import java.util.Locale

@Composable
fun ChatInputBar(
    inputText: String,
    onInputTextChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    isGenerating: Boolean,
    onStopGeneration: () -> Unit,
    quickSuggestions: List<String>,
    onSuggestionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isListening by remember { mutableStateOf(false) }

    // Pulsing animation for the active microphone state
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isListening) 1.25f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Speech-to-text launcher using Android RecognizerIntent
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val spokenTextList = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenTextList?.firstOrNull()?.trim()
            if (!spokenText.isNullOrBlank()) {
                val newText = if (inputText.isBlank()) spokenText else "$inputText $spokenText"
                onInputTextChange(newText)
                Toast.makeText(context, "Voice input captured: \"$spokenText\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Function to launch the Android Speech Recognizer dialog
    fun startSpeechRecognizer() {
        try {
            isListening = true
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak to Astra (English, Hindi, or Hinglish)...")
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            isListening = false
            Toast.makeText(
                context,
                "Voice recognition is not available or disabled on this device.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // Runtime permission launcher for RECORD_AUDIO
    val recordAudioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognizer()
        } else {
            isListening = false
            Toast.makeText(
                context,
                "Microphone permission is needed to speak your queries to Astra.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(top = 4.dp, bottom = 8.dp)
    ) {
        // Quick suggestions row
        if (quickSuggestions.isNotEmpty() && !isGenerating) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                quickSuggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { onSuggestionSelected(suggestion) },
                        label = {
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 12.sp)
                            )
                        },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }
        }

        // Voice active banner
        AnimatedVisibility(visible = isListening) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(AstraCyanLight.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(AstraCyanLight)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Astra is listening... Speak your question naturally.",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = AstraCyanLight,
                        fontSize = 12.sp
                    )
                )
            }
        }

        // Input controls row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voice-to-Text Microphone Button
            Box(contentAlignment = Alignment.Center) {
                if (isListening) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(AstraCyanLight.copy(alpha = 0.35f))
                    )
                }

                IconButton(
                    onClick = {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            startSpeechRecognizer()
                        } else {
                            recordAudioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening) {
                                Brush.linearGradient(listOf(AstraCyanLight, AstraIndigoLight))
                            } else {
                                Brush.linearGradient(
                                    listOf(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        )
                        .border(
                            1.dp,
                            if (isListening) AstraCyanLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                            CircleShape
                        )
                        .testTag("voice_input_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Mic else Icons.Default.MicNone,
                        contentDescription = "Voice input - speak your query to Astra",
                        tint = if (isListening) Color.White else AstraCyanLight,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Text Input Field
            OutlinedTextField(
                value = inputText,
                onValueChange = onInputTextChange,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 48.dp, max = 120.dp)
                    .testTag("chat_input_field"),
                placeholder = {
                    Text(
                        text = "Ask Astra or tap mic to speak...",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                },
                trailingIcon = {
                    if (inputText.isNotEmpty()) {
                        IconButton(
                            onClick = { onInputTextChange("") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear input",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AstraIndigoLight,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                ),
                maxLines = 4
            )

            // Send / Stop button
            IconButton(
                onClick = {
                    if (isGenerating) {
                        onStopGeneration()
                    } else if (inputText.isNotBlank()) {
                        val text = inputText
                        onSendMessage(text)
                    }
                },
                enabled = isGenerating || inputText.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (isGenerating) MaterialTheme.colorScheme.error
                        else if (inputText.isNotBlank()) AstraIndigoLight
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    .testTag("send_message_button")
            ) {
                if (isGenerating) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = "Stop generation",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message",
                        tint = if (inputText.isNotBlank()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
