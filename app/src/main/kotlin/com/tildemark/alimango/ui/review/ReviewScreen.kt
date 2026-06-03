package com.tildemark.alimango.ui.review

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import com.tildemark.alimango.ui.theme.WaniKaniBlue
import com.tildemark.alimango.ui.theme.WaniKaniPink
import com.tildemark.alimango.ui.theme.WaniKaniPurple
import dev.esnault.wanakana.core.Wanakana

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    viewModel: ReviewViewModel,
    onSessionFinished: (correct: Int, total: Int) -> Unit,
    onBackToDashboard: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var answerInput by remember { mutableStateOf("") }
    
    // Media player for vocabulary audio
    val mediaPlayer = remember { MediaPlayer() }
    
    LaunchedEffect(state) {
        if (state is ReviewUiState.SessionFinished) {
            val finishedState = state as ReviewUiState.SessionFinished
            onSessionFinished(finishedState.correctCount, finishedState.totalCount)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Review Session") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        when (val currentState = state) {
            is ReviewUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = WaniKaniPink)
                }
            }
            is ReviewUiState.Empty -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No reviews available!",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onBackToDashboard,
                        colors = ButtonDefaults.buttonColors(containerColor = WaniKaniPink)
                    ) {
                        Text("Back to Dashboard")
                    }
                }
            }
            is ReviewUiState.Active -> {
                val subject = currentState.currentItem.subject
                val isReading = currentState.questionType == QuestionType.READING

                var currentSubjectId by remember { mutableStateOf(-1) }
                var showHelp by remember { mutableStateOf(false) }

                if (currentSubjectId != subject.id) {
                    currentSubjectId = subject.id
                    showHelp = false
                }

                // Subject character card background color based on type
                val cardColor = when (subject.type) {
                    "radical" -> WaniKaniBlue
                    "kanji" -> WaniKaniPink
                    "vocabulary" -> WaniKaniPurple
                    else -> WaniKaniPink
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Linear progress indicator
                    LinearProgressIndicator(
                        progress = { currentState.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp),
                        color = WaniKaniPink,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Item ${currentState.correctCount} of ${currentState.totalCount}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))

                    // Character card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = subject.characters ?: (subject.meanings.firstOrNull() ?: ""),
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Audio play button for Vocabulary
                    if (subject.type == "vocabulary" && subject.audioUrls.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                try {
                                    mediaPlayer.reset()
                                    mediaPlayer.setAudioAttributes(
                                        AudioAttributes.Builder()
                                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                                            .setUsage(AudioAttributes.USAGE_MEDIA)
                                            .build()
                                    )
                                    // Use first available MP3 audio url
                                    val audioUrl = subject.audioUrls.firstOrNull { it.endsWith(".mp3") } ?: subject.audioUrls.first()
                                    mediaPlayer.setDataSource(audioUrl)
                                    mediaPlayer.prepareAsync()
                                    mediaPlayer.setOnPreparedListener { it.start() }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            },
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Play Pronunciation",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Prompt text
                    Text(
                        text = if (isReading) "Reading" else "Meaning",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )

                    Text(
                        text = when (subject.type) {
                            "radical" -> "Radical"
                            "kanji" -> "Kanji"
                            "vocabulary" -> "Vocabulary"
                            else -> ""
                        },
                        fontSize = 14.sp,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Text Input
                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = { input ->
                            // Convert Romaji to Kana in real-time for reading questions
                            answerInput = if (isReading) {
                                Wanakana.toHiragana(input)
                            } else {
                                input
                            }
                        },
                        label = { Text("Enter answer...") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = cardColor,
                            focusedLabelColor = cardColor,
                            cursorColor = cardColor
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onAny = {
                                if (answerInput.isNotBlank()) {
                                    viewModel.submitAnswer(answerInput)
                                    answerInput = ""
                                }
                            }
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (answerInput.isNotBlank()) {
                                viewModel.submitAnswer(answerInput)
                                answerInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = cardColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = answerInput.isNotBlank()
                    ) {
                        Text(
                            text = "Submit",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    if (!showHelp) {
                        Button(
                            onClick = { showHelp = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = cardColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Need a Hint?")
                        }
                    } else {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (isReading) "Reading Mnemonic" else "Meaning Mnemonic",
                                    fontWeight = FontWeight.Bold,
                                    color = cardColor,
                                    fontSize = 14.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                val rawText = if (isReading) {
                                    subject.readingMnemonic ?: "No reading mnemonic available."
                                } else {
                                    subject.meaningMnemonic
                                }
                                val cleanText = rawText.replace(Regex("<[^>]*>"), "")
                                Text(
                                    text = cleanText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            else -> {}
        }
    }
}
