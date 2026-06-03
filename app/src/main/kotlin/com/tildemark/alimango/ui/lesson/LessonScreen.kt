package com.tildemark.alimango.ui.lesson

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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tildemark.alimango.domain.model.Subject
import com.tildemark.alimango.ui.theme.WaniKaniBlue
import com.tildemark.alimango.ui.theme.WaniKaniPink
import com.tildemark.alimango.ui.theme.WaniKaniPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LessonScreen(
    viewModel: LessonViewModel,
    onBackToDashboard: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Lessons Session") },
                navigationIcon = {
                    IconButton(onClick = onBackToDashboard) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Session")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            when (val currentState = state) {
                is LessonUiState.Loading -> {
                    CircularProgressIndicator(color = WaniKaniPink)
                }
                is LessonUiState.Empty -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "No lessons available right now!", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onBackToDashboard,
                            colors = ButtonDefaults.buttonColors(containerColor = WaniKaniPink)
                        ) {
                            Text("Back to Dashboard")
                        }
                    }
                }
                is LessonUiState.Slides -> {
                    LessonSlidesView(
                        state = currentState,
                        onNext = { viewModel.nextSlide() },
                        onPrev = { viewModel.prevSlide() }
                    )
                }
                is LessonUiState.Quiz -> {
                    LessonQuizView(
                        state = currentState,
                        onAnswerChanged = { val currentQuestion = currentState.questions[currentState.currentQuestionIndex]; viewModel.onAnswerChanged(it, currentQuestion.isMeaning) },
                        onSubmit = { viewModel.submitAnswer() }
                    )
                }
                is LessonUiState.Summary -> {
                    LessonSummaryView(
                        state = currentState,
                        onFinish = onBackToDashboard
                    )
                }
            }
        }
    }
}

@Composable
fun LessonSlidesView(
    state: LessonUiState.Slides,
    onNext: () -> Unit,
    onPrev: () -> Unit
) {
    val item = state.items[state.currentItemIndex]
    val subject = item.subject
    val cardColor = when (subject.type) {
        "radical" -> WaniKaniBlue
        "kanji" -> WaniKaniPink
        "vocabulary" -> WaniKaniPurple
        else -> WaniKaniPink
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Step Indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Item ${state.currentItemIndex + 1} of ${state.items.size}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            // Small indicator dots
            val maxSlides = if (subject.readings.isEmpty()) 2 else 3
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(maxSlides) { idx ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (idx == state.currentSlideIndex) cardColor else MaterialTheme.colorScheme.surfaceVariant
                            )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Main Slide Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Banner with matching subject color
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .widthIn(min = 120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(cardColor)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.characters ?: "",
                        fontSize = if ((subject.characters?.length ?: 0) > 4) 32.sp else 54.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                when (state.currentSlideIndex) {
                    0 -> {
                        // Slide 0: General Introduction
                        Text(
                            text = subject.type.uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = cardColor
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = subject.meanings.joinToString(", "),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        if (subject.readings.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Reading",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = subject.readings.joinToString(", "),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = cardColor
                            )
                        }
                    }
                    1 -> {
                        // Slide 1: Meaning Mnemonic
                        Text(
                            text = "Meaning Explanation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = cardColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stripHtmlTags(subject.meaningMnemonic),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start,
                            lineHeight = 24.sp
                        )
                    }
                    2 -> {
                        // Slide 2: Reading Mnemonic (if any)
                        Text(
                            text = "Reading Explanation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = cardColor
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stripHtmlTags(subject.readingMnemonic ?: ""),
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Start,
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Navigation Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = onPrev,
                enabled = !(state.currentItemIndex == 0 && state.currentSlideIndex == 0),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Back", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            val isLast = state.currentItemIndex == state.items.size - 1 && 
                    state.currentSlideIndex == (if (subject.readings.isEmpty()) 1 else 2)

            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = cardColor),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isLast) "Start Quiz" else "Next")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
            }
        }
    }
}

@Composable
fun LessonQuizView(
    state: LessonUiState.Quiz,
    onAnswerChanged: (String) -> Unit,
    onSubmit: () -> Unit
) {
    val currentQuestion = state.questions[state.currentQuestionIndex]
    val subject = currentQuestion.item.subject
    val cardColor = when (subject.type) {
        "radical" -> WaniKaniBlue
        "kanji" -> WaniKaniPink
        "vocabulary" -> WaniKaniPurple
        else -> WaniKaniPink
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "QUIZ TIME",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = WaniKaniPink
        )
        Text(
            text = "Question ${state.currentQuestionIndex + 1} of ${state.questions.size}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Quiz Question card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .height(80.dp)
                        .widthIn(min = 100.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(cardColor)
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = subject.characters ?: "",
                        fontSize = if ((subject.characters?.length ?: 0) > 4) 24.sp else 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = if (currentQuestion.isMeaning) "What is the MEANING?" else "What is the READING?",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = state.inputAnswer,
                    onValueChange = onAnswerChanged,
                    label = { Text(if (currentQuestion.isMeaning) "Enter English Meaning" else "Enter Hiragana Reading") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    isError = state.showError,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { onSubmit() }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = cardColor,
                        focusedLabelColor = cardColor,
                        cursorColor = cardColor
                    )
                )

                if (state.showError) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Incorrect, try again!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onSubmit,
            colors = ButtonDefaults.buttonColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit Answer", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LessonSummaryView(
    state: LessonUiState.Summary,
    onFinish: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Success",
                tint = Color.White,
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Lessons Completed!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "You learned and started ${state.itemsCount} subjects successfully.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(30.dp))

        Button(
            onClick = onFinish,
            colors = ButtonDefaults.buttonColors(containerColor = WaniKaniPink),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Return to Dashboard", fontWeight = FontWeight.Bold)
        }
    }
}

private fun stripHtmlTags(html: String): String {
    return html.replace(Regex("<[^>]*>"), "")
}
